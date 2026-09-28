from datetime import datetime, timezone
import os
import json

from util import file_sha256


DATA_SUBDIRS = {"gods", "items"}


def build_manifest(data_root: str, patch: str) -> dict:
    manifest = {
        "patch": patch,
        "pulled_at": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "source": "https://wiki.smite2.com",
        "counts": {}
    }

    for folder in DATA_SUBDIRS:
        path = os.path.join(data_root, folder)
        print(f"Adding {path} to manifest")

        entries = []
        for filename in sorted(os.listdir(path)):
            file_path = os.path.join(path, filename)
            if not filename.endswith(".json"):
                print(f"Skipping {file_path}")
                continue

            with open(file_path, "r", encoding="utf-8") as f:
                name = json.load(f).get("name", filename[:-5])

            entries.append({
                "name": name,
                "file": os.path.join(folder, filename),
                "sha256": file_sha256(file_path)
            })

        manifest[folder] = entries
        manifest["counts"][folder] = len(entries)

    return manifest


def create_manifest(data_root: str, patch: str) -> None:
    manifest = build_manifest(data_root, patch)
    manifest_path = os.path.join(data_root, "manifest.json")

    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2, ensure_ascii=False)
