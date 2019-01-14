package CivcraftRedefined.WorldGen.VillageGen;

import CivcraftRedefined.WorldGen.Perlin;
import CivcraftRedefined.WorldGen.VillageGen.StructIntrp.StrTypes;
import CivcraftRedefined.WorldGen.VillageGen.StructIntrp.Structure;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.World;

public class StructGenrat {
    private long seed;
    private Perlin perlin;

    public StructGenrat(World world) {
        seed = world.getProperties().getSeed();
        perlin = new Perlin(seed);
    }

    private static BlockState[][][] getRing(int diameter, BlockState blockType) {
        diameter = (diameter / 2) * 2;
        BlockState[][][] ring = new BlockState[diameter][diameter][diameter];
        for (int i = 1; i < diameter; i++) {
            ring[i][0][0] = blockType;
            ring[i][0][diameter - 1] = blockType;
            ring[0][0][i] = blockType;
            ring[diameter - 1][0][i] = blockType;
        }
        return ring;
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

        for (int height = 0; height < 6; height++)
            StructIntrp.mergeBlockState(structure, getRing(6, BlockTypes.GOLD_BLOCK.getDefaultState()), height);
        return structure;
    }

}
