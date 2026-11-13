# Adam Client – source audit & provenance

Adam Client is a renamed build of the "Donut Client 3.0" Fabric mod source (Minecraft 1.21.11).
Original MIT notice (`LICENSE`) and the bundled third-party notices (`src/main/resources/licenses/`)
are kept unchanged. Parts of the code are GPL-3.0-only (Spectre ports) – if you distribute a jar,
you must also provide the source under the GPL terms.

## What was checked (static review of all 200+ files)
- No HTTP/socket/WebSocket code, no URLs in source, no Discord webhooks.
- No `Runtime.exec` / `ProcessBuilder`, no native library loading, no custom class loading,
  no `Unsafe`, no script engines, no Base64/encoded payloads.
- No reading of session tokens, access tokens, account credentials, env vars or user-home files.
- File access is limited to: the mod config (`config/adam-client.json`), schematics folder,
  sound-detector exports, build-plan JSON, and a temp scratch dir for BlockESP.
- Only outbound packet sent by the mod: a command-suggestion request for `/msg ` (Staff Detector option).
- Gradle: only the official Fabric Maven, Maven Central and Gradle plugin portal; wrapper points to
  services.gradle.org; `gradle-wrapper.jar` contains only standard Gradle wrapper classes.

## Things worth knowing (not malware)
- BlockESP builds a hidden in-process vanilla world generator. It constructs Mojang's
  `YggdrasilAuthenticationService(Proxy.NO_PROXY)` only because `MinecraftServer`'s constructor requires it.
- Several modules (Fly, AutoBuilder, BlockESP, Freecam, etc.) are cheats and may get you banned on servers
  that prohibit them. Use at your own risk.
- Couldn't verify `gradle-wrapper.jar` against an official checksum offline. To be extra safe, delete it and run
  `gradle wrapper --gradle-version 9.2.1` (or compare its SHA-256 to the one on gradle.org).

Nothing suspicious was found, so nothing had to be removed.
