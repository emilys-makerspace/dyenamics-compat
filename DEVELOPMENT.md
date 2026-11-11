# Development

Use JDK 21. `./gradlew build` downloads and verifies the exact development jars from the [tismcraft pack](https://github.com/emilybelnavis/mc-modpacks/tree/main/tismcraft), then builds the mod and runs standalone checks. The mod and sources jars contain the MIT license and retained third-party notices; development jars and test mods are excluded.

`./gradlew runClient` runs Create, Dyenamics, and all sixteen reviewed Let's Do mods. The project mod is registered in the development run. Create's libraries, Architectury, Curios, Foundation, and Biolith are included on the development runtime classpath. Pinned Let's Do download URLs and checksums are in `config/letsdo-dev-mods.json`.

## Runtime profiles

Use `-PcompatProfile=<profile>` with `runClient` or `runServer`:

| Profile | Runtime content |
| --- | --- |
| `all` (default) | Create, Dyenamics, and the Let's Do collection |
| `letsdo` | Dyenamics and the Let's Do collection, without Create |
| `letsdo-only` | The Let's Do collection without Dyenamics or Create |
| `both` | Create and Dyenamics |
| `create` | Create without Dyenamics |
| `dyenamics` | Dyenamics without compatibility mods |
| `none` | No optional mods |

Filter the collection with `-PletsdoMods=camping,furniture` (comma-separated mod IDs). Required upstream Farm & Charm is added automatically for Brewery, Bakery, and Candlelight. Runtime directories are isolated by profile under `run/`.

The pinned Dyenamics 3.5.0 jar incorrectly declares Minecraft `[1.21,1.21.1)`. For profiles containing it, `prepareDevRuntime` creates a fresh `config/fml.toml` with `dependencyOverrides.dyenamics = ["-minecraft"]`. This development-only override leaves upstream jars and existing configs untouched. It is not shipped in the release. The same upstream metadata issue affects installations using this exact jar without an appropriate override or upstream fix.

## Verification

```sh
./gradlew build
python3 tools/validate_resources.py
./gradlew help --task runClient
./gradlew runServer -PregistryProbe
./gradlew runServer -PregistryProbe -PcompatProfile=none
./gradlew runServer -PregistryProbe -PcompatProfile=letsdo-only
./gradlew runServer -PregistryProbe -PcompatProfile=letsdo -PletsdoMods=camping,furniture
./gradlew runGameTestServer -PworldTests -PcompatProfile=letsdo -PletsdoMods=brewery,camping,furniture,hearth_and_timber,lilis_pottery
```

`registryProbe` enables a release-excluded test mod. It checks registry gates, every registered Let's Do block state, entity save/load, generated textures/models, and recipe decoding, writes a JSON report under `build/verification/`, then exits before starting a server or world. `worldTests` enables the same test mod plus eleven GameTests in a disposable Minecraft test world. Neither flag affects the published jar.

`verifyNative` covers component copying, palette texture generation, and entry-point loading with optional APIs hidden. `tools/validate_resources.py` checks the static Create resources; Let's Do resources are generated at runtime and checked by `registryProbe`. `tools/generate_resources.py` regenerates the static Create JSON.

See [LETSDO.md](LETSDO.md) for audited coverage and known upstream limitations. Further visual review and complete uninstall/reinstall testing remain useful before publishing.
