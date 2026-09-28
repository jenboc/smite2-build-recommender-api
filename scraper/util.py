import hashlib


def file_sha256(path: str) -> str:
    h = hashlib.sha256()

    with open(path, "rb") as f:
        for block in iter(lambda: f.read(8192), b""):
            h.update(block)

    return h.hexdigest()
