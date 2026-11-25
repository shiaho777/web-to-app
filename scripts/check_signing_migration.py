#!/usr/bin/env python3
import contextlib
import http.server
import os
from pathlib import Path
import subprocess
import tempfile
import threading
import time


ROOT = Path(__file__).resolve().parent.parent
PORT = 18742
FIXTURE = b"""<!doctype html><html lang="en"><head><meta charset="utf-8">
<title>Signing migration</title></head><body>
<h1 id="state"></h1><script>
const key = 'wta-signing-migration';
const state = document.getElementById('state');
if (VERIFY_AFTER_UPDATE) {
    state.textContent = localStorage.getItem(key) === 'retained'
        ? 'SIGNING MIGRATION DATA RETAINED' : 'SIGNING MIGRATION DATA MISSING';
} else {
    localStorage.setItem(key, 'retained');
    state.textContent = 'SIGNING MIGRATION DATA SAVED';
}
</script></body></html>"""


class FixtureHandler(http.server.BaseHTTPRequestHandler):
    verify_after_update = False

    def do_GET(self):
        body = FIXTURE.replace(b"VERIFY_AFTER_UPDATE", b"true" if self.verify_after_update else b"false")
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def adb(*args, **kwargs):
    return subprocess.run(["adb", *args], text=True, **kwargs)


def launch_and_wait(package_name, expected):
    resolved = adb(
        "shell", "cmd", "package", "resolve-activity", "--brief", "-a", "android.intent.action.MAIN",
        "-c", "android.intent.category.LAUNCHER", "-p", package_name,
        capture_output=True, check=True, timeout=20,
    )
    matches = resolved.stdout.strip().splitlines()
    component = matches[-1] if matches else ""
    if not component.startswith(package_name + "/"):
        raise RuntimeError(resolved.stdout + resolved.stderr)
    launch = adb("shell", "am", "start", "-W", "-n", component, capture_output=True, check=True, timeout=20)
    if "Status: ok" not in launch.stdout:
        raise RuntimeError(launch.stdout + launch.stderr)

    device_dump = f"/data/local/tmp/{package_name}-smoke.xml"
    try:
        deadline = time.monotonic() + 45
        while time.monotonic() < deadline:
            dump = adb("shell", "uiautomator", "dump", device_dump, capture_output=True, timeout=15)
            if dump.returncode == 0:
                xml = adb("shell", "cat", device_dump, capture_output=True, check=True, timeout=10).stdout
                if expected in xml:
                    print(expected, flush=True)
                    return
                if "SIGNING MIGRATION DATA MISSING" in xml:
                    raise RuntimeError("The updated APK could not read the data saved by version 1.")
            time.sleep(1)
    finally:
        adb("shell", "rm", "-f", device_dump, timeout=10)
    raise RuntimeError(f"Expected screen text: {expected}")


def install_fixture(cleanup, package_name, apk):
    existing = adb("shell", "pm", "path", package_name, capture_output=True, timeout=10)
    if existing.stdout.strip():
        raise RuntimeError(f"A package named {package_name} is already on the device.")
    adb("install", str(apk), check=True, timeout=90)
    cleanup.callback(adb, "uninstall", package_name, timeout=30)


def verify_generated_apks(result_dir):
    package_name = (result_dir / "package-name.txt").read_text().strip()
    offline_package = (result_dir / "offline-package-name.txt").read_text().strip()
    with contextlib.ExitStack() as cleanup:
        install_fixture(cleanup, offline_package, result_dir / "offline-startup.apk")
        launch_and_wait(offline_package, "PACKAGED OFFLINE HTML STARTED")

    with http.server.ThreadingHTTPServer(("127.0.0.1", PORT), FixtureHandler) as server:
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        with contextlib.ExitStack() as cleanup:
            cleanup.callback(server.shutdown)
            endpoint = f"tcp:{PORT}"
            adb("reverse", "--no-rebind", endpoint, endpoint, check=True, timeout=10)
            cleanup.callback(adb, "reverse", "--remove", endpoint, timeout=10)
            install_fixture(cleanup, package_name, result_dir / "migration-v1.apk")
            launch_and_wait(package_name, "SIGNING MIGRATION DATA SAVED")
            FixtureHandler.verify_after_update = True
            adb("install", "-r", str(result_dir / "migration-v2.apk"), check=True, timeout=90)
            launch_and_wait(package_name, "SIGNING MIGRATION DATA RETAINED")


def main():
    adb("get-state", check=True, timeout=10)
    with tempfile.TemporaryDirectory(prefix="wta-signing-smoke-") as work:
        build = subprocess.run(
            [
                str(ROOT / "gradlew"),
                ":app:syncShellTemplateApk",
                ":app:testStandardDebugUnitTest",
                "--tests", "com.webtoapp.core.backup.SigningMigrationTest",
                "--rerun",
            ],
            cwd=ROOT, env={**os.environ, "WTA_SIGNING_SMOKE_DIR": work},
        )
        build.check_returncode()

        verify_generated_apks(Path(work))


if __name__ == "__main__":
    main()
