package CivcraftRedefined.WorldGen.VillageGen;

import CivcraftRedefined.WorldGen.Perlin;
import CivcraftRedefined.WorldGen.VillageGen.StructIntrp.StrTypes;
import CivcraftRedefined.WorldGen.VillageGen.StructIntrp.Structure;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;

public class StructGenrat {
    private long seed;
    private Perlin perlin;

    public StructGenrat(World world) {
        seed = world.getProperties().getSeed();
        perlin = new Perlin(seed);
    }

    private static BlockState[][][] getRing(int radius, BlockState blockType) {
        radius = (radius / 2) * 2;

    }

    private static BlockState[][][] mergeBlockState(BlockState[][][] master, BlockState[][][] slave) {
        Vector3i wHL = getBlockStateSize(master);
        Vector3i wHLS = getBlockStateSize(slave);
        Vector3i dif = wHL.sub(wHLS).div(2);

        for (int x = dif.getX(); x < wHL.getX() - dif.getX(); x++)
            for (int y = dif.getY(); y < wHL.getY() - dif.getY(); y++)
                for (int z = dif.getZ(); z < wHL.getZ() - dif.getZ(); z++)
                    if (slave[x - dif.getX()][y - dif.getY()][z - dif.getZ()] != null)
                        master[x][y][z] = slave[x - dif.getX()][y - dif.getY()][z - dif.getZ()];

        return master;
    }

    private static BlockState[][][] mergeBlockStateSoft(BlockState[][][] master, BlockState[][][] slave) {
        Vector3i wHL = getBlockStateSize(master);
        Vector3i wHLS = getBlockStateSize(slave);
        Vector3i dif = wHL.sub(wHLS).div(2);

        for (int x = dif.getX(); x < wHL.getX() - dif.getX(); x++)
            for (int y = dif.getY(); y < wHL.getY() - dif.getY(); y++)
                for (int z = dif.getZ(); z < wHL.getZ() - dif.getZ(); z++)
                    if (slave[x - dif.getX()][y - dif.getY()][z - dif.getZ()] != null && master[x][y][z] == null)
                        master[x][y][z] = slave[x - dif.getX()][y - dif.getY()][z - dif.getZ()];

        return master;
    }

    private static Vector3i getBlockStateSize(BlockState[][][] master) {
        int x = master.length;
        int y = master[0].length;
        int z = master[0][0].length;

        return new Vector3i(x, y, z);
    }

    //return number 0.75-1.00
    private double getRatio(int x) {
        double ratio = perlin.eval(x, 0);
        ratio *= 0.25;
        ratio += 0.75;
        return ratio;
    }

    public Structure createStructure(int type, double ratio, int importance) {
        Vector3i widthHeightLength = new Vector3i(8, 8, 8);
        Structure structure = new Structure(StrTypes.KEEP, widthHeightLength);
        new BlockState[][][]
        return structure;
    }

}
