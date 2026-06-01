import urllib.request
import urllib.error
import json
import base64

# Create a minimal valid JPEG (1x1 pixel)
TINY_JPEG = base64.b64decode(
    "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8U"
    "HRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgN"
    "DRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIy"
    "MjIyMjL/wAARCAABAAEDASIAAhEBAxEB/8QAFAABAAAAAAAAAAAAAAAAAAAACf/EABQQAQAA"
    "AAAAAAAAAAAAAAAAAP/EABQBAQAAAAAAAAAAAAAAAAAAAAD/xAAUEQEAAAAAAAAAAAAAAAAA"
    "AAAA/9oADAMBAAIRAxEAPwCwABmX/9k="
)

boundary = "----FormBoundary7MA4YWxkTrZu0gW"

def make_multipart(boundary, food_json, image_bytes):
    parts = []
    parts.append(f"--{boundary}\r\n".encode())
    parts.append(b'Content-Disposition: form-data; name="food"\r\n')
    parts.append(b"Content-Type: application/json\r\n\r\n")
    parts.append(food_json.encode())
    parts.append(b"\r\n")
    parts.append(f"--{boundary}\r\n".encode())
    parts.append(b'Content-Disposition: form-data; name="file"; filename="test.jpg"\r\n')
    parts.append(b"Content-Type: image/jpeg\r\n\r\n")
    parts.append(image_bytes)
    parts.append(b"\r\n")
    parts.append(f"--{boundary}--\r\n".encode())
    return b"".join(parts)

food_json = '{"name":"Chicken Biryani","description":"Delicious biryani","price":250,"category":"Biryani"}'
body = make_multipart(boundary, food_json, TINY_JPEG)

req = urllib.request.Request(
    "http://localhost:8080/api/foods",
    data=body,
    headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
    method="POST"
)

try:
    with urllib.request.urlopen(req) as r:
        result = json.loads(r.read().decode())
        print("SUCCESS! Food saved:")
        print(f"  id: {result['id']}")
        print(f"  name: {result['name']}")
        print(f"  imageUrl starts with: {result['imageUrl'][:30]}...")
except urllib.error.HTTPError as e:
    print(f"HTTP ERROR {e.code}:")
    print(e.read().decode())
except Exception as e:
    print(f"ERROR: {type(e).__name__}: {e}")
