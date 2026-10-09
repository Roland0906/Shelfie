# 0004. Module rules

Accepted · 2026-10-05

Module boundaries are checked while Gradle configures the build: features depend on neither each other nor the network and database modules, and those two never depend on each other. A violation fails the build before anything compiles, with the reason in the message. Architecture tests were the alternative, but they only fail once tests run.
