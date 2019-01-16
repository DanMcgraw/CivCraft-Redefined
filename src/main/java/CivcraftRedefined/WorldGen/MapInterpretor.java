package CivcraftRedefined.WorldGen;

import com.flowpowered.math.vector.Vector2i;
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
    private BiomeType[][] array2Dx4;
    private boolean[][] array2DBordersx4;
    private boolean[][] array2DBorders;
    private int width, height;
    public static int sizeMultiplier = 8;

    public MapInterpretor(String mapName) {
        Sponge.getServer().getConsole().sendMessage(Text.of(new File(mapName).getAbsolutePath()));
        try {
            BufferedImage image = ImageIO.read(new File(mapName));
            width = image.getWidth();
            height = image.getHeight();

            array2D = new BiomeType[width][height];
            array2Dx4 = new BiomeType[width * sizeMultiplier][height * sizeMultiplier];
            array2DBordersx4 = new boolean[width * sizeMultiplier][height * sizeMultiplier];
            array2DBorders = new boolean[width][height];
            for (int xPixel = 0; xPixel < width; xPixel++) {
                for (int yPixel = 0; yPixel < height; yPixel++) {
                    String color = convertColorToHexadeimal(image.getRGB(xPixel, yPixel));
                    array2D[xPixel][yPixel] = colToBio(color);
                }
            }
            for (int x = sizeMultiplier / 2; x < width * sizeMultiplier - sizeMultiplier; x++) {
                for (int y = sizeMultiplier / 2; y < height * sizeMultiplier - sizeMultiplier; y++) {
                    BiomeType type = array2D[x / sizeMultiplier][y / sizeMultiplier];
                    BiomeType[] compares = new BiomeType[12];
                    compares[0] = array2D[(x + 1) / sizeMultiplier][(y) / sizeMultiplier];
                    compares[1] = array2D[(x + 2) / sizeMultiplier][(y) / sizeMultiplier];
                    compares[2] = array2D[(x + 3) / sizeMultiplier][(y) / sizeMultiplier];
                    compares[3] = array2D[(x - 3) / sizeMultiplier][(y) / sizeMultiplier];
                    compares[4] = array2D[(x - 2) / sizeMultiplier][(y) / sizeMultiplier];
                    compares[5] = array2D[(x - 1) / sizeMultiplier][(y) / sizeMultiplier];
                    compares[6] = array2D[(x) / sizeMultiplier][(y + 1) / sizeMultiplier];
                    compares[7] = array2D[(x) / sizeMultiplier][(y + 2) / sizeMultiplier];
                    compares[8] = array2D[(x) / sizeMultiplier][(y + 3) / sizeMultiplier];
                    compares[9] = array2D[(x) / sizeMultiplier][(y - 3) / sizeMultiplier];
                    compares[10] = array2D[(x) / sizeMultiplier][(y - 2) / sizeMultiplier];
                    compares[11] = array2D[(x) / sizeMultiplier][(y - 1) / sizeMultiplier];
                    array2Dx4[x][y] = useNeighbor(type, compares);
                }
            }
            for (int xPixel = sizeMultiplier; xPixel < width * sizeMultiplier - sizeMultiplier; xPixel++) {
                for (int yPixel = sizeMultiplier; yPixel < height * sizeMultiplier - sizeMultiplier; yPixel++) {
                    array2DBordersx4[xPixel][yPixel] = hasDifferentNeighbor(array2Dx4, new Vector2i(xPixel, yPixel));
                }
            }
            for (int xPixel = 1; xPixel < width - 1; xPixel++) {
                for (int yPixel = 1; yPixel < height - 1; yPixel++) {
                    array2DBorders[xPixel][yPixel] = hasDifferentNeighbor(array2D, new Vector2i(xPixel, yPixel));
                }
            }
        } catch (IOException io) {
            Sponge.getServer().getConsole().sendMessage(Text.of("NOT WORKING" + io.toString()));
        }
    }

    private static BiomeType useNeighbor(BiomeType original, BiomeType[] biomeTypes) {
        int i = 0;
        BiomeType otherBiome = original;
        for (BiomeType biomeType : biomeTypes)
            if (!biomeType.equals(original)) {
                i++;
                otherBiome = biomeType;
            }
        return i > 4 ? otherBiome : original;
    }

    public static boolean hasDifferentNeighbor(BiomeType[][] biomeType, Vector2i loc) {
        if (!biomeType[loc.getX()][loc.getY()].equals(biomeType[loc.getX() + 1][loc.getY()]))
            return true;
        if (!biomeType[loc.getX()][loc.getY()].equals(biomeType[loc.getX() - 1][loc.getY()]))
            return true;
        if (!biomeType[loc.getX()][loc.getY()].equals(biomeType[loc.getX()][loc.getY() - 1]))
            return true;
        if (!biomeType[loc.getX()][loc.getY()].equals(biomeType[loc.getX()][loc.getY() + 1]))
            return true;
        if (!biomeType[loc.getX()][loc.getY()].equals(biomeType[loc.getX() + 1][loc.getY() + 1]))
            return true;
        if (!biomeType[loc.getX()][loc.getY()].equals(biomeType[loc.getX() - 1][loc.getY() - 1]))
            return true;
        if (!biomeType[loc.getX()][loc.getY()].equals(biomeType[loc.getX() + 1][loc.getY() - 1]))
            return true;
        return !biomeType[loc.getX()][loc.getY()].equals(biomeType[loc.getX() - 1][loc.getY() + 1]);
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
            case "4090ff":
                return BiomeTypes.OCEAN;
            case "ffe75f":
                return BiomeTypes.BEACH;

        }
        return BiomeTypes.DEEP_OCEAN;
    }

    public boolean isBorderSmall(int x, int z) {
        x += width * sizeMultiplier / 2;
        z += height * sizeMultiplier / 2;
        if (x > sizeMultiplier && x < width * sizeMultiplier - sizeMultiplier && z > sizeMultiplier && z < height * sizeMultiplier - sizeMultiplier) {
            return array2DBordersx4[x][z];
        } else
            return false;
    }

    public boolean isBorderLarge(int x, int z) {
        x -= 2;
        x /= sizeMultiplier;
        z /= sizeMultiplier;
        x += width / 2;
        z += height / 2;
        if (x > 1 && x < width - 1 && z > 1 && z < height - 1) {
            return array2DBorders[x][z];
        } else
            return false;
    }

    public BiomeType getBiomeAt(int x, int z) {

        //x /= sizeMultiplier/2;
        //z /= sizeMultiplier/2;
        x += width * sizeMultiplier / 2;
        z += height * sizeMultiplier / 2;
        if (x > 0 && x < width * sizeMultiplier && z > 0 && z < height * sizeMultiplier) {
            return array2Dx4[x][z];
        } else
            return BiomeTypes.DEEP_OCEAN;
    }
}
