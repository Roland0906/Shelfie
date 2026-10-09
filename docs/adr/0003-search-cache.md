# 0003. Search cache

Accepted · 2026-10-05 · amended 2026-10-09

Queries are normalized (trimmed, whitespace collapsed, lowercased) into one cache key, and results are stored per query. A search is reused for 6 hours, counted from its first page, and only the 30 most recent queries are kept. A unique `(query, workId)` index drops books that a later page repeats. The trade-off is that offset paging can skip a book if the ranking changes between pages.
