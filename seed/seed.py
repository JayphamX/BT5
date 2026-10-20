"""Nạp dữ liệu mẫu (3 danh mục, 10 khóa học) lên MockAPI.

Cách dùng:
    python3 seed.py https://YOUR_PROJECT_ID.mockapi.io/api/v1

Lưu ý: chạy 1 lần trên project còn trống (xóa dữ liệu fake sinh sẵn nếu có).
categoryId trong courses.json ("1","2","3") được ánh xạ sang id THẬT do MockAPI trả về.
"""
import json
import pathlib
import sys
import urllib.request

if len(sys.argv) != 2:
    sys.exit("Dùng: python3 seed.py <BASE_URL>   (vd: https://abc123.mockapi.io/api/v1)")

BASE = sys.argv[1].rstrip("/")
HERE = pathlib.Path(__file__).parent


def post(path, body):
    req = urllib.request.Request(
        f"{BASE}/{path}",
        data=json.dumps(body).encode("utf-8"),
        headers={"Content-Type": "application/json", "User-Agent": "Mozilla/5.0"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.load(resp)


categories = json.loads((HERE / "categories.json").read_text(encoding="utf-8"))
courses = json.loads((HERE / "courses.json").read_text(encoding="utf-8"))

id_map = {}
for c in categories:
    created = post("categories", {"name": c["name"], "description": c["description"]})
    id_map[c["id"]] = created["id"]
    print(f"[categories] {c['name']} -> id {created['id']}")

for course in courses:
    body = dict(course)
    body["categoryId"] = id_map[body["categoryId"]]
    created = post("courses", body)
    print(f"[courses] {body['title']} -> id {created['id']}")

print("Hoàn tất: 3 danh mục + 10 khóa học.")
