# Jana

Cross-platform offline client for [RSDN](https://rsdn.org) forums.  
Spiritual successor to [RSDN@Home (Janus)](https://github.com/rsdn/janus), rebuilt with Kotlin Multiplatform.

## Platforms

- Windows
- macOS
- Linux

## Key features

- Offline-first: all reading works without internet
- Smart sync via RSDN REST API (`/rs/*`)
- Delta sync — only new messages fetched each session
- Outbox: compose replies offline, delivered on next sync
- Thread tree view with recursive reply nesting
- Smart notifications: mentions and watched threads
- Full-text search (SQLite FTS5)

## Tech stack

| Layer | Technology |
|---|---|
| UI | Compose Multiplatform |
| HTTP | Ktor |
| Local DB | SQLite + jOOQ |
| Migrations | Flyway |
| DI | Koin |
| Async | Kotlinx Coroutines + Flow |
| Serialization | Kotlinx.serialization |

## Status

🚧 Early development

## Related

- [RSDN@Home (Janus)](https://github.com/rsdn/janus) — original Windows-only .NET client
- [RSDN REST API demo](https://rsdn.org/rs/demo)

## License

MIT