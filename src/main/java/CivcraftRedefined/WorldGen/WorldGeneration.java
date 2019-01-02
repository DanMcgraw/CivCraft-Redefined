package CivcraftRedefined.WorldGen;

import CivcraftRedefined.civcraftRedefined;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
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
    private GenerationPopulator originalGenerationPopulator;
    public class SolidWorldGeneratorModifier implements WorldGeneratorModifier {
        @Override
        public void modifyWorldGenerator(WorldProperties world, DataContainer settings, WorldGenerator worldGenerator) {
            GroundCoverLayerPopulator groundCoverPopulator = new GroundCoverLayerPopulator();

            for (BiomeType biomeType : Sponge.getRegistry().getAllOf(BiomeType.class)) {
                BiomeGenerationSettings biomeData = worldGenerator.getBiomeSettings(biomeType);
                biomeData.getPopulators().removeIf(pop -> (pop instanceof Ore) || (pop instanceof Dungeon) || (pop instanceof RandomBlock));
                Object[] layers = biomeData.getGroundCoverLayers().toArray();
                for (Object layer : layers) {
                    //((GroundCoverLayer)layer).setBlockState(BlockTypes.BRICK_BLOCK.getDefaultState());
                }
                biomeData.setMaxHeight(0.25f);
                biomeData.setMinHeight(0.15f);
                List<GroundCoverLayer> biomeLayers = biomeData.getGroundCoverLayers();
                groundCoverPopulator.getBiomeLayers(biomeType).addAll(biomeLayers);
                biomeLayers.clear();
                //biomeData.getGenerationPopulators().clear();
            }
            //worldGenerator.getPopulators().clear();
            worldGenerator.getPopulators(Dungeon.class).clear();
            originalGenerationPopulator = worldGenerator.getBaseGenerationPopulator();
            GenerationPopulatorAdapter sinusoidalGenerator = new GenerationPopulatorAdapter(-40, 40);
            sinusoidalGenerator.getPopulators().add(originalGenerationPopulator);
            sinusoidalGenerator.getPopulators().add(groundCoverPopulator);

            worldGenerator.setBiomeGenerator(new IslandBiomeGen());
            worldGenerator.setBaseGenerationPopulator(sinusoidalGenerator);
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

    public class GenerationPopulatorAdapter implements GenerationPopulator {
        public final int offsetY, minY;
        private final List<GenerationPopulator> populators = new ArrayList<>();

        public GenerationPopulatorAdapter(int offsetY, int minY) {
            this.offsetY = offsetY;
            this.minY = minY;
        }

        public List<GenerationPopulator> getPopulators() {
            return this.populators;
        }

        @Override
        public void populate(World world, MutableBlockVolume volume, ImmutableBiomeVolume biomes) {
            MutableBlockVolumeAdapter adapter = new MutableBlockVolumeAdapter(volume, this.offsetY, this.minY, world);

            for (GenerationPopulator p : this.populators) {
                try {
                    p.populate(world, adapter, biomes);
                } catch (Exception e) {
                    civcraftRedefined.getInstance().getLogger().error("Generation populator '" + p.getClass().getName() + "' has thrown an exception", e);
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
