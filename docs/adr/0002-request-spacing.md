# 0002. Request spacing

Accepted · 2026-10-05

Open Library allows identified clients about three requests per second. Every request sends a `User-Agent` with the repository URL, and a Ktor plugin keeps 350 ms between requests, retries included. Only 429, 5xx and I/O errors are retried. Spacing was chosen over a token bucket because evenly spread requests hit 429 less often than bursts.
