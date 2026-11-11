package ca.emilybelnavis.mcmods.dyenamicscompat.create;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicColor;

import ca.emilybelnavis.mcmods.dyenamicscompat.DyenamicsCompat;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatInteractionBehaviour;
import com.simibubi.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.simibubi.create.content.contraptions.bearing.SailBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CreateCompat {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DyenamicsCompat.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DyenamicsCompat.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DyenamicsCompat.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DyenamicsCompat.MOD_ID);

    public record Variant(DyenamicColor color, String family, DeferredBlock<? extends Block> block) {}
    public static final List<Variant> VARIANTS = new ArrayList<>();

    static {
        for (DyenamicColor color : DyenamicColor.values()) {
            add(color, "seat", () -> new SeatBlock(softProperties(), color.analogue));
            add(color, "sail", () -> SailBlock.withCanvas(softProperties().noOcclusion(), color.analogue));
            add(color, "valve_handle", () -> new CompatValveBlock(metalProperties().noOcclusion()));
            add(color, "toolbox", () -> new CompatToolboxBlock(metalProperties().noOcclusion(), color));
        }
    }

    public static final Supplier<BlockEntityType<CompatValveEntity>> VALVE_ENTITY = ENTITIES.register("create_valve_handle",
            () -> BlockEntityType.Builder.of(CompatValveEntity::new, blocks("valve_handle")).build(null));
    public static final Supplier<BlockEntityType<CompatToolboxEntity>> TOOLBOX_ENTITY = ENTITIES.register("create_toolbox",
            () -> BlockEntityType.Builder.of(CompatToolboxEntity::new, blocks("toolbox")).build(null));
    public static final Supplier<RecipeSerializer<ToolboxColoringRecipe>> TOOLBOX_COLORING = RECIPES.register("toolbox_coloring", ToolboxColoringRecipe.Serializer::new);

    private static BlockBehaviour.Properties softProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5F).sound(SoundType.WOOL);
    }

    private static BlockBehaviour.Properties metalProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.NETHERITE_BLOCK);
    }

    private static <T extends Block> void add(DyenamicColor color, String family, Supplier<T> factory) {
        String name = "create_" + color.id + "_" + family;
        DeferredBlock<T> block = BLOCKS.register(name, factory);
        ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        VARIANTS.add(new Variant(color, family, block));
    }

    public static Block[] blocks(String family) {
        return VARIANTS.stream().filter(v -> v.family().equals(family)).map(v -> v.block().get()).toArray(Block[]::new);
    }

    public static Variant variant(Block block) {
        return VARIANTS.stream().filter(v -> v.block().get() == block).findFirst().orElse(null);
    }

    public static Block block(DyenamicColor color, String family) {
        return VARIANTS.stream().filter(v -> v.color() == color && v.family().equals(family))
                .findFirst().orElseThrow().block().get();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(DyenamicsCompat.MOD_ID, path);
    }

    public static void register(IEventBus bus) {
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            ca.emilybelnavis.mcmods.dyenamicscompat.create.client.CreateCompatClient.register(bus);
        }
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        RECIPES.register(bus);
        bus.addListener(CreateCompat::setup);
        bus.addListener(CreateCompat::capabilities);
        bus.addListener(CreateCompat::creativeTabContents);
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, CreateDyeInteractions::rightClick);
    }

    private static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (Block block : blocks("seat")) {
                MovementBehaviour.REGISTRY.register(block, new SeatMovementBehaviour());
                MovingInteractionBehaviour.REGISTRY.register(block, new SeatInteractionBehaviour());
            }
            for (Block block : blocks("toolbox")) {
                MountedItemStorageType.REGISTRY.register(block, AllMountedStorageTypes.TOOLBOX.get());
            }
        });
    }

    private static void creativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey())) {
            VARIANTS.forEach(variant -> event.accept(variant.block().get()));
        }
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TOOLBOX_ENTITY.get(),
                (be, side) -> be.collectComponents().get(AllDataComponents.TOOLBOX_INVENTORY));
    }

    private CreateCompat() {}
}
