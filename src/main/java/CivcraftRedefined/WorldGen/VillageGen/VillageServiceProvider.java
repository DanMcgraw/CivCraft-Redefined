package CivcraftRedefined.WorldGen.VillageGen;

import CivcraftRedefined.WorldGen.PerlinMaps;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class VillageServiceProvider {

    private static List<StructIntrp.Structure> structureMappings;
    private static StructGenrat structGenrat;
    private static PerlinMaps perlinMaps;

    public VillageServiceProvider(WorldProperties world, PerlinMaps perlinMaps) {
        //Generate Villages, and map each villages structures to structureMappings
        VillageServiceProvider.perlinMaps = perlinMaps;
        structGenrat = new StructGenrat(world);
        StructIntrp.Structure structure = structGenrat.createStructure(0, 0, 0);
        structure.mapRelative = new Vector3i(500, perlinMaps.getAdjustedHeight(500, 100), 100);
        structure.foundation = BlockTypes.BONE_BLOCK.getDefaultState();
        structureMappings = new ArrayList<StructIntrp.Structure>();
        structureMappings.add(structure);
    }

    public BlockStack getStructureStack(int x, int z) {
        ListIterator<StructIntrp.Structure> listIterator = structureMappings.listIterator();
        StructIntrp.Structure temp;
        while (listIterator.hasNext()) {
            temp = listIterator.next();
            if (temp.mapRelative.getX() < x && temp.mapRelative.getX() + temp.wHL.getX() > x)
                if (temp.mapRelative.getZ() < z && temp.mapRelative.getZ() + temp.wHL.getZ() > z) {
                    return new BlockStack(StructIntrp.getColumn(x - temp.mapRelative.getX(), z - temp.mapRelative.getZ(), temp), temp);
                }
        }
        return null;
    }

    public static class BlockStack {
        public static BlockState[] blockStates;
        public static StructIntrp.Structure associatedStruct;

        public BlockStack(BlockState[] blockStates, StructIntrp.Structure associatedStruct) {
            BlockStack.associatedStruct = associatedStruct;
            BlockStack.blockStates = blockStates;
        }
    }
}
