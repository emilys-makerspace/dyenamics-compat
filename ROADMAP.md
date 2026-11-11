# Compatibility roadmap

Planned integrations recorded on 2026-11-11. Unchecked entries are planned, not implemented; their order does not establish priority. Links below were supplied with the plan. Target versions should come from the [tismcraft modpack](https://github.com/emilybelnavis/mc-modpacks/tree/main/tismcraft).

## Current integration

- Create: implemented for 6.0.10, with seats, sails, valve handles, and toolboxes. Build and client startup verified; further in-game and uninstall testing remains.

## Planned integrations

- [ ] [Ceramics](https://www.curseforge.com/minecraft/mc-mods/ceramics)
- [ ] [Chalk](https://modrinth.com/mod/chalk-mod)
- [ ] [Clayworks](https://www.curseforge.com/minecraft/mc-mods/clayworks)
- [ ] [MrCrayfish's Furniture Mod: Refurbished](https://www.curseforge.com/minecraft/mc-mods/refurbished-furniture)
- [ ] [Applied Energistics 2](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2)
- [ ] [Botany Pots](https://www.curseforge.com/minecraft/mc-mods/botany-pots)
- [ ] [Just Blahaj](https://modrinth.com/mod/just-blahaj)
- [ ] [Create More Seats](https://www.curseforge.com/minecraft/mc-mods/create-more-seats)
- [ ] [Create: Interiors](https://www.curseforge.com/minecraft/mc-mods/interiors)
- [ ] [Domum Ornamentum](https://www.curseforge.com/minecraft/mc-mods/domum-ornamentum)
- [ ] [Sophisticated Backpacks](https://modrinth.com/mod/sophisticated-backpacks)
- [ ] [Mekanism Additions](https://modrinth.com/mod/mekanism-additions)
- [ ] [Furnish](https://www.curseforge.com/minecraft/mc-mods/furnish)
- [ ] [Farmer's Delight](https://modrinth.com/mod/farmers-delight)
- [ ] [Quark](https://modrinth.com/mod/quark)
- [ ] [Supplementaries](https://modrinth.com/mod/supplementaries)
- [ ] [Supplementaries Squared](https://modrinth.com/mod/supplementaries-squared)
- [ ] [Comforts](https://modrinth.com/mod/comforts)

### [Let's Do] series

- [ ] Alpinewhispers
- [ ] Bakery
- [ ] Beachparty
- [ ] BloomingNature
- [ ] Brewery
- [ ] Camping
- [ ] Candlelight
- [ ] Farm & Charm
- [ ] Furniture
- [ ] Hearth & Timber
- [ ] HerbalBrews
- [ ] Lili's Pottery
- [ ] Lili's Lucky Lures
- [ ] Meadow
- [ ] Vinery
- [ ] Wilder Nature

### Macaw's series

- [ ] Biomes O' Plenty
- [ ] Furniture
- [ ] Mystic's Biomes
- [ ] Regiones Unexplored

## Requirements for every integration

- Keep compatibility mods optional, with no hard mod dependencies or color mixins.
- Register content only when its required compatibility mods are installed. Removing Dyenamics Compat or a compatibility mod should use normal missing-content uninstall behavior.
- Put each integration directly under `ca.emilybelnavis.mcmods.dyenamicscompat` (for example, `.create`), without an intermediate `compat` package. Java package names cannot contain hyphens.
- Add variants to the compatible mod's existing creative tab instead of creating a separate compatibility tab.
- Maintain a working Gradle `runClient` task, register this project mod in the development run, include the integration's required development runtime dependencies, and verify task availability before delivery.

For each planned mod, confirm the pack's version, relevant colored content, and available extension APIs before implementing its integration.
