# Dyenamics Compat

Extra Dyenamics colors for other Minecraft mods, with no hard mod dependencies or color mixins. Content is registered only when its compatible mods are installed.

**Minecraft 1.21.1 · NeoForge 21.1.252 · Java 21**

Adds 18 extra colors to **Create** and existing dyeable families in **[Let’s Do] Brewery, Camping, Furniture, Hearth & Timber, and Lili’s Pottery**. Find variants in each mod’s creative tab, craft them, or recolor placed blocks with dyes. See [Let’s Do coverage](LETSDO.md) for the full sixteen-mod review.

See the [roadmap](ROADMAP.md) for planned compatibility.

## Development

With JDK 21:

```sh
./gradlew build
./gradlew runClient
```

Development dependencies are downloaded automatically. See [development notes](DEVELOPMENT.md) for runtime profiles, checks, and the pinned Dyenamics version’s metadata workaround.

## License

[MIT](LICENSE). Third-party notices are retained in the mod’s `META-INF` directory.
