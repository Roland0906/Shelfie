# 0008. Server in the same repository

Proposed · 2026-10-10

The sync server from [0007](0007-shelf-sync.md) is a `:server` module in this repository rather than a separate project. Both sides use `:core:model` directly, so the `ShelfEntry` rules and the sync DTOs are written once, and a change that breaks either side fails the same pull request. A separate repository would have to publish `:core:model` as an artifact and keep the two versions in step.

Sharing a repository does not mean releasing together: older app versions stay installed after the server changes, so the API still has to stay backward compatible. Configuring the build needs the Android SDK even for server-only tasks, so CI builds the server into a fat JAR and the Docker image only copies it in. The server moves to its own repository if another client or team needs it.
