package CivcraftRedefined.WorldGen;

import CivcraftRedefined.civcraftRedefined;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.populator.RandomBlock;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.ArrayList;
import java.util.List;

public class WorldGeneration {
    private PerlinMaps perlinMaps;
    public class SolidWorldGeneratorModifier implements WorldGeneratorModifier {
        @Override
        public void modifyWorldGenerator(WorldProperties world, DataContainer settings, WorldGenerator worldGenerator) {
            perlinMaps = new PerlinMaps(world.getSeed());
            GroundCoverLayerPopulator groundCoverPopulator = new GroundCoverLayerPopulator();
            civcraftRedefined.getInstance().getLogger().info(civcraftRedefined.getMapInterpretor().getBiomeAt(0, 0).getName());
            civcraftRedefined.getInstance().getLogger().info(civcraftRedefined.getMapInterpretor().getBiomeAt(20, 250).getName());

            for (BiomeType biomeType : Sponge.getRegistry().getAllOf(BiomeType.class)) {
                BiomeGenerationSettings biomeData = worldGenerator.getBiomeSettings(biomeType);
                biomeData.getPopulators().removeIf(pop -> (pop instanceof Ore) || (pop instanceof Dungeon) || (pop instanceof RandomBlock));
                Object[] layers = biomeData.getGroundCoverLayers().toArray();
                for (Object layer : layers) {
                    ((GroundCoverLayer) layer).setBlockState(BlockTypes.BRICK_BLOCK.getDefaultState());
                }
                biomeData.setMaxHeight(0.25f);
                biomeData.setMinHeight(0.15f);
                biomeData.getPopulators().clear();
                //biomeData.getGenerationPopulators(GenerationPopulator)
                biomeData.getGenerationPopulators().clear();
                biomeData.getGroundCoverLayers().clear();

                List<GroundCoverLayer> biomeLayers = biomeData.getGroundCoverLayers();
                groundCoverPopulator.getBiomeLayers(biomeType).addAll(biomeLayers);
                biomeLayers.clear();
                biomeData.getGenerationPopulators().clear();
            }
            worldGenerator.getPopulators().clear();
            worldGenerator.getGenerationPopulators().clear();
            worldGenerator.getGenerationPopulators().add(groundCoverPopulator);
            CivcraftGenerator civcraftGenerator = new CivcraftGenerator();
            civcraftGenerator.getPopulators().add(new TerrainGeneration());

            worldGenerator.setBiomeGenerator(new IslandBiomeGen());
            worldGenerator.setBaseGenerationPopulator(civcraftGenerator);
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

    public class CivcraftGenerator implements GenerationPopulator {
        private final List<GenerationPopulator> populators = new ArrayList<>();

        public CivcraftGenerator() {

        }

        public List<GenerationPopulator> getPopulators() {
            return this.populators;
        }

        @Override
        public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeVolume biomes) {

            for (GenerationPopulator p : this.populators) {
                try {
                    p.populate(world, buffer, biomes);
                } catch (Exception e) {
                    civcraftRedefined.getInstance().getLogger().error("Generation populator '" + p.getClass().getName() + "' has thrown an exception", e);
                }
            }


        }
    }

    public class TerrainGeneration implements GenerationPopulator {

        @Override
        public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeVolume biomes) {
            double height;
            final int water = 30;
            for (int x = buffer.getBlockMin().getX(); x <= buffer.getBlockMax().getX(); x++) {
                for (int z = buffer.getBlockMin().getZ(); z <= buffer.getBlockMax().getZ(); z++) {
                    buffer.setBlockType(x, 0, z, BlockTypes.BEDROCK);

                    height = perlinMaps.getAdjustedHeight(x, z);
                    height += water - 6;
                    for (int y = 1; y < height; y++)
                        buffer.setBlockType(x, y, z, BlockTypes.STONE);
                    for (int y = (int) height; y < water; y++)
                        buffer.setBlockType(x, y, z, BlockTypes.WATER);

                }
            }
        }
    }

    public class IslandBiomeGen implements BiomeGenerator {

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
