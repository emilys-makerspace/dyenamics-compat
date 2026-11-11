package ca.emilybelnavis.mcmods.dyenamicscompat.letsdo.furniture;

import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.satisfy.furniture.core.block.CurtainBlock;
import net.satisfy.furniture.core.block.LampBlock;
import net.satisfy.furniture.core.block.LampWallBlock;
import net.satisfy.furniture.core.block.PouffeBlock;
import net.satisfy.furniture.core.block.SofaBlock;
import net.neoforged.bus.api.IEventBus;

public final class FurnitureCompat {
    public static void register(IEventBus bus) {
        var module = new PaletteModule("furniture", "furniture");
        module.family("curtain", "curtain_%s", "white", false, (color, properties) -> new CurtainBlock(properties, color.analogue));
        module.family("sofa", "sofa_%s", "white", false, (color, properties) -> new SofaBlock(properties, color.analogue));
        module.family("pouffe", "pouffe_%s", "white", false, (color, properties) -> new PouffeBlock(properties, color.analogue));
        module.family("lamp_wall", "lamp_wall_%s", "white", false, (color, properties) -> new LampWallBlock(properties, color.analogue), null);
        module.family("lamp", "lamp_%s", "white", false, (color, properties) -> new LampBlock(properties, color.analogue), block -> {
            var variant = PaletteModule.variant(block);
            return new StandingAndWallBlockItem(block, PaletteModule.variant("furniture", "lamp_wall", variant.color()).block().get(), new Item.Properties(), Direction.DOWN);
        });
        for (String textile : new String[]{"rustic", "linen", "jacquard", "plaid", "chambray", "tweed", "warped", "homespun"}) {
            String owner = textile.equals("homespun") ? "alpinewhispers" : "meadow";
            if (!net.neoforged.fml.ModList.get().isLoaded(owner)) continue;
            String key = owner + "_" + textile;
            if (net.satisfy.furniture.core.registry.ObjectRegistry.CURTAINS.containsKey(key)) {
                module.family(textile + "_curtain", "curtain_" + textile, "white", false, (color, properties) -> new CurtainBlock(properties, color.analogue));
            }
            if (net.satisfy.furniture.core.registry.ObjectRegistry.POUFFE.containsKey(key)) {
                module.family(textile + "_pouffe", "pouffe_" + textile, "white", false, (color, properties) -> new PouffeBlock(properties, color.analogue));
            }
            if (net.satisfy.furniture.core.registry.ObjectRegistry.SOFAS.containsKey(key)) {
                module.family(textile + "_sofa", "sofa_" + textile, "white", false, (color, properties) -> new SofaBlock(properties, color.analogue));
            }
            if (net.satisfy.furniture.core.registry.ObjectRegistry.LAMP_ITEMS.containsKey(key)) {
                module.family(textile + "_lamp_wall", "lamp_wall_" + textile, "white", false, (color, properties) -> new LampWallBlock(properties, color.analogue), null);
                module.family(textile + "_lamp", "lamp_" + textile, "white", false, (color, properties) -> new LampBlock(properties, color.analogue), block -> {
                    var variant = PaletteModule.variant(block);
                    return new StandingAndWallBlockItem(block, PaletteModule.variant("furniture", textile + "_lamp_wall", variant.color()).block().get(), new Item.Properties(), Direction.DOWN);
                });
            }
        }
        module.register(bus);
    }
    private FurnitureCompat() {}
}
