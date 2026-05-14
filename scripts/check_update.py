import json
import urllib.request
from dataclasses import dataclass
from typing import Optional


@dataclass
class UpdateInfo:
    version: str
    download_url: str
    release_date: str
    notes: str


def check_for_update(
    repo_owner: str,
    repo_name: str,
    current_version: str,
    branch: str = "master",
) -> Optional[UpdateInfo]:
    url = (
        f"https://raw.githubusercontent.com/"
        f"{repo_owner}/{repo_name}/{branch}/update.json"
    )
    req = urllib.request.Request(url, headers={"User-Agent": "BeerZaao/1.0"})

    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except Exception as e:
        print(f"Failed to fetch update info: {e}")
        return None

    if _compare_versions(data["version"], current_version) <= 0:
        return None

    return UpdateInfo(
        version=data["version"],
        download_url=data["download_url"],
        release_date=data["release_date"],
        notes=data["notes"],
    )


def download_apk(info: UpdateInfo, save_path: str) -> bool:
    try:
        req = urllib.request.Request(
            info.download_url, headers={"User-Agent": "BeerZaao/1.0"}
        )
        with urllib.request.urlopen(req, timeout=120) as resp:
            with open(save_path, "wb") as f:
                while chunk := resp.read(8192):
                    f.write(chunk)
        print(f"Downloaded to {save_path}")
        return True
    except Exception as e:
        print(f"Download failed: {e}")
        return False


def _compare_versions(v1: str, v2: str) -> int:
    parts1 = [int(x) for x in v1.split(".")]
    parts2 = [int(x) for x in v2.split(".")]
    max_len = max(len(parts1), len(parts2))
    for i in range(max_len):
        a = parts1[i] if i < len(parts1) else 0
        b = parts2[i] if i < len(parts2) else 0
        if a != b:
            return a - b
    return 0


if __name__ == "__main__":
    import sys

    repo = "GanJiaKouN16/BeerZaao-apps"
    owner, name = repo.split("/")
    current = sys.argv[1] if len(sys.argv) > 1 else "0.0.0"

    info = check_for_update(owner, name, current)
    if info:
        print(f"Update available: v{info.version}")
        print(f"  Download: {info.download_url}")
        print(f"  Date: {info.release_date}")
        print(f"  Notes: {info.notes}")
    else:
        print("Already up to date.")
