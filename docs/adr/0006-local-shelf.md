# 0006. Local shelf

Accepted · 2026-10-05

The shelf lives only on the device, because writing to Open Library needs a login. Entries copy the title, authors and cover when added and have no foreign key to the book cache, so clearing the cache never touches the shelf. The rules tying status to progress live in `ShelfEntry`. The cost is that copied details can go stale and the shelf does not move between devices.
