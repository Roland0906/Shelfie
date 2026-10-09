# 0005. Fakes over mocks

Accepted · 2026-10-05

Tests use hand-written fakes for the API, DAOs, repositories and clock instead of a mocking library, and assert on the resulting state rather than on calls, so they survive refactoring. The cost is maintaining the fakes, and fake DAOs do not run SQL, which still needs instrumented Room tests.
