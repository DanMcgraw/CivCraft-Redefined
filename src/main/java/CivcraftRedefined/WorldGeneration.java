package CivcraftRedefined;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.List;

public class WorldGeneration {
    public class SolidWorldGeneratorModifier implements WorldGeneratorModifier {
        @Override
        public void modifyWorldGenerator(WorldProperties world, DataContainer settings, WorldGenerator worldGenerator) {
            for (BiomeType biomeType : Sponge.getRegistry().getAllOf(BiomeType.class)) {
                BiomeGenerationSettings biomeData = worldGenerator.getBiomeSettings(biomeType);
                List<Ore> populators = biomeData.getPopulators(Ore.class);
                biomeData.getPopulators().removeAll(populators);
                biomeData.setMaxHeight(128);
                biomeData.setMinHeight(100);
                civcraftRedefined.getInstance().getLogger().info("Biome " + biomeType.getName() + " is max height " + biomeData.getMaxHeight());
                //worldGenerator.getBiomeSettings(biomeType).getPopulators().clear();
            }
            worldGenerator.setBiomeGenerator(new IslandBiomeGen());
            //worldGenerator.setBaseGenerationPopulator(new SolidWorldTerrainGenerator());
        }

        @Override
        public String getId() {
            return "civ:solid";
        }

        @Override
        public String getName() {
            return "Solid";
        }
    }

    public class SolidWorldTerrainGenerator implements GenerationPopulator {
        @Override
        public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeVolume biomes) {
            //world.set
            for (int x = buffer.getBlockMin().getX(); x < buffer.getBlockMax().getX(); x++) {
                for (int z = buffer.getBlockMin().getZ(); z < buffer.getBlockMax().getZ(); z++) {
                    BiomeType biome = biomes.getBiome(x, 0, z);
                    int height = getHeight(x, z, world.getWorldGenerator().getBiomeSettings(biome));
                    for (int y = 0; y < height || y < 64; y++) {
                        if (y < height) {
                            buffer.setBlockType(x, y, z, BlockTypes.STONE);
                        } else {
                            buffer.setBlockType(x, y, z, BlockTypes.WATER);
                        }
                    }
                }
            }
        }

        private int getHeight(int x, int z, BiomeGenerationSettings biome) {
            double sx = Math.sin(x / 64d) + 1;
            double sz = Math.sin(z / 64d) + 1;
            double value = (sx + sz) / 4d;
            double heightRange = biome.getMaxHeight() - biome.getMinHeight();
            double height = heightRange * value / biome.getMinHeight();
            return GenericMath.floor(height * 256);
        }
    }

    public class IslandBiomeGen implements BiomeGenerator {

        private static final double ISLAND_SIZE = 200f;
        private static final double BEACH_RADIUS = ISLAND_SIZE * ISLAND_SIZE;
        private static final double FOREST_SIZE = ISLAND_SIZE - 14;
        private static final double FOREST_RADIUS = FOREST_SIZE * FOREST_SIZE;
        private static final double HILLS_SIZE = FOREST_SIZE - 120;
        private static final double HILLS_RADIUS = HILLS_SIZE * HILLS_SIZE;
        private double ANGLE;

        @Override
        public void generateBiomes(MutableBiomeVolume buffer) {
            Vector3i min = buffer.getBiomeMin();
            Vector3i max = buffer.getBiomeMax();

            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    buffer.setBiome(x, 0, z, civcraftRedefined.getMapInterpretor().getBiomeAt(x, z));

                }
            }
        }
    }
}
