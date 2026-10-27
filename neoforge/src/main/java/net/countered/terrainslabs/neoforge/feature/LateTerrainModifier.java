package net.countered.terrainslabs.neoforge.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.countered.terrainslabs.TerrainSlabs;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public record LateTerrainModifier(Holder<PlacedFeature> feature) implements BiomeModifier {
    public static final MapCodec<LateTerrainModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(PlacedFeature.CODEC.fieldOf("feature").forGetter(LateTerrainModifier::feature)).apply(instance, LateTerrainModifier::new));
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, TerrainSlabs.MOD_ID);
    static { SERIALIZERS.register("late_terrain", () -> CODEC); }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.AFTER_EVERYTHING || biome.unwrapKey().map(key -> key.location().toString().equals("minecraft:the_void")).orElse(false)) return;
        var stage = GenerationStep.Decoration.TOP_LAYER_MODIFICATION;
        var features = builder.getGenerationSettings().getFeatures(stage);
        if (!features.contains(feature)) builder.getGenerationSettings().addFeature(stage, feature);
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() { return CODEC; }
}
