package ca.emilybelnavis.mcmods.dyenamicscompat.create.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Recolors only pixels that differ between Create's white and red reference textures. */
public final class PaletteTextures {
    public static byte[] recolor(byte[] whitePng, byte[] redPng, int color) throws IOException {
        BufferedImage white = ImageIO.read(new ByteArrayInputStream(whitePng));
        BufferedImage red = ImageIO.read(new ByteArrayInputStream(redPng));
        if (white == null || red == null || white.getWidth() != red.getWidth() || white.getHeight() != red.getHeight()) {
            throw new IOException("Invalid or mismatched Create palette textures");
        }
        BufferedImage output = new BufferedImage(white.getWidth(), white.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < white.getHeight(); y++) {
            for (int x = 0; x < white.getWidth(); x++) {
                int pixel = white.getRGB(x, y);
                if (pixel != red.getRGB(x, y) && (pixel >>> 24) != 0) {
                    int intensity = Math.max((pixel >> 16) & 255, Math.max((pixel >> 8) & 255, pixel & 255));
                    int r = ((color >> 16) & 255) * intensity / 255;
                    int g = ((color >> 8) & 255) * intensity / 255;
                    int b = (color & 255) * intensity / 255;
                    pixel = (pixel & 0xFF000000) | (r << 16) | (g << 8) | b;
                }
                output.setRGB(x, y, pixel);
            }
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(output, "PNG", bytes);
        return bytes.toByteArray();
    }

    private PaletteTextures() {}
}
