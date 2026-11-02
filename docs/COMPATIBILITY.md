# Compatibility

WildSlabs 0.1.0-alpha.9 targets Minecraft 1.21.1, NeoForge 21.1.265 or newer in the 21.1 branch, and Java 21. All integrations are optional.

## Supported behavior

Generation geometry uses the same conservative checks for every mod: both the outline and collision must fill a cube,
the block must be a mapped terrain material, and it must have no fluid or block entity. A plant, rock, stick, mushroom,
wall, fence, or unrelated partial block cannot act as a raised terrain step. Unknown obstacles are left on their full supports.
Movable foliage must depend on the ground below; wall-mounted decorations keep their original attachments.

The default probability is 80 percent of eligible positions. Neighboring collision-free decorations are treated as open
space for slope calculations, while their occupied cells remain protected. Fixed decorations block a slope only when
their native survival depends on the block being changed. Logs, planks, wooden stairs/slabs, fences, gates, doors,
and trapdoors are never terrain materials and prevent slab generation directly beside them.

Farmland, dirt paths, soul sand, and mud are intentional exceptions to the full-cube requirement. Their native classes,
soil/soul-fire-base tags, and the `wildslabs:terrain_shape_exceptions` block tag identify compatible variants.
Exceptions must still have solid rectangular shapes covering the full horizontal footprint, at least 14/16 block high,
with no fluids or block entities. Registered material mappings are required; the exception tag does not create new slab models.
Generated farmland retains moisture, and cultivated slabs use the native underlying dirt material.

Registered structure bounding boxes are excluded automatically, with a two-block safety margin and cross-chunk references.
Each feature pass writes only inside its own chunk, preventing neighbor passes from bypassing those exclusions.
This protects the full Hexa ancient golem arena even where its floor uses vanilla grass, dirt, moss, or stone.
A data pack can opt individual natural structures back in through the `wildslabs:allow_terrain_smoothing` structure tag.
Steep surface height changes and exposed drops are preserved to retain cliff bands and recesses.
Material profiles still define which full blocks have slab models; these safety checks do not require a profile for each plant.

- 141 explicit terrain material profiles, including native soil, grass, rock, sand, farmland, and path variants.
- Source-soil checks for eligible modded plants, preservation of flower counts and facing, and vegetation offsets on recognized soil slabs.
- Native bone meal and block interactions where the source provides them, with slab geometry retained during mapped soil conversions.
- Terrain generation runs once at the end of every registered biome except the void, including modded dimensions.
- Seeded gaps and waterway protection remain enabled by default. Aquatic vegetation and rooted columns are preserved during terrain generation.
- Existing recognized soil slabs from other mods can support vegetation without becoming additional world-generation targets.
- Optional slabs are registered only when their required mod is loaded, so absent integrations have no block/item IDs, command entries, or creative items.
- All WildSlabs terrain slabs use their native source block's drops, whether generated or player-placed. Tool enchantments and native farmland/path soil drops are retained. A double slab uses one full block's native drops.
- Standing torches, soul torches, redstone torches, and lanterns support slab surfaces, including vanilla slabs. Hanging lanterns follow upper slab undersides. Models, selection, collision, and vanilla torch particles follow the surface height.

Terralith, Tectonic, and Lithostitched integration uses their generated terrain and biome settings; they do not need duplicate terrain blocks. Twilight Forest, Ecologics, and several Let's Do modules likewise use existing materials and plant compatibility rather than receiving a slab for every decorative block.

## Version snapshot

Hexa Biomes 0.1.0 is supported using its local development build. Its `hexabiomes:wet_sand` slab retains the source's
non-falling behavior, hardness, native drops, and tint on all six faces. Dry sand slabs touching water become wet sand
slabs while retaining orientation, waterlogging, and generation state. Seashells, pebbles, starfish, washed-up kelp,
mossy pebbles, and mussels can follow suitable terrain slabs; marram grass retains its native sand restriction and both halves.
The custom Hexa terrain generator uses the existing final biome decoration integration.

These are the latest matching NeoForge 1.21.1 builds resolved for this compatibility pass. Exact download URLs, dependencies, and SHA-512 hashes are recorded in [the test lock](../compat/test-mods.lock.json). Later upstream updates require another verification pass.

