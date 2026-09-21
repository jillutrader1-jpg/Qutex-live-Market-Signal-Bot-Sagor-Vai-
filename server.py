import os
import http.server
import socketserver
import sys

PORT = int(os.environ.get("PORT", 10000))
WEB_DIR = os.path.join(os.path.dirname(__file__), "public")

class CustomHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=WEB_DIR, **kwargs)

    def end_headers(self):
        # Enable CORS and proper caching
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Cache-Control", "no-cache")
        super().end_headers()

if __name__ == "__main__":
    # Ensure web dir exists
    if not os.path.exists(WEB_DIR):
        os.makedirs(WEB_DIR, exist_ok=True)

    print(f"Starting Trader Vai AI Bot Web & APK Server on port {PORT}...")
    with socketserver.TCPServer(("", PORT), CustomHandler) as httpd:
        print(f"Serving files from {WEB_DIR}")
        print(f"Listening on http://0.0.0.0:{PORT}")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("Server shutting down.")
            sys.exit(0)
