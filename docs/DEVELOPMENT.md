# Development

## Build

Use JDK 21. The project includes the Gradle wrapper.

```powershell
.\gradlew.bat :neoforge:build
```

On Linux and macOS:

```sh
./gradlew :neoforge:build
```

The installable JAR is produced in `neoforge/build/libs/`. NeoForge is the supported loader.

## Resources and compatibility profiles

Node.js is required only when changing generated resources or running the resource checks.

```sh
node tools/generate-resources.mjs
node tools/validate-resources.mjs
```

Add terrain materials to `compat/profiles.json`, including their source block, material family, and texture references.
Re-run the resource generator to update models, recipes, loot tables, tags, and translations.
The `wildslabs:tillable_slabs` tag explicitly enables hoe conversion for additional compatible soil slabs.

## Tests

```powershell
.\gradlew.bat :neoforge:runGameTestServer
```

The test server runs in `neoforge/run-gametest/`. Tests cover soil and crops, flower states, seeded smoothing,
river preservation, rooted plants, and vegetation offsets.

To install optional biome mods in the isolated test instance:

```sh
node tools/download-test-mods.mjs
```

The download tool verifies SHA-512 hashes. Third-party mods are not part of this source distribution.

For the complete compatibility snapshot, use `node tools/download-compat-mods.mjs`.
This downloads the pinned files in `compat/test-mods.lock.json` to `build/compat-mods`.
Use `--refresh` to resolve newer matching Modrinth builds; externally pinned artifacts retain their explicit version.

Test the full pack on a standard NeoForge 21.1.265 server with the production JAR. The development remapper
does not correctly remap a Vinery 1.5.6 inner-class shadow field, so that pack cannot be verified with the Loom development launcher.
Do not install a separate Architectury JAR when using the bundled version. Accept the Minecraft EULA before starting a local test server.

Production GameTests require `-Dneoforge.enableGameTest=true -Dneoforge.gameTestServer=true`
and `-Dneoforge.enabledGameTestNamespaces=wildslabs -Dwildslabs.integrationTests=true`.
The additional integration flag enables the test registry and ticker in production; it is disabled during normal gameplay.

A normal server launched with `-Dwildslabs.worldgenSmoke=true` samples two chunks in each registered dimension,
writes `worldgen-smoke.json`, and stops. Use a dedicated disposable world and a fixed seed for this test.

## Settings

| Setting | Default | Purpose |
| --- | --- | --- |
| `enableSlabGeneration` | `true` | Generate terrain slabs |
| `slabPlacementChance` | `0.65` | Select eligible positions for smoothing |
| `terrainSeedOffset` | `0` | Add reproducible variation to the world seed |
| `preserveWaterways` | `true` | Preserve fluids, aquatic plants, and nearby shores |
| `enableVegetationOnSlabs` | `true` | Enable vegetation support and offsets |
| `automaticModVegetationCompatibility` | `true` | Reuse source-soil rules for compatible modded plants |
| `enableSnowOnSlabs` | `true` | Enable snow support |
| `enableBetterGrassCompatibility` | `false` | Enable the bundled grassy-side resource pack |

Changing generation settings affects new chunks. A probability of `1.0` smooths all eligible positions.
The old `1.0` default is migrated once to `0.65`; existing probabilities below 1 are preserved.
Restart the client after changing the Better Grass setting.

## License

WildSlabs-specific material is All Rights Reserved. Inherited Terrain Slabs code retains its MIT license.
Upstream contributors are credited in `CREDITS.md`, and their license is in `LICENSES/Countered-Terrain-Slabs-MIT.txt`.
The Gradle wrapper is distributed under the notices in `LICENSES/` and its embedded license.
