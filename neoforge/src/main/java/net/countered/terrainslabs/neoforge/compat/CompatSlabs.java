package net.countered.terrainslabs.neoforge.compat;

import com.google.gson.Gson;
import net.countered.terrainslabs.TerrainSlabs;
import net.countered.terrainslabs.block.ModSlabsMap;
import net.countered.terrainslabs.block.customslabs.soilslabs.GrassSlab;
import net.countered.terrainslabs.block.customslabs.specialslabs.CustomSlab;
import net.countered.terrainslabs.block.customslabs.specialslabs.GravityAffectedSlab;
import net.countered.terrainslabs.registries.ModBlocksRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class CompatSlabs {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, TerrainSlabs.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TerrainSlabs.MOD_ID);
    private static final List<Entry> ACTIVE = new ArrayList<>();
    private static final List<Profile> PROFILES = new ArrayList<>();
    public record Profile(String source, String kind, String modId, String base) {
        public String requiredMod() { return modId == null ? ResourceLocation.parse(source).getNamespace() : modId; }
    }
    public record Entry(ResourceLocation source, Supplier<Block> slab, Supplier<Item> item, String kind) {}
    public static List<Entry> entries() { return List.copyOf(ACTIVE); }
    public static Profile profile(ResourceLocation source) { return PROFILES.stream().filter(p -> p.source().equals(source.toString())).findFirst().orElseThrow(); }

    public static void register(IEventBus bus) {
        try (var stream = CompatSlabs.class.getResourceAsStream("/wildslabs-compat.json")) {
            if (stream == null) throw new IllegalStateException("Missing compatibility manifest");
            Profile[] profiles = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Profile[].class);
            for (Profile profile : profiles) {
                ResourceLocation source = ResourceLocation.parse(profile.source());
                if (!ModList.get().isLoaded(profile.requiredMod())) continue;
                PROFILES.add(profile);
                String id = source.getNamespace() + "/" + source.getPath() + "_slab";
                Supplier<Block> slab = BLOCKS.register(id, () -> create(profile));
                Supplier<Item> item = ITEMS.register(id, () -> new BlockItem(slab.get(), new Item.Properties()));
                ACTIVE.add(new Entry(source, slab, item, profile.kind()));
            }
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot read WildSlabs compatibility manifest", exception);
        }
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(CompatSlabs::creativeTab);
    }

    private static Block create(Profile profile) {
        var sourceId = ResourceLocation.parse(profile.source());
        Block fallback = switch (profile.kind()) { case "grass" -> Blocks.GRASS_BLOCK; case "sand" -> Blocks.SAND; case "soil" -> Blocks.DIRT; case "farmland" -> Blocks.FARMLAND; default -> Blocks.STONE; };
        Block source = BuiltInRegistries.BLOCK.containsKey(sourceId) ? BuiltInRegistries.BLOCK.get(sourceId) : fallback;
        var properties = BlockBehaviour.Properties.ofFullCopy(source);
        if (source instanceof net.minecraft.world.level.block.FallingBlock) return new GravityAffectedSlab(properties);
        return switch (profile.kind()) {
            case "grass" -> new CompatGrassSlab(properties, profile);
            case "surface" -> new CompatSurfaceSlab(properties, profile);
            case "farmland" -> new CompatFarmlandSlab(properties, profile);
            case "path" -> new CompatPathSlab(properties.noOcclusion(), profile);
            case "sand" -> new CustomSlab(properties);
            case "soil" -> source instanceof net.minecraft.world.level.block.MudBlock
                    ? new net.countered.terrainslabs.block.customslabs.specialslabs.MudSlab(properties.noOcclusion()) : new CompatDirtSlab(properties,profile);
            default -> source instanceof net.minecraft.world.level.block.MudBlock
                    ? new net.countered.terrainslabs.block.customslabs.specialslabs.MudSlab(properties.noOcclusion()) : new CustomSlab(properties);
        };
    }

    public static Block baseBlock(Profile profile) {
        var id = ResourceLocation.parse(profile.base() == null ? "minecraft:dirt" : profile.base());
        return BuiltInRegistries.BLOCK.containsKey(id) ? BuiltInRegistries.BLOCK.get(id) : Blocks.DIRT;
    }

    public static Block baseSlab(Profile profile) {
        Block slab = ModSlabsMap.getInteractionSlab(baseBlock(profile));
        return slab == null ? ModBlocksRegistry.DIRT_SLAB.get() : slab;
    }

    public static List<Profile> grassCovers(String source) {
        return PROFILES.stream().filter(p -> p.kind().equals("grass") && source.equals(p.base())).toList();
    }

    public static void setup() {
        for (Entry entry : ACTIVE) {
            if (!BuiltInRegistries.BLOCK.containsKey(entry.source())) {
                org.slf4j.LoggerFactory.getLogger("WildSlabs").warn("Missing optional source block {}; skipping worldgen mapping", entry.source());
                continue;
            }
            Block source = BuiltInRegistries.BLOCK.get(entry.source());
            if (entry.kind().equals("farmland") || entry.kind().equals("path")) ModSlabsMap.registerInteraction(source, entry.slab().get());
            else if (entry.kind().equals("grass") || entry.kind().equals("surface")) ModSlabsMap.registerSoilSlab(source, entry.slab().get());
            else ModSlabsMap.register(source, entry.slab().get());
        }
        for(var block:BuiltInRegistries.BLOCK){
            if(!(block instanceof net.minecraft.world.level.block.SlabBlock)||block instanceof CustomSlab)continue;
            var id=BuiltInRegistries.BLOCK.getKey(block);
            if(!id.getPath().endsWith("_slab"))continue;
            String material=id.getPath().substring(0,id.getPath().length()-5);
            var candidates=List.of(ResourceLocation.fromNamespaceAndPath(id.getNamespace(),material),
                    ResourceLocation.fromNamespaceAndPath(id.getNamespace(),material+"_block"),
                    ResourceLocation.withDefaultNamespace(material),ResourceLocation.withDefaultNamespace(material+"_block"));
            for(var candidate:candidates){
                if(!BuiltInRegistries.BLOCK.containsKey(candidate))continue;
                var source=BuiltInRegistries.BLOCK.get(candidate);
                if(source==Blocks.AIR||source instanceof net.minecraft.world.level.block.SlabBlock||source.defaultBlockState().hasBlockEntity())continue;
                ModSlabsMap.registerSourceAlias(block,source);
                break;
            }
        }
        HexaBiomesCompat.register();
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModBlocksRegistry.TERRAIN_SLABS_TAB.get()) {
            for (Entry entry : ACTIVE) {
                if (BuiltInRegistries.BLOCK.containsKey(entry.source())) event.accept(entry.item().get());
            }
        }
    }
}