| Mod | Version |
| --- | --- |
| The Aether | 1.21.1-1.5.10-neoforge |
| Biomes O' Plenty | 21.1.0.14 |
| Ecologics | 2.3.8-NeoForge |
| [Let's Do] Alpine Whispers | 1.0.4 |
| [Let's Do] Bakery - Farm&Charm Compat | 2.1.11 |
| [Let's Do] Beachparty | 2.1.5 |
| [Let's Do] BloomingNature | 1.1.12 |
| [Let's Do] Brewery - Farm&Charm Compat | 2.1.13 |
| [Let's Do] Camping | 2.1.6 |
| [Let's Do] Candlelight - Farm&Charm compat | 2.1.15 |
| [Let's Do] Farm & Charm | 1.1.29 |
| [Let's Do] Furniture | 1.1.10 |
| [Let's Do] Hearth & Timber | 1.0.5 |
| [Let's Do] HerbalBrews | 1.1.4 |
| [Let's Do] Legacy | 1.0.3 |
| [Let's Do] Lili's Lucky Lures | 1.1.5 |
| [Let's Do] Lili's Pottery | 1.0.3 |
| [Let's Do] Meadow | 1.4.10 |
| [Let's Do] Smoker | 1.0.3 |
| [Let's Do] Vinery | 1.5.6 |
| [Let's Do] WilderNature | 1.1.8 |
| Lithostitched | 1.8.1-neoforge-21.1 |
| Nature's Spirit | 2.3.2-1.21.1 |
| Oh The Biomes We've Gone | 2.6.3-NeoForge |
| Promenade | 6.0.1+neoforge |
| Regions Unexplored | 0.6.3-neoforge-21.1 |
| Spawn | 4.0.8 |
| Tectonic | 3.0.28-neoforge-21.1 |
| Terralith | 2.6.2 |
| Vanilla Backport | 1.2.2 |
| The Twilight Forest | 4.8.3345 |

Old Let's Do modules without a matching NeoForge 1.21.1 release, including NetherVinery, DoAPI, and LevelZ compatibility, are outside this snapshot.

## Deliberately preserved blocks

Special full blocks retain their native behavior instead of being replaced by ordinary slabs. These include smoking ash soil, layered sand and quicksand, Aether clouds and freezing ice, Twilight Forest uberous soil, and fertilized Farm & Charm soil. The optional list is in [preserved-blocks.json](../compat/preserved-blocks.json). Compatible farmland and paths are generation and interaction targets.

## Verification

Alpha.9 passed all 39 required production tests with the compatibility pack, Hexa Biomes, and Sable.
All 39 tests also passed without optional mods installed.
With the full compatibility pack, the controlled slope produced seven sampled slabs at 65 percent and nine at 80 percent.
Without optional mods, it produced eight at 65 percent and nine at 80 percent.
In both configurations, the same nine remained with adjacent unknown ground decorations, while neighboring wood prevented placement.
Migration checks preserve custom densities and allow later manual changes after the one-time default upgrade.

Alpha.8 passed all 37 required production GameTests with the full compatibility pack, Hexa Biomes, and Sable.
All 37 tests also passed without optional mods installed.
The added cases exercise vanilla and inherited modded terrain classes, actual generation for supported modded farmland
and paths, native base soil, and moisture preservation. Partial-block, cliff, waterway, arena, and chunk-boundary checks remain in the suite.

Alpha.7 passed all 35 required GameTests on NeoForge 21.1.265 in three production configurations: current Hexa Biomes
with Sable 2.0.6, that combination plus the pinned compatibility pack, and WildSlabs without optional mods.
New regression cases cover full collisions with partial outlines, flat terrain decorated with rocks/plants/golem blocks,
layered cliffs and recesses, narrow flowing rivers, ocean beds, aquatic plants, referenced arena footprints away from
the center chunk, and chunk-boundary writes. Positive controls verify that gentle terrain still receives slabs.
The client also completed startup and resource loading with current Hexa Biomes and Sable.

A normal Hexa world using seed `9247205` completed generation in all three dimensions. Two sampled Overworld chunks
using `fr.hexa.biomes.HexaChunkGenerator` contained 11 terrain slabs and 300 plant blocks across Hexa plains,
tall grass plains, and copper plains. These samples establish startup and terrain integration rather than exhaustive biome coverage.

The alpha.5 production pack passed all 27 required GameTests on NeoForge 21.1.265. Tests exercised all 140 active profiles,
the final terrain feature in 488 biomes, 14 native hoe conversions, 27 native bone meal handlers, native vegetation
states from the installed plant mods, flower quantities, waterway protection, and geometry offsets.
Alpha.5 adds regression checks for absent optional registries and commands, native block drops, actual light item placement,
neighbor updates, and hanging lantern targeting. All 27 tests also passed without optional mods installed;
the absent-mod registry and command checks cover all 140 optional profiles in that configuration.
The development client reached the title screen and completed resource loading with the lighting mixins enabled.

Alpha.6 passed all 29 required tests with Hexa Biomes 0.1.0, including wet sand conversion, native drops,
non-falling wet sand, beach decoration offsets, and two-block marram grass. Its development client completed resource loading.
All 29 tests also passed with Hexa Biomes and the complete compatibility snapshot together (141 active profiles),
and with all optional mods absent (141 profiles excluded from registries and commands).
With seed `9247205`, a normal world using `fr.hexa.biomes.HexaChunkGenerator` generated 18 terrain slabs in two sampled
Overworld chunks across `hexabiomes:plains` and `hexabiomes:tall_grass_plains`. Nether and End smoke checks also completed.

A normal dedicated server running alpha.4 also generated and inspected two chunks per dimension with seed `9247205`.

| Dimension | Generated terrain slabs in sampled chunks |
| --- | ---: |
| Overworld | 15 |
| Aether | 138 |
| Nether | 182 |
| End | 20 |
| Twilight Forest | 135 |

The Overworld samples were ocean biomes, so waterway protection left the underwater terrain intact.
These are generation smoke checks, not exhaustive exploration of every biome or a client rendering assessment.

## Limits

Generation corrections apply to newly generated chunks. They do not automatically remove slabs placed by an older build.

This is an alpha compatibility implementation. Server tests cannot establish correct rendering with every shader, resource pack, or custom plant renderer. The built-in Better Grass pack supplies grassy slab sides; unrelated Better Grass implementations may need their own rules. Unusual plant generators, native entity abilities, block events, and special mechanics can still need dedicated integration.

Spawn 4.0.8 emits an upstream archaeology loot warning about a missing spawn:roly_poly item in the test pack. It does not prevent server startup.

Changes to terrain generation apply to new chunks. Keep a world backup when changing a modpack.
