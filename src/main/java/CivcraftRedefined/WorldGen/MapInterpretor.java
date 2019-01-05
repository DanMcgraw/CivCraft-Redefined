package CivcraftRedefined.WorldGen;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MapInterpretor {
    private BiomeType[][] array2D;
    private int width, height;
    private int sizeMultiplier;

    public MapInterpretor(String mapName) {
        Sponge.getServer().getConsole().sendMessage(Text.of(new File(mapName).getAbsolutePath()));
        sizeMultiplier = 4;
        try {
            BufferedImage image = ImageIO.read(new File(mapName));
            width = image.getWidth();
            height = image.getHeight();

            array2D = new BiomeType[width][height];
            for (int xPixel = 0; xPixel < width; xPixel++) {
                for (int yPixel = 0; yPixel < height; yPixel++) {
                    String color = convertColorToHexadeimal(image.getRGB(xPixel, yPixel));
                    array2D[xPixel][yPixel] = colToBio(color);
//                    if (color == "08694a") {
//                        array2D[xPixel][yPixel] = 1;
//                    } else if(color == "b5b2b5") {
//                        array2D[xPixel][yPixel] = 2;
//                    }else if(color == "5a8a84") {
//                        array2D[xPixel][yPixel] = 3;
//                    }else if(color == "63c69c") {
//                        array2D[xPixel][yPixel] = 4;
//                    }else if(color == "ffa263") {
//                        array2D[xPixel][yPixel] = 5;
//                    }else if(color == "bdca8c") {
//                        array2D[xPixel][yPixel] = 6;
//                    }else{
//                        array2D[xPixel][yPixel] = 0; // ?
//                    }
                }
            }
        } catch (IOException io) {
            Sponge.getServer().getConsole().sendMessage(Text.of("NOT WORKING" + io.toString()));
        }
    }

    public static String convertColorToHexadeimal(int color) {
        String hex = Integer.toHexString(color & 0xffffff);
        if (hex.length() < 6) {
            if (hex.length() == 5)
                hex = "0" + hex;
            if (hex.length() == 4)
                hex = "00" + hex;
            if (hex.length() == 3)
                hex = "000" + hex;
        }
        return hex;
    }

    public BiomeType colToBio(String color) {
        switch (color) {
            case "08694a":
                return BiomeTypes.TAIGA_HILLS;
            case "5a8a84":
                return BiomeTypes.TAIGA_MOUNTAINS;
            case "b5b2b5":
                return BiomeTypes.COLD_TAIGA_MOUNTAINS;
            case "63c69c":
                return BiomeTypes.MESA_PLATEAU_MOUNTAINS;
            case "ffa263":
                return BiomeTypes.MESA_PLATEAU;
            case "bdca8c":
                return BiomeTypes.PLAINS;
            case "ffdb31":
                return BiomeTypes.DESERT_HILLS;
            case "e6c694":
                return BiomeTypes.DESERT;
            case "ffdbde":
                return BiomeTypes.MESA;

        }
        return BiomeTypes.DEEP_OCEAN;
    }

    public BiomeType getBiomeAt(int x, int z) {

        x /= sizeMultiplier;
        z /= sizeMultiplier;
        x += width / 2;
        z += height / 2;
        if (x > 0 && x < width && z > 0 && z < height) {
            return array2D[x][z];
        } else
            return BiomeTypes.DEEP_OCEAN;
    }
}
