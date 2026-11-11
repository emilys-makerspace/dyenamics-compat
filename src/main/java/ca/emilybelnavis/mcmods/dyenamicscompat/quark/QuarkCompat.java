package ca.emilybelnavis.mcmods.dyenamicscompat.quark;

import ca.emilybelnavis.mcmods.dyenamicscompat.palette.PaletteModule;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.IEventBus;
import org.violetmoon.quark.content.building.block.StoolBlock;
import org.violetmoon.zeta.block.ZetaGlassBlock;

public final class QuarkCompat {
    public static void register(IEventBus bus) {
        var module = new PaletteModule("quark", null, "quark");
        module.family("shingles", "%s_shingles", "white", false, (c,p) -> new Block(p));
        module.family("shingles_slab", "%s_shingles_slab", "white", false, (c,p) -> new SlabBlock(p));
        module.family("shingles_stairs", "%s_shingles_stairs", "white", false, (c,p) -> new StairBlock(PaletteModule.variant("quark","shingles",c).block().get().defaultBlockState(),p));
        module.family("shingles_vertical_slab", "%s_shingles_vertical_slab", "white", false, (c,p) -> new org.violetmoon.quark.content.building.block.VerticalSlabBlock(() -> PaletteModule.variant("quark","shingles",c).block().get(), p));
        module.family("stool", "%s_stool", "white", false, (c,p) -> new StoolBlock(null,c.analogue));
        module.family("framed_glass", "%s_framed_glass", "white", false, (c,p) -> new ZetaGlassBlock("",null,true,p));
        module.family("framed_glass_pane", "%s_framed_glass_pane", "white", false, (c,p) -> new IronBarsBlock(p));
        module.itemFamily("shard", "%s_shard");
        module.register(bus);
    }
    private QuarkCompat() {}
}
