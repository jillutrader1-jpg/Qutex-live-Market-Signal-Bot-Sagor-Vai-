FROM python:3.11-alpine

WORKDIR /app

# Copy server script and public directory containing APK & web landing page
COPY server.py .
COPY public ./public

# Default port for Render (Render passes $PORT dynamically)
ENV PORT=10000
EXPOSE 10000

# Start server
CMD ["python", "server.py"]
