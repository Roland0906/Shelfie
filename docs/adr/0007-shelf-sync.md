# 0007. Shelf sync

Proposed · 2026-10-10

A Ktor server keeps a copy of the shelf so it follows the user across devices, which removes the main cost of [0006](0006-local-shelf.md). Room stays the source of truth: edits are saved locally, marked as pending and pushed in the background, so the shelf still works offline. Users sign in with Google; the server verifies the ID token and issues its own JWT.

Each entry has a version assigned by the server. A push names the version it started from, and the server rejects it as a conflict if the entry has moved on since. The app then merges the two copies: the higher progress wins, status follows it through the `ShelfEntry` rules, and the note comes from the copy edited last. Deletions are kept as tombstones so they reach other devices, and pulls ask for changes since a cursor. The server checks incoming entries with the same `ShelfEntry` rules, since both sides use `:core:model`, and refreshes title, authors and cover from Open Library on a schedule so copied details stop going stale.

Last write wins was the simpler alternative, but it trusts device clocks for every field and can throw away reading progress. The cost of merging is that lowering progress on one device loses to a higher value from another, and the note still depends on clocks. Sync also adds a login and a server to run.
