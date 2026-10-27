# WildSlabs

**This is the official repository for WildSlabs, an independent Minecraft mod. WildSlabs does not require Countered's Terrain Slabs.**

WildSlabs brings more natural terrain transitions, plantable terrain slabs, and biome-mod compatibility to **Minecraft 1.21.1 on NeoForge**.

## Features

- Natural terrain slabs across surface landscapes and caves
- Partial, seed-based smoothing that leaves gaps and keeps the terrain varied
- Protection for rivers, shorelines, riverbeds, and aquatic vegetation
- Flowers, grass, and tall plants on compatible terrain slabs
- Preservation of flower-patch quantities, orientations, and plant states
- Tillable soil slabs with moisture, drying, and support for compatible crops
- Grass spreading, bone meal, and shovel-created paths
- Optional Biomes O' Plenty integration with 13 terrain materials
- Automatic compatibility with modded plants using standard placement rules
- A built-in Better Grass resource pack for grassy slab sides
- Configurable generation density, seed offset, vegetation, snow, and rendering

## Screenshot

**Screenshot coming soon.**

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.215 or newer within the 21.1 branch
- Java 21
- WildSlabs installed on both client and server

Architectury and MidnightLib are bundled. Biomes O' Plenty is optional and requires its own dependencies.

## Installation

Place the WildSlabs JAR in your `mods` folder. When updating, replace the previous WildSlabs JAR.

Generation changes apply to newly generated chunks. Existing terrain is not regenerated automatically.
WildSlabs is currently in alpha; custom plant generators and unusual rendering systems may need dedicated integration.

## Configuration

Settings are stored in `config/wildslabs.json`. Terrain smoothing uses the world seed and a configurable seed offset.
The default placement probability is `0.65`, and waterway protection is enabled by default.

See [Development](docs/DEVELOPMENT.md) for building, testing, and adding compatibility profiles.

## License

WildSlabs-specific additions, documentation, and branding are **All Rights Reserved** by HexaMC.
Unmodified official builds may be used for Minecraft gameplay. Redistribution, including in modpacks, requires permission.
Inherited Countered's Terrain Slabs material retains its original **MIT license** and permissions.
See [LICENSE](LICENSE) and [third-party notices](LICENSES/Countered-Terrain-Slabs-MIT.txt).

## Credits

WildSlabs builds on the terrain engine, slab models, and vegetation-offset work from **Countered's Terrain Slabs**, by Countered and its contributors.

- [Countered's Terrain Slabs source](https://github.com/Coun7ered/terrain_slabs_multiloader)
- [Countered's Terrain Slabs](https://www.curseforge.com/minecraft/mc-mods/countereds-terrain-slabs)
- [Biomes O' Plenty by Glitchfiend](https://github.com/Glitchfiend/BiomesOPlenty)
- [Architectury API](https://github.com/architectury/architectury-api)
- [MidnightLib](https://github.com/TeamMidnightDust/MidnightLib)

WildSlabs is an independent project and is not affiliated with or endorsed by Countered or Glitchfiend.
Original work remains credited to its creators and contributors. See [CREDITS.md](CREDITS.md) and [LICENSE](LICENSE).
