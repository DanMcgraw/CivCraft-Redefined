package CivcraftRedefined.WorldGen.VillageGen;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;

public class StructIntrp {
    public static Structure applyBlockState(Structure struct, BlockState[][][] toApply) {
        for (int x = 0; x < struct.wHL.getX(); x++)
            for (int z = 0; z < struct.wHL.getZ(); z++)
                for (int y = 0; y < struct.wHL.getY(); y++)
                    if (toApply[x][z][y] != null)
                        struct.blockData[x][z][y] = toApply[x][z][y];
        return struct;
    }

    public static Structure applyQuadMirror(Structure struct, BlockState[][][] toApply) {
        for (int x = 0; x < struct.wHL.getX() / 2; x++)
            for (int z = 0; z < struct.wHL.getZ() / 2; z++)
                for (int y = 0; y < struct.wHL.getY() / 2; y++)
                    if (toApply[x][z][y] != null) {
                        struct.blockData[struct.origin.getX() + x][struct.origin.getZ() + z][y] = toApply[x][z][y];
                        struct.blockData[struct.origin.getX() - x - 1][struct.origin.getZ() + z][y] = toApply[x][z][y];
                        struct.blockData[struct.origin.getX() - x - 1][struct.origin.getZ() - z - 1][y] = toApply[x][z][y];
                        struct.blockData[struct.origin.getX() + x][struct.origin.getZ() - z - 1][y] = toApply[x][z][y];
                    }
        return struct;
    }

    public static BlockState[] getXZColumn(int x, int z, Structure struct) {
        return struct.blockData[x][z];
    }

    public enum StrTypes {
        TOWNHALL, KEEP, HOUSE
    }

    public static class Structure {
        public boolean isStatic;
        public String name;
        public BlockState[][][] blockData;
        public Vector3i wHL;
        public Vector3i origin;
        public Vector3i doorPosition;
        public int importance;
        public StrTypes type;
        public BlockState foundation;

        public Structure(StrTypes type, Vector3i wHL) {
            this.wHL = wHL.div(2).mul(2);
            this.origin = wHL.div(2);
            this.type = type;
        }

    }
}
