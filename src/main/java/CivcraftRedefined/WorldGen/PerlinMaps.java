package CivcraftRedefined.WorldGen;

import CivcraftRedefined.civcraftRedefined;
import org.spongepowered.api.world.biome.BiomeType;

import java.util.ArrayList;
import java.util.List;

public class PerlinMaps {
    private MapInterpretor mapInterpretor = civcraftRedefined.getMapInterpretor();
    private Perlin PERLIN1;
    private Perlin PERLIN2;
    private Perlin PERLIN3;

    public PerlinMaps(long seed) {
        PERLIN1 = new Perlin(seed);
        PERLIN2 = new Perlin(seed + 1);
        PERLIN3 = new Perlin(seed + 2);
    }

    public int getMesaPlateau(double x, double y) {
        double result;
        result = PERLIN1.eval(x / 128, y / 128);
        result *= 10;
        result += PERLIN1.eval(x / 128, y / 128) > 0.6 ? 30 : 50;
        result = Math.max(1, result);
        result = Math.min(200, result);

        return (int) result;
    }

    public double getSurfaceTexture(int x, int y) {
        return PERLIN1.eval(((double) x) / 20, ((double) y) / 16);
    }

    public int getPlains(double x, double y) {
        double result;
        result = PERLIN2.eval(x / 100, y / 100) / 3;
        result += PERLIN3.eval(x / 50, y / 50) / 2;
        result *= PERLIN3.eval(y / 128, x / 128);
        result = 1 - result * (result + 1);
        result *= 12;
        result = Math.max(1, result);
        result = Math.min(200, result);

        return (int) result;
    }

    public int getDeepOcean(double x, double y) {
        double result;
        result = PERLIN1.eval(x / 128, y / 128);
        result *= -15;
        result -= 15;
        result = Math.max(-22, result);
        result = Math.min(-3, result);

        return (int) result;
    }

    public int getHeight(int x, int y) {
        switch (mapInterpretor.getBiomeAt(x, y).getName()) {
            case "Mesa Plateau":
                return getMesaPlateau(x, y);
            case "Plains":
                return getPlains(x, y);
            case "Deep Ocean":
                return getDeepOcean(x, y);
            default:
                return getPlains(x, y);

        }
    }

    private double calculateAverage(List<Integer> marks) {
        Integer sum = 0;
        if (!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }

    private int getRangeForBiome(BiomeType biomeType) {
        switch (biomeType.getName()) {
            case "Mesa Plateau":
                return 8;
            case "Plains":
                return 3;
            default:
                return 5;

        }
    }

    public int getAdjustedHeight(int x, int y) {
        int result = getHeight(x, y);
        //int[] qs = new int[81];
        int rangeMax = getRangeForBiome(mapInterpretor.getBiomeAt(x, y));

        List<Integer> qs = new ArrayList<Integer>();
        int range = (int) (PERLIN2.eval(x / 20, y / 20) * rangeMax / 2);
        for (int i = -range; i <= range; i += 1)
            for (int j = -range; j <= range; j += 1) {
                qs.add(getHeight(x + i, y + i));
            }
        int avg = (int) calculateAverage(qs);
        int q1 = getHeight(x + 2, y + 2);
        int q2 = getHeight(x - 2, y + 2);
        int q3 = getHeight(x - 2, y - 2);
        int q4 = getHeight(x + 2, y - 2);
        int q5 = getHeight(x, y - 3);
        int q6 = getHeight(x - 3, y);
        int q7 = getHeight(x + 3, y);
        int q8 = getHeight(x, y + 3);
        int diag = q1 + q2 + q3 + q4;
        diag += PERLIN3.eval((double) y / 18, (double) x / 18) * rangeMax - rangeMax / 3;
        int axis = q5 + q6 + q7 + q8;
        axis += PERLIN3.eval((double) x / 18, (double) y / 18) * rangeMax - rangeMax / 3;
        result = (diag) / 20 + (axis) / 16 + avg / 8 + result / 2;


        return result + 24;
    }
}
