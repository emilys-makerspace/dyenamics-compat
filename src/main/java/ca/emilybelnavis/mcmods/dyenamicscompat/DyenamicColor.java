package ca.emilybelnavis.mcmods.dyenamicscompat;

import net.minecraft.world.item.DyeColor;

/** Dyenamics 3.5.0's extra palette. The analogue is only used by Create's vanilla UI. */
public enum DyenamicColor {
    PEACH("peach", 12556403, DyeColor.ORANGE),
    AQUAMARINE("aquamarine", 2915711, DyeColor.CYAN),
    FLUORESCENT("fluorescent", 15657398, DyeColor.YELLOW),
    MINT("mint", 9038008, DyeColor.LIME),
    MAROON("maroon", 9502720, DyeColor.RED),
    BUBBLEGUM("bubblegum", 16216518, DyeColor.PINK),
    LAVENDER("lavender", 14522879, DyeColor.MAGENTA),
    PERSIMMON("persimmon", 13779225, DyeColor.ORANGE),
    CHERENKOV("cherenkov", 104655, DyeColor.LIGHT_BLUE),
    AMBER("amber", 13857561, DyeColor.ORANGE),
    HONEY("honey", 16757830, DyeColor.YELLOW),
    ULTRAMARINE("ultramarine", 5655039, DyeColor.BLUE),
    SPRING_GREEN("spring_green", 12844953, DyeColor.LIME),
    ROSE("rose", 16722021, DyeColor.RED),
    NAVY("navy", 1845361, DyeColor.BLUE),
    ICY_BLUE("icy_blue", 10414847, DyeColor.BLUE),
    WINE("wine", 7478875, DyeColor.PURPLE),
    CONIFER("conifer", 12245589, DyeColor.LIME);

    public final String id;
    public final int rgb;
    public final DyeColor analogue;

    DyenamicColor(String id, int rgb, DyeColor analogue) {
        this.id = id;
        this.rgb = rgb;
        this.analogue = analogue;
    }
}
