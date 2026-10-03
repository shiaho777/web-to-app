#!/usr/bin/env python3
"""Rebuild sample-bundles/*.zip + manifest.json from unpacked pack directories.

The heavy per-framework sample dependency packs (python-*-shared .pypackages,
go-*-shared vendor) are NOT shipped inside the APK — SampleSharedPackManager
downloads them on first use. This script turns a directory of unpacked packs
into the publishable artifacts committed under sample-bundles/.

Usage:
    python3 scripts/build_sample_bundles.py <packs_root>

    <packs_root>/python-django-shared/.pypackages/...  ->  sample-bundles/python-django-shared.zip
    + sample-bundles/manifest.json  (sha256 + byte size per pack)

Zips are deterministic: entries sorted, timestamps pinned to the Unix epoch,
extra fields stripped — identical input trees always produce identical SHA-256,
so manifest.json only changes when pack content actually changes.
"""

import hashlib
import json
import os
import sys
import zipfile

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT_DIR = os.path.join(REPO_ROOT, "sample-bundles")

# zipfile requires dates >= 1980; epoch-truncated DOS date for determinism.
FIXED_DATE = (1980, 1, 1, 0, 0, 0)


def build_pack(pack_dir: str, zip_path: str) -> dict:
    entries = []
    for root, dirs, files in os.walk(pack_dir):
        dirs.sort()
        for name in sorted(files):
            full = os.path.join(root, name)
            rel = os.path.relpath(full, os.path.dirname(pack_dir))
            entries.append((rel.replace(os.sep, "/"), full))

    tmp_path = zip_path + ".tmp"
    with zipfile.ZipFile(tmp_path, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as zf:
        for rel, full in sorted(entries):
            info = zipfile.ZipInfo(rel, FIXED_DATE)
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = (0o755 if os.access(full, os.X_OK) else 0o644) << 16
            with open(full, "rb") as f:
                zf.writestr(info, f.read())
    os.replace(tmp_path, zip_path)

    with open(zip_path, "rb") as f:
        digest = hashlib.sha256(f.read()).hexdigest()
    return {"sha256": digest, "bytes": os.path.getsize(zip_path)}


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__.strip())
        return 2

    packs_root = sys.argv[1]
    packs = sorted(
        d for d in os.listdir(packs_root)
        if os.path.isdir(os.path.join(packs_root, d)) and d.endswith("-shared")
    )
    if not packs:
        print(f"no *-shared pack directories under {packs_root}")
        return 1

    os.makedirs(OUT_DIR, exist_ok=True)
    manifest = {}
    for name in packs:
        zip_path = os.path.join(OUT_DIR, f"{name}.zip")
        manifest[name] = build_pack(os.path.join(packs_root, name), zip_path)
        size_mb = manifest[name]["bytes"] / 1048576
        print(f"{name}: {size_mb:.2f} MB  sha256={manifest[name]['sha256'][:16]}...")

    manifest_path = os.path.join(OUT_DIR, "manifest.json")
    with open(manifest_path, "w") as f:
        json.dump({"packs": manifest}, f, indent=2, sort_keys=True)
        f.write("\n")
    print(f"wrote {manifest_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
