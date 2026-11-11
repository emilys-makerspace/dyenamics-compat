# Development

Set `JAVA_HOME` to a JDK 21 installation, then:

```sh
./gradlew setupDevMods
./gradlew build
./gradlew runClient
```

The mod is registered in `neoForge.mods`, and the default development runtime includes both target mods plus Create's bundled Registrate, Ponder, and Flywheel libraries. `setupDevMods` downloads and checksum-verifies the pack's exact jars, extracting those libraries for development only. No optional mod jars are packaged in the release.

Test independent installations with isolated directories:

```sh
./gradlew runClient -PcompatProfile=both
./gradlew runClient -PcompatProfile=create
./gradlew runClient -PcompatProfile=dyenamics
./gradlew runClient -PcompatProfile=none
```

`runServer` supports the same profiles. `./gradlew verifyNative` checks toolbox component copying, all 90 generated textures, and entry-point linkage with compatibility APIs hidden. `python3 tools/validate_resources.py` validates conditional data and model/texture references against the exact local upstream jars. `python3 tools/generate_resources.py` regenerates the checked-in JSON.

The pinned Dyenamics jar declares Minecraft `[1.21,1.21.1)`, excluding its own target version. For development runs containing Dyenamics, `prepareDevRuntime` creates a fresh `config/fml.toml` with `dependencyOverrides.dyenamics = ["-minecraft"]`. This override is confined to development directories; it neither patches the upstream jar nor ships in the release. Existing development configs are left intact. The same upstream metadata issue affects installations using this exact Dyenamics jar without an appropriate override or upstream fix.

## Verification status

All production Java sources have compiled directly against the mapped NeoForge 21.1.252 and pinned Create APIs. Standalone checks passed using actual Minecraft classes: component copying, all generated palette textures, and entry-point linkage with optional APIs hidden. Resource validation results are in `build/verification/resources.json` when the validator runs.

Verified on 2026-11-11: `./gradlew tasks --all` exposes `runClient`, and `./gradlew build runClient` builds successfully, passes the native checks, and launches the client with Create, Dyenamics, and Dyenamics Compat loaded. The client completes resource/model/texture loading without mod-loading errors. The game is left running for interactive testing. World loading/uninstallation, all four full runtime profiles, and in-game interactions still require verification before publishing.
