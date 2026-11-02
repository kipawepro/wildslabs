# Changelog

## 0.1.0-alpha.8

- Allow native and modded farmland, paths, soul sand, and mud as nearly full terrain exceptions.
- Enable generation mappings for compatible cultivated soils.
- Preserve farmland moisture and native base dirt during generation.
- Add the extensible terrain_shape_exceptions block tag while retaining structure and decoration protection.

## 0.1.0-alpha.7

- Require full outline and collision cubes when identifying terrain steps across mods.
- Preserve fixed decorations and determine movable foliage through its native ground support.
- Exclude registered structure footprints, including the full Hexa golem arena across chunk boundaries.
- Preserve steep cliff bands, waterways, ocean beds, and aquatic vegetation.
- Extend shifted-shape targeting after native ray tracing for compatibility with Sable.

## 0.1.0-alpha.6

- Add optional Hexa Biomes wet sand slabs with native tint, non-falling behavior, and drops.
- Keep slab geometry when Hexa Biomes wets sand beside water.
- Support Hexa beach decorations and marram grass on suitable terrain slabs.
- Verify generation with the Hexa terrain generator.

## 0.1.0-alpha.5

- Use native full-block drops for mined terrain slabs.
- Support torches and lanterns on slabs, including hanging lanterns under upper slabs.
- Verify absent optional slabs are excluded from registries and commands.

## 0.1.0-alpha.4

- Extend optional terrain profiles and native soil interactions across biome mods.
- Apply final terrain decoration across registered biomes and dimensions.

## 0.1.0-alpha.3

- Preserve rivers, flowing water, riverbeds, and aquatic vegetation during smoothing.
- Keep bamboo, sugar cane, and cactus on their original full-block supports.
- Apply plant offsets only on compatible terrain soils.
- Align seagrass models and selection shapes with submerged terrain slabs.
- Use a 65 percent default placement probability and migrate the former full-density default.

## 0.1.0-alpha.2

- Preserve biome vegetation, tall plant halves, flower-patch amounts, and orientations.
- Add source-soil checks for compatible modded plants.
- Add reproducible seed offsets and placement probability settings.

## 0.1.0-alpha.1

- Introduce the standalone WildSlabs mod for Minecraft 1.21.1 and NeoForge.
- Add hydrated farmland slabs, crop support, grass spreading, and bone meal interactions.
- Add optional Biomes O' Plenty terrain profiles and a Better Grass resource pack.
