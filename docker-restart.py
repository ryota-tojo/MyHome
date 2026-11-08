import http.client
import shlex
import subprocess
import sys
import time
import urllib.error
import urllib.request

WSL_DISTRO = "Ubuntu-22.04"
PROJECT_DIR = "/home/ryota-tojo/projects/my_home"
CHECK_URL = "http://localhost:8081/login"
TIMEOUT_SECONDS = 90
INTERVAL_SECONDS = 3


def run_wsl(command: str, check: bool = True) -> str:
    result = subprocess.run(
        ["wsl.exe", "-d", WSL_DISTRO, "--", "bash", "-lc", command],
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if check and result.returncode != 0:
        raise RuntimeError(f"コマンド失敗: {command}\n{result.stderr.strip()}")
    return result.stdout.strip()


def compose(command: str, check: bool = True) -> str:
    return run_wsl(f"cd {shlex.quote(PROJECT_DIR)} && docker compose {command}", check)


def rebuild_and_restart() -> None:
    print("[1/3] PostgreSQLを起動します")
    compose("up -d postgres")

    print("[2/3] Javaをソースから再ビルドします")
    # docker restart/start だけではソース変更が反映されないため、必ずbuildして再作成する。
    compose("build web")
    compose("up -d --force-recreate web")

    print("[3/3] Javaの起動完了を待機します")
    deadline = time.monotonic() + TIMEOUT_SECONDS
    while time.monotonic() < deadline:
        status = run_wsl(
            "curl -sS -o /dev/null -w '%{http_code}' " + shlex.quote(CHECK_URL),
            check=False,
        )
        if status == "200":
            print("[OK] Java起動完了: HTTP 200")
            return
        time.sleep(INTERVAL_SECONDS)
    raise RuntimeError("Javaの起動確認がタイムアウトしました")


def check_windows_localhost() -> None:
    opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
    try:
        with opener.open(CHECK_URL, timeout=5) as response:
            print(f"[OK] Windowsから接続可能: HTTP {response.status}")
    except (urllib.error.URLError, http.client.HTTPException, OSError) as error:
        print(f"[WARN] Javaは起動していますが、Windowsのlocalhost転送を確認できません: {error}")


def main() -> int:
    print("====================================")
    print(" Home Application Rebuild & Restart")
    print("====================================")
    try:
        rebuild_and_restart()
        check_windows_localhost()
        print(compose("ps"))
        print(f"URL: {CHECK_URL}")
        return 0
    except Exception as error:
        print(f"[ERROR] {error}")
        print(compose("logs --tail=80 web", check=False))
        return 1


if __name__ == "__main__":
    sys.exit(main())
