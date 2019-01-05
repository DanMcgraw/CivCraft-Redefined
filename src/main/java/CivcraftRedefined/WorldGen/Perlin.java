package CivcraftRedefined.WorldGen;

import com.flowpowered.math.vector.Vector2i;

import java.util.Random;

public class Perlin {
    private long seed;
    private Random rand;
    private int octave;

    public Perlin(long seed, int octave) {
        this.seed = seed;
        this.octave = octave;
        rand = new Random();
    }

    public double getNoiseLevelAtPosition(int x, int z) {
        x = Math.abs(x);
        z = Math.abs(z);
        int xmin = (int) (double) x / octave;
        int xmax = xmin + 1;
        int zmin = (int) (double) z / octave;
        int zmax = zmin + 1;
        Vector2i a = new Vector2i(xmin, zmin);
        Vector2i b = new Vector2i(xmax, zmin);
        Vector2i c = new Vector2i(xmax, zmax);
        Vector2i d = new Vector2i(xmin, zmax);
        double ra = getRandomAtPosition(a);
        double rb = getRandomAtPosition(b);
        double rc = getRandomAtPosition(c);
        double rd = getRandomAtPosition(d);
        double result = (double) cosineInterpolate( //Interpolate Z direction
                cosineInterpolate((float) ra, (float) rb, (float) (x - xmin * octave) / octave), //Interpolate X1
                cosineInterpolate((float) rd, (float) rc, (float) (x - xmin * octave) / octave), //Interpolate X2
                ((float) z - (float) zmin * (float) octave) / (float) octave);
        return result;
    }

    private float cosineInterpolate(float a, float b, float x) {
        float ft = (float) (x * Math.PI);
        float f = (float) ((1f - Math.cos(ft)) * .5f);
        return a * (1f - f) + b * f;
    }

    private double getRandomAtPosition(Vector2i coord) {
        double var = 10000 * (Math.sin(coord.getX()) + Math.cos(coord.getY()) + Math.tan(seed));
        rand.setSeed((long) var);
        return rand.nextDouble();
    }
}
