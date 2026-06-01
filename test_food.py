import urllib.request
import urllib.error
import json

boundary = "----TestBoundary123"
body = (
    f"--{boundary}\r\n"
    f'Content-Disposition: form-data; name="food"\r\n'
    f"Content-Type: application/json\r\n\r\n"
    f'{{"name":"Test Biryani","description":"Test desc","price":250,"category":"Biryani"}}\r\n'
    f"--{boundary}\r\n"
    f'Content-Disposition: form-data; name="file"; filename="test.jpg"\r\n'
    f"Content-Type: image/jpeg\r\n\r\n"
    f"FAKEIMAGEBYTES\r\n"
    f"--{boundary}--\r\n"
).encode()

req = urllib.request.Request(
    "http://localhost:8080/api/foods",
    data=body,
    headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
    method="POST"
)
try:
    with urllib.request.urlopen(req) as r:
        print("SUCCESS:", r.status, r.read().decode())
except urllib.error.HTTPError as e:
    print("HTTP ERROR:", e.code)
    print("RESPONSE:", e.read().decode())
except Exception as e:
    print("ERROR:", type(e).__name__, str(e))
