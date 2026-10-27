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
    public record Profile(String source, String kind) {}
    public record Entry(ResourceLocation source, Supplier<Block> slab, Supplier<Item> item, String kind) {}
    public static List<Entry> entries() { return List.copyOf(ACTIVE); }

    public static void register(IEventBus bus) {
        try (var stream = CompatSlabs.class.getResourceAsStream("/wildslabs-compat.json")) {
            if (stream == null) throw new IllegalStateException("Missing compatibility manifest");
            Profile[] profiles = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Profile[].class);
            for (Profile profile : profiles) {
                ResourceLocation source = ResourceLocation.parse(profile.source());
                if (!ModList.get().isLoaded(source.getNamespace())) continue;
                String id = source.getNamespace() + "/" + source.getPath() + "_slab";
                Supplier<Block> slab = BLOCKS.register(id, () -> create(profile.kind()));
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

    private static Block create(String kind) {
        return switch (kind) {
            case "grass" -> new GrassSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK));
            case "sand" -> new GravityAffectedSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.SAND));
            case "soil" -> new CustomSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT));
            default -> new CustomSlab(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
        };
    }

    public static void setup() {
        for (Entry entry : ACTIVE) {
            if (!BuiltInRegistries.BLOCK.containsKey(entry.source())) {
                org.slf4j.LoggerFactory.getLogger("WildSlabs").warn("Missing optional source block {}; skipping worldgen mapping", entry.source());
                continue;
            }
            Block source = BuiltInRegistries.BLOCK.get(entry.source());
            if (entry.kind().equals("grass")) ModSlabsMap.registerSoilSlab(source, entry.slab().get());
            else ModSlabsMap.register(source, entry.slab().get());
        }
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModBlocksRegistry.TERRAIN_SLABS_TAB.get()) {
            for (Entry entry : ACTIVE) {
                if (BuiltInRegistries.BLOCK.containsKey(entry.source())) event.accept(entry.item().get());
            }
        }
    }
}
