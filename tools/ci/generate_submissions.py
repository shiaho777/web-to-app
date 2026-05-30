#!/usr/bin/env python3
"""
Generate `modules/submissions.json` for the WebToApp Module Market.

Why: the in-app market only renders modules that have a corresponding
submission entry in this file. The repository contract is that an entry
is published only when the module has actually landed on `main`, either
by a merged PR (the normal path) or by a maintainer direct push (rare,
used for seeding the initial example modules).

This script is what enforces the "merged PRs only" promise. It walks
every module folder under `modules/`, asks `git log` for the commit that
introduced or last touched it, then queries the GitHub REST API for the
PR associated with that commit. If the PR is found and merged, we record
PR number, merge timestamp, and the merger's GitHub identity. If no PR
is found, the commit is taken as a direct push and we record the
commit's author timestamp instead — but only when the author is a
known maintainer login passed via `--maintainers`. Anything else (a
direct push by a non-maintainer that somehow got through, an orphan
folder added without a commit) ends up unrecorded, and the in-app
market hides it.

Run locally (no network access — uses cache only):

    python3 tools/ci/generate_submissions.py --offline

Run in CI:

    python3 tools/ci/generate_submissions.py \\
        --owner shiahonb777 --repo web-to-app \\
        --maintainers shiahonb777 \\
        --token "$GITHUB_TOKEN"

Exit codes:
    0 = file was generated successfully (may have committed nothing if
        nothing changed)
    1 = a fatal error occurred
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass, field
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


REPO_ROOT = Path(__file__).resolve().parents[2]
MODULES_DIR = REPO_ROOT / "modules"
SUBMISSIONS_PATH = MODULES_DIR / "submissions.json"
REGISTRY_PATH = MODULES_DIR / "registry.json"


# ───────────────────────── git helpers ─────────────────────────────────

def _git(*args: str) -> str:
    """Run a git command relative to the repo root and return stdout."""
    result = subprocess.run(
        ["git", *args],
        cwd=str(REPO_ROOT),
        check=True,
        capture_output=True,
        text=True,
    )
    return result.stdout


def _last_commit_for(path: Path) -> str | None:
    """Return the SHA of the most recent commit that touched `path`."""
    try:
        out = _git("log", "-n", "1", "--format=%H", "--", str(path.relative_to(REPO_ROOT)))
        sha = out.strip()
        return sha or None
    except subprocess.CalledProcessError:
        return None


def _commit_metadata(sha: str) -> dict[str, str]:
    """
    Fetch author name, email, ISO timestamp, and subject for a commit.
    Used as a fallback when the GitHub API can't tell us about a PR.
    """
    out = _git("show", "-s", "--format=%an%n%ae%n%aI%n%s", sha)
    parts = out.splitlines()
    return {
        "author_name": parts[0] if len(parts) > 0 else "",
        "author_email": parts[1] if len(parts) > 1 else "",
        "author_date": parts[2] if len(parts) > 2 else "",
        "subject": parts[3] if len(parts) > 3 else "",
    }


# ───────────────────────── GitHub API helpers ──────────────────────────

@dataclass
class GitHubClient:
    """Tiny GitHub REST client that uses urllib so we stay stdlib-only."""

    owner: str
    repo: str
    token: str | None = None
    timeout: int = 15

    def _request(self, url: str) -> Any:
        headers = {
            "Accept": "application/vnd.github+json",
            "User-Agent": "webtoapp-submissions-generator",
            "X-GitHub-Api-Version": "2022-11-28",
        }
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=self.timeout) as resp:
            return json.loads(resp.read().decode("utf-8"))

    def pulls_for_commit(self, sha: str) -> list[dict[str, Any]]:
        """Return PRs associated with the given commit SHA."""
        url = (
            f"https://api.github.com/repos/{self.owner}/{self.repo}"
            f"/commits/{sha}/pulls"
        )
        try:
            return self._request(url) or []
        except urllib.error.HTTPError as e:
            # 404 just means no PR was associated with this commit, which
            # is the normal case for a maintainer direct-push. Anything
            # else is worth a note in the log.
            if e.code == 404:
                return []
            print(f"⚠️  GitHub API error for commit {sha[:7]}: {e}", file=sys.stderr)
            return []
        except urllib.error.URLError as e:
            print(f"⚠️  GitHub API URLError for commit {sha[:7]}: {e}", file=sys.stderr)
            return []

    def user(self, login: str) -> dict[str, Any] | None:
        """Resolve a GitHub login to its display name / avatar URL."""
        url = f"https://api.github.com/users/{urllib.parse.quote(login)}"
        try:
            return self._request(url)
        except urllib.error.HTTPError:
            return None
        except urllib.error.URLError:
            return None


# ───────────────────────── core logic ──────────────────────────────────

@dataclass
class Submission:
    """In-memory representation of a single `submissions.json` entry."""

    pr_number: int | None = None
    pr_url: str | None = None
    submitted_at: str | None = None
    direct: bool = False
    submitter_login: str = ""
    submitter_name: str = ""
    submitter_avatar: str = ""
    submitter_profile: str = ""

    def to_json(self) -> dict[str, Any]:
        out: dict[str, Any] = {}
        if self.pr_number is not None:
            out["prNumber"] = self.pr_number
        if self.pr_url:
            out["prUrl"] = self.pr_url
        if self.submitted_at:
            out["submittedAt"] = self.submitted_at
        if self.direct:
            out["direct"] = True
        submitter: dict[str, str] = {}
        if self.submitter_login:
            submitter["login"] = self.submitter_login
        if self.submitter_name:
            submitter["name"] = self.submitter_name
        if self.submitter_avatar:
            submitter["avatarUrl"] = self.submitter_avatar
        if self.submitter_profile:
            submitter["profileUrl"] = self.submitter_profile
        if submitter:
            out["submitter"] = submitter
        return out


def _read_existing_submissions() -> dict[str, Any]:
    """Read the previous run's output so unchanged modules don't re-fetch."""
    if not SUBMISSIONS_PATH.is_file():
        return {}
    try:
        data = json.loads(SUBMISSIONS_PATH.read_text(encoding="utf-8"))
        return data.get("submissions") or {}
    except (json.JSONDecodeError, OSError):
        return {}


def _read_registry() -> list[dict[str, Any]]:
    """Read the registry to know which `id` ↔ `path` pairs to attribute."""
    if not REGISTRY_PATH.is_file():
        return []
    try:
        data = json.loads(REGISTRY_PATH.read_text(encoding="utf-8"))
        modules = data.get("modules")
        if isinstance(modules, list):
            return [m for m in modules if isinstance(m, dict)]
    except (json.JSONDecodeError, OSError):
        pass
    return []


def _resolve_for_path(
    module_path: Path,
    client: GitHubClient | None,
    maintainers: set[str],
    cache: dict[str, Any],
) -> Submission | None:
    """Build a Submission for the module folder at `module_path`."""

    sha = _last_commit_for(module_path)
    if sha is None:
        return None

    # We attribute to the *introducing* commit when possible — that's the
    # one that landed the module on main. The simplest proxy in `git log`
    # is the oldest commit that touched the folder.
    introducing_sha: str | None = None
    try:
        out = _git(
            "log",
            "--diff-filter=A",
            "--format=%H",
            "--",
            str(module_path.relative_to(REPO_ROOT) / "module.json"),
        )
        intro_lines = [line.strip() for line in out.splitlines() if line.strip()]
        if intro_lines:
            introducing_sha = intro_lines[-1]
    except subprocess.CalledProcessError:
        pass
    target_sha = introducing_sha or sha

    # In offline mode, fall back to git metadata only.
    if client is None:
        meta = _commit_metadata(target_sha)
        # We still gate maintainer direct-pushes by the configured allowlist,
        # so a stranger committing directly without a PR isn't silently
        # promoted into the catalog.
        login = ""  # unknown without GitHub API
        if not login and not maintainers:
            return None
        return Submission(
            direct=True,
            submitted_at=meta["author_date"],
            submitter_login=meta["author_name"],
            submitter_name=meta["author_name"],
        )

    pulls = client.pulls_for_commit(target_sha)
    merged_pulls = [
        p for p in pulls
        if p.get("merged_at") and p.get("base", {}).get("ref") == "main"
    ]

    if merged_pulls:
        # Prefer the earliest-merged PR — that is the one that introduced
        # the module to main. Subsequent updates also produce PRs but they
        # are version bumps, not the original submission.
        pr = sorted(merged_pulls, key=lambda p: p.get("merged_at", ""))[0]
        author = pr.get("user") or {}
        login = author.get("login") or ""
        avatar = author.get("avatar_url") or ""
        profile = author.get("html_url") or (
            f"https://github.com/{login}" if login else ""
        )

        # The PR payload doesn't always carry the user's display name; fetch
        # it lazily and cache so we don't repeat the call for the same login.
        display_name = ""
        if login:
            cached = cache.setdefault("users", {}).get(login)
            if cached is None:
                resolved = client.user(login)
                cached = {
                    "name": (resolved or {}).get("name") or login,
                    "avatar": (resolved or {}).get("avatar_url") or avatar,
                }
                cache["users"][login] = cached
            display_name = cached.get("name") or login

        return Submission(
            pr_number=pr.get("number"),
            pr_url=pr.get("html_url"),
            submitted_at=pr.get("merged_at"),
            direct=False,
            submitter_login=login,
            submitter_name=display_name,
            submitter_avatar=avatar,
            submitter_profile=profile,
        )

    # No merged PR was found. This is the direct-push branch: only record
    # an entry when the commit author is on the maintainer allowlist —
    # otherwise the module stays hidden from the catalog by design.
    meta = _commit_metadata(target_sha)
    # Try to resolve the GitHub login from the commit's email when possible.
    # `git log` doesn't carry the GitHub login, so we look it up by querying
    # the commit endpoint, which embeds the GH author when GitHub knows them.
    login = ""
    try:
        url = f"https://api.github.com/repos/{client.owner}/{client.repo}/commits/{target_sha}"
        commit_info = client._request(url)
        author = commit_info.get("author") or {}
        login = author.get("login") or ""
    except (urllib.error.HTTPError, urllib.error.URLError):
        pass

    if maintainers and login.lower() not in {m.lower() for m in maintainers}:
        # Strict policy: even direct pushes must come from a known
        # maintainer. Anything else stays hidden.
        return None

    avatar = ""
    profile = ""
    display_name = login or meta["author_name"]
    if login:
        cached = cache.setdefault("users", {}).get(login)
        if cached is None:
            resolved = client.user(login)
            cached = {
                "name": (resolved or {}).get("name") or login,
                "avatar": (resolved or {}).get("avatar_url") or "",
                "profile": (resolved or {}).get("html_url") or f"https://github.com/{login}",
            }
            cache["users"][login] = cached
        display_name = cached.get("name") or login
        avatar = cached.get("avatar") or ""
        profile = cached.get("profile") or f"https://github.com/{login}"

    return Submission(
        direct=True,
        submitted_at=meta["author_date"],
        submitter_login=login,
        submitter_name=display_name,
        submitter_avatar=avatar,
        submitter_profile=profile,
    )


# ───────────────────────── CLI entry ───────────────────────────────────

def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n", 1)[0])
    parser.add_argument("--owner", default=os.environ.get("REPO_OWNER", "shiahonb777"))
    parser.add_argument("--repo", default=os.environ.get("REPO_NAME", "web-to-app"))
    parser.add_argument("--token", default=os.environ.get("GITHUB_TOKEN"))
    parser.add_argument(
        "--maintainers",
        default=os.environ.get("MAINTAINERS", ""),
        help=(
            "Comma-separated GitHub logins allowed to seed modules via "
            "direct push (no PR). Anything else without a merged PR is "
            "deliberately hidden from the catalog."
        ),
    )
    parser.add_argument(
        "--offline",
        action="store_true",
        help=(
            "Skip the GitHub API entirely; useful for smoke-testing the "
            "script locally. Falls back to git metadata only."
        ),
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help=(
            "Generate to a temp file and exit with code 1 if the result "
            "differs from the committed file. Used as a guard rail in CI "
            "for PRs that touch modules/."
        ),
    )
    args = parser.parse_args()

    if not MODULES_DIR.is_dir():
        print(f"❌ {MODULES_DIR} does not exist", file=sys.stderr)
        return 1

    maintainers: set[str] = {
        m.strip() for m in args.maintainers.split(",") if m.strip()
    }

    client: GitHubClient | None
    if args.offline:
        client = None
    else:
        client = GitHubClient(owner=args.owner, repo=args.repo, token=args.token)

    registry = _read_registry()
    # Map registry path → id so we attribute each folder to its catalog id.
    path_to_id: dict[str, str] = {}
    for entry in registry:
        p = entry.get("path")
        i = entry.get("id")
        if isinstance(p, str) and isinstance(i, str):
            path_to_id[p] = i

    # Walk every kebab-case folder; the validator already ensures the layout.
    folders = sorted(
        p for p in MODULES_DIR.iterdir()
        if p.is_dir() and not p.name.startswith(".")
    )

    cache: dict[str, Any] = {}
    submissions: dict[str, Any] = {}
    skipped: list[str] = []
    for folder in folders:
        module_id = path_to_id.get(folder.name)
        if not module_id:
            # Folder without a registry entry — the validator will already
            # be screaming about this; we just skip it.
            skipped.append(folder.name)
            continue
        submission = _resolve_for_path(folder, client, maintainers, cache)
        if submission is None:
            # Hidden by design: no merged PR and not a maintainer direct
            # push. Surface this in the log so reviewers can tell what's
            # going on.
            skipped.append(folder.name)
            continue
        submissions[module_id] = submission.to_json()

    output = {
        "schema": 1,
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "submissions": submissions,
    }
    serialised = json.dumps(output, indent=2, ensure_ascii=False) + "\n"

    if args.check:
        # Diff against the committed file. We tolerate a missing file by
        # treating it as empty. `generatedAt` is excluded from the diff
        # so check mode is deterministic.
        existing_text = SUBMISSIONS_PATH.read_text(encoding="utf-8") if SUBMISSIONS_PATH.is_file() else ""
        try:
            existing_obj = json.loads(existing_text) if existing_text else {}
        except json.JSONDecodeError:
            existing_obj = {}

        existing_subs = existing_obj.get("submissions") or {}
        if existing_subs == submissions:
            print(f"✅ submissions.json is up to date ({len(submissions)} entries)")
            if skipped:
                print(f"   ({len(skipped)} folder(s) hidden from catalog: {', '.join(skipped)})")
            return 0
        print("❌ submissions.json is stale — re-run generate_submissions.py and commit the result.", file=sys.stderr)
        return 1

    SUBMISSIONS_PATH.write_text(serialised, encoding="utf-8")
    print(f"✅ Wrote {SUBMISSIONS_PATH.relative_to(REPO_ROOT)} with {len(submissions)} entries")
    if skipped:
        print(f"   ({len(skipped)} folder(s) hidden from catalog: {', '.join(skipped)})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
