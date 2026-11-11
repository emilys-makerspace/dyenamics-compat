# Let's Do compatibility

Scope: extend existing dyeable families, plus Furniture’s existing Compat Layer textiles as subsequently requested. All sixteen mods from the roadmap were inspected at the versions pinned in tismcraft. Natural flower colors, food varieties, and wood species are not treated as dyeable families; Meadow and Alpinewhispers fabrics are recolored through Furniture rather than adding standalone textile families.

| Addon | Pack version | Coverage |
| --- | --- | --- |
| Alpinewhispers | 1.0.3 | Reviewed: no existing dyeable families; homespun textiles and the bed have fixed designs. |
| Bakery | 2.1.8 | Reviewed: no existing dyeable families. |
| Beachparty | 2.1.5 | Reviewed: towel/lounger constructors use vanilla color arguments, but the mod exposes fixed designs rather than a dyeable palette. |
| BloomingNature | 1.1.12 | Reviewed: flower and foliage varieties are natural variants, not dyeable blocks. |
| Brewery | 2.1.12 | Patterned wool, patterned carpet, and tablecloths. |
| Camping | 2.1.6 | Sleeping bags and tents. |
| Candlelight | 2.1.14 | Reviewed: fixed furniture designs and material variants; no dyeable palette. |
| Farm & Charm | 1.1.27 | Reviewed: no existing dyeable families. |
| Furniture | 1.1.7 | Curtains, sofas, pouffes, standing lamps, and wall lamps; patterned Meadow/Alpinewhispers curtains, pouffes, and lamps in the Compat Layer. |
| Hearth & Timber | 1.0.4 | Plaster. |
| HerbalBrews | 1.1.4 | Reviewed: teas and plants are ingredient varieties, not dyeable blocks. |
| Lili's Pottery | 1.0.3 | Clay, bricks, chiseled bricks, brick stairs/slabs/walls, planters, plant bowls, cupellas, bud vases, vases, garden pots, square pots, urns, amphores; clay balls and bricks as items. |
| Lili's Lucky Lures | 1.1.5 | Reviewed: no existing dyeable families. |
| Meadow | 1.4.9 | Reviewed: fixed textile patterns, beds, and sofas; no dyeable palette. |
| Vinery | 1.5.5 | Reviewed: grapes, wine, and wood species are distinct materials, not dyeable families. |
| Wilder Nature | 1.1.6 | Reviewed: no existing dyeable families. |

The implementations add **1,044 blocks and 918 items** across the 18 extra Dyenamics colors. Standing and wall lamps share a placement item. Java packages and registry paths follow `letsdo/<addon>`; for example, `dyenamicscompat:letsdo/camping/mint_tent`. Each module loads only when both Dyenamics and that addon are present. Patterned Furniture variants also require their Meadow or Alpinewhispers source content; disabling that upstream compatibility category omits its variants. Neither Create nor the other Let's Do addons are required by an individual integration.

Variants appear next to their original families in the addon's existing creative tab, including Camping, Hearth & Timber, and Lili's Pottery. Furniture's base curtains, lamps, pouffes, and sofas are included in its Furniture side tab; the Compat Layer also includes recolored rustic, linen, jacquard, plaid, chambray, tweed, warped, and homespun curtains/pouffes/lamps when their owner mods and upstream compatibility options are enabled. The pinned Furniture version has no patterned sofas in its Compat Layer. White dye restores a recolored patterned item to its original fabric design. Craft a source variant with a Dyenamics dye, or recolor it in the world. Recoloring consumes one dye outside creative mode and preserves properties and block-entity contents. Vanilla dyes return variants to the addon's available native colors. A colored Brewery tablecloth requires fabric: attach a colored patterned carpet to a bare table, or craft a table with a patterned carpet and dye. Removing the cloth returns its actual extra-color carpet.

Colored material, pottery-table, and shaped recipes are derived from the installed addon's recipes. Pottery contents, paint, and glaze survive recoloring. The native kiln's pigment-mixing operation still recognizes its original vanilla dyes; use crafting or placed-block dyeing to select a Dyenamics color. Uninstalling an integration removes its registered variants normally; there are no replacement blocks or registry remaps.

Models, textures, loot tables, tags, and material recipes are derived in memory from the installed mods. Upstream assets and mod jars are not bundled in the release. Resource packs can override resources in the `dyenamicscompat` namespace. The generated palette uses the original installed textures rather than resource-pack edits to upstream textures.

## Validation

- Actual loader: all 1,044 blocks, 55,278 block states, and 1,368 block-entity serialization round trips passed.
- Generated resources: 10,117 client entries (including 3,186 textures) and 3,006 data entries passed structural checks; every generated recipe decoded with Minecraft's recipe codecs.
- Eleven Minecraft GameTests passed: tent multipart recoloring and stored bag retention, both sleeping-bag halves, pottery inventory/paint/glaze retention without duplicate drops, tablecloth removal, actual recipe component preservation, curtain redstone and lamp item behavior, native slab/clay drop counts, actual upstream creative-tab contents, native kiln processing, the actual Foundation side-tab filters, and contiguous grouping beside native counterparts.
- Full collection startup, partial-addon startup without Create, and optional-dependency absence checks are documented in [development notes](DEVELOPMENT.md). These checks do not exhaustively test every visual state or every upstream workstation feature.

The full collection's own data includes some invalid Hearth & Timber recipes (such as `swamp_cyrpress_slab` and the wrong namespace for arolla pine planks). Those upstream warnings do not originate in the generated compatibility data. BloomingNature also needs Biolith at runtime despite not declaring it in its mod metadata; the development profile includes the pack's pinned Biolith jar.

The updated development client launched with the full pinned mod collection, completed model/texture loading, and logged no errors or missing compatibility textures. Detailed visual review remains useful before publishing.
