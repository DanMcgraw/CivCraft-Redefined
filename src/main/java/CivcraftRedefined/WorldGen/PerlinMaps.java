package CivcraftRedefined.WorldGen;

public class PerlinMaps {
    public Perlin TAIGA;
    public Perlin MESA;

    public PerlinMaps(long seed) {
        TAIGA = new Perlin(seed, 2);
        MESA = new Perlin(seed, 32);
    }
}
