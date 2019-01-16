package CivcraftRedefined.WorldGen.VillageGen;

import CivcraftRedefined.WorldGen.PerlinMaps;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.storage.WorldProperties;

public class VillageGenerator implements GenerationPopulator {

    private PerlinMaps perlinMaps;
    private VillageServiceProvider villageServiceProvider;

    public VillageGenerator(PerlinMaps perlinMaps, WorldProperties world) {
        this.perlinMaps = perlinMaps;
        villageServiceProvider = new VillageServiceProvider(world, perlinMaps);
    }

    @Override
    public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeVolume biomes) {
        double height;
        for (int x = buffer.getBlockMin().getX(); x <= buffer.getBlockMax().getX(); x++) {
            for (int z = buffer.getBlockMin().getZ(); z <= buffer.getBlockMax().getZ(); z++) {
                VillageServiceProvider.BlockStack struct = villageServiceProvider.getStructureStack(x, z);
                if (struct != null) {
                    height = perlinMaps.getAdjustedHeight(x, z);
                    for (int y = (int) height; y < VillageServiceProvider.BlockStack.associatedStruct.mapRelative.getY(); y++) {
                        buffer.setBlock(x, y, z, VillageServiceProvider.BlockStack.associatedStruct.foundation);
                    }
                    for (int y = VillageServiceProvider.BlockStack.associatedStruct.mapRelative.getY(); y < VillageServiceProvider.BlockStack.blockStates.length + VillageServiceProvider.BlockStack.associatedStruct.mapRelative.getY(); y++) {
                        BlockState curBlock = VillageServiceProvider.BlockStack.blockStates[y - VillageServiceProvider.BlockStack.associatedStruct.mapRelative.getY()];
                        if (curBlock != null)
                            buffer.setBlock(x, y, z, curBlock);
                    }
                }
            }
        }
    }
}