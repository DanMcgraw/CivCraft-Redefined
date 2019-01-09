package CivcraftRedefined.WorldGen;

import CivcraftRedefined.civcraftRedefined;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.ImmutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBiomeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.ArrayList;
import java.util.List;

public class WorldGeneration {
    private PerlinMaps perlinMaps;

    public class SolidWorldGeneratorModifier implements WorldGeneratorModifier {
        @Override
        public void modifyWorldGenerator(WorldProperties world, DataContainer settings, WorldGenerator worldGenerator) {
            perlinMaps = new PerlinMaps(world.getSeed());

            for (BiomeType biomeType : Sponge.getRegistry().getAllOf(BiomeType.class)) {
                BiomeGenerationSettings biomeData = worldGenerator.getBiomeSettings(biomeType);
                biomeData.getPopulators().clear();
                biomeData.getGenerationPopulators().clear();
                biomeData.getGroundCoverLayers().clear();
            }
            worldGenerator.getPopulators().clear();
            worldGenerator.getGenerationPopulators().clear();
            CivcraftGenerator civcraftGenerator = new CivcraftGenerator();
            civcraftGenerator.getPopulators().add(new TerrainGeneration());
            civcraftGenerator.getPopulators().add(new TopLayerGeneration());
            civcraftGenerator.getPopulators().add(new MesaColorLayers());
            civcraftGenerator.getPopulators().add(new TopPops());

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
                    for (int y = 1; y < (int) height; y++)
                        buffer.setBlockType(x, y, z, BlockTypes.STONE);
                    for (int y = (int) height; y < water; y++)
                        buffer.setBlockType(x, y, z, BlockTypes.WATER);

                }
            }
        }
    }

    public class TopLayerGeneration implements GenerationPopulator {

        @Override
        public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeVolume biomes) {
            double height;
            final int water = 30;
            for (int x = buffer.getBlockMin().getX(); x <= buffer.getBlockMax().getX(); x++) {
                for (int z = buffer.getBlockMin().getZ(); z <= buffer.getBlockMax().getZ(); z++) {
                    height = perlinMaps.getAdjustedHeight(x, z);
                    //height += water - 6;
                    List<BlockState> topBlocks = new ArrayList<>();
                    if (world.getBiome(x, 0, z).equals(BiomeTypes.MESA)) {
                        if (height < water)
                            topBlocks.add((perlinMaps.getSurfaceTexture(x, z, 10) > 0.5) ? (BlockState) BlockTypes.CONCRETE_POWDER.getAllBlockStates().toArray()[14] : BlockTypes.GRAVEL.getDefaultState());
                        else
                            topBlocks.add((perlinMaps.getSurfaceTexture(x, z, 10) > 0.5) ? (BlockState) BlockTypes.CONCRETE_POWDER.getAllBlockStates().toArray()[14] : (BlockState) BlockTypes.SAND.getAllBlockStates().toArray()[1]);
                        topBlocks.add(BlockTypes.RED_SANDSTONE.getDefaultState());
                        topBlocks.add(BlockTypes.RED_SANDSTONE.getDefaultState());
                        for (int i = (int) height - 1; i > height - 1 - topBlocks.size() * 2 && i > 0; i--)
                            buffer.setBlock(x, i, z, topBlocks.get(((int) height - 1 - i) / 2));
                    }
                    if (world.getBiome(x, 0, z).equals(BiomeTypes.MESA_PLATEAU)) {
                        //buffer.setBlockType(x,perlinMaps.getMesaPlateau(x,z),z,BlockTypes.STAINED_HARDENED_CLAY);
                        if (civcraftRedefined.getMapInterpretor().isBorderLarge(x, z))
                            for (int i = (int) height - 1; i > water; i--)
                                buffer.setBlockType(x, i, z, BlockTypes.STAINED_HARDENED_CLAY);
                        if (perlinMaps.isMesaTransition(x, z))
                            for (int i = (int) height - 1; i > water + 28; i--)
                                buffer.setBlockType(x, i, z, BlockTypes.STAINED_HARDENED_CLAY);
                        topBlocks.add((perlinMaps.getSurfaceTexture(x, z, 30) > 0.4) ? BlockTypes.STAINED_HARDENED_CLAY.getDefaultState() : (BlockState) BlockTypes.SAND.getAllBlockStates().toArray()[1]);
                        topBlocks.add(BlockTypes.STAINED_HARDENED_CLAY.getDefaultState());
                        topBlocks.add(BlockTypes.STAINED_HARDENED_CLAY.getDefaultState());
                        for (int i = (int) height - 1; i > height - 1 - topBlocks.size() * 2 && i > 0; i--)
                            buffer.setBlock(x, i, z, topBlocks.get(((int) height - 1 - i) / 2));
                    }
                    if (world.getBiome(x, 0, z).equals(BiomeTypes.PLAINS) || world.getBiome(x, 0, z).equals(BiomeTypes.TAIGA_HILLS)) {
                        //buffer.setBlockType(x,perlinMaps.getMesaPlateau(x,z),z,BlockTypes.STAINED_HARDENED_CLAY);
                        if (civcraftRedefined.getMapInterpretor().isBorderLarge(x, z))
                            for (int i = (int) height - 1; i > water; i--)
                                buffer.setBlockType(x, i, z, BlockTypes.STAINED_HARDENED_CLAY);
                        if (perlinMaps.isMesaTransition(x, z))
                            for (int i = (int) height - 1; i > water + 28; i--)
                                buffer.setBlockType(x, i, z, BlockTypes.STAINED_HARDENED_CLAY);
                        topBlocks.add((perlinMaps.getSurfaceTexture(x, z, 30) > 0.4) ? BlockTypes.STAINED_HARDENED_CLAY.getDefaultState() : (BlockState) BlockTypes.SAND.getAllBlockStates().toArray()[1]);
                        topBlocks.add(BlockTypes.STAINED_HARDENED_CLAY.getDefaultState());
                        topBlocks.add(BlockTypes.STAINED_HARDENED_CLAY.getDefaultState());
                        for (int i = (int) height - 1; i > height - 1 - topBlocks.size() * 2 && i > 0; i--)
                            buffer.setBlock(x, i, z, topBlocks.get(((int) height - 1 - i) / 2));
                    }
                    if (world.getBiome(x, 0, z).equals(BiomeTypes.DESERT_HILLS) || world.getBiome(x, 0, z).equals(BiomeTypes.DESERT)) {
                        //buffer.setBlockType(x,perlinMaps.getMesaPlateau(x,z),z,BlockTypes.STAINED_HARDENED_CLAY);
                        if (civcraftRedefined.getMapInterpretor().isBorderLarge(x, z))
                            for (int i = (int) height - 1; i > water; i--)
                                buffer.setBlockType(x, i, z, BlockTypes.SAND);
                        if (height > 45)
                            topBlocks.add((BlockState) BlockTypes.SAND.getAllBlockStates().toArray()[1]);
                        else if (height > 40)
                            topBlocks.add((perlinMaps.getSurfaceTexture(x, z, 2) > 1 - ((height - 41) / 5)) ? (BlockState) BlockTypes.SAND.getAllBlockStates().toArray()[1] : BlockTypes.SAND.getDefaultState());
                        else
                            topBlocks.add(BlockTypes.SAND.getDefaultState());
                        topBlocks.add(BlockTypes.SAND.getDefaultState());
                        topBlocks.add(BlockTypes.SANDSTONE.getDefaultState());
                        for (int i = (int) height - 1; i > height - 1 - topBlocks.size() * 2 && i > 0; i--)
                            buffer.setBlock(x, i, z, topBlocks.get(((int) height - 1 - i) / 2));
                    }
                }
            }
        }
    }

    public class MesaColorLayers implements GenerationPopulator {

        private List<BlockState> mesaLayers;

        public MesaColorLayers() {
            mesaLayers = new ArrayList<>();
            mesaLayers.add(BlockTypes.HARDENED_CLAY.getDefaultState());
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[14]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[4]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[8]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[1]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[0]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[8]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[4]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[12]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[1]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[12]);
            mesaLayers.add((BlockState) BlockTypes.STAINED_HARDENED_CLAY.getAllBlockStates().toArray()[4]);
        }

        @Override
        public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeVolume biomes) {
            double height;
            final int water = 30;
            for (int x = buffer.getBlockMin().getX(); x <= buffer.getBlockMax().getX(); x++) {
                for (int z = buffer.getBlockMin().getZ(); z <= buffer.getBlockMax().getZ(); z++) {
                    height = perlinMaps.getAdjustedHeight(x, z);
                    if (world.getBiome(x, 0, z).equals(BiomeTypes.MESA_PLATEAU)) {
                        //buffer.setBlockType(x,perlinMaps.getMesaPlateau(x,z),z,BlockTypes.STAINED_HARDENED_CLAY);
                        for (int y = water; y < height; y++)
                            if (buffer.getBlockType(x, y, z).equals(BlockTypes.STAINED_HARDENED_CLAY))
                                buffer.setBlock(x, y, z, mesaLayers.get((((y - water)) / (2 + (int) (perlinMaps.getSurfaceTexture(x, z + y / 10, 30) * 2))) % mesaLayers.size()));
//                        topBlocks.add((perlinMaps.getSurfaceTexture(x, z) > 0.4) ? BlockTypes.STAINED_HARDENED_CLAY.getDefaultState() : (BlockState) BlockTypes.SAND.getAllBlockStates().toArray()[1]);
//                        topBlocks.add(BlockTypes.STAINED_HARDENED_CLAY.getDefaultState());
//                        topBlocks.add(BlockTypes.STAINED_HARDENED_CLAY.getDefaultState());
//                        for (int i = (int) height - 1; i > height - 1 - topBlocks.size() * 2 && i > 0; i--)
//                            buffer.setBlock(x, i, z, topBlocks.get(((int) height - 1 - i) / 2));

                    }
                }
            }
        }
    }

    public class TopPops implements GenerationPopulator {

        @Override
        public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeVolume biomes) {
            double height;
            final int water = 30;
            for (int x = buffer.getBlockMin().getX(); x <= buffer.getBlockMax().getX(); x++) {
                for (int z = buffer.getBlockMin().getZ(); z <= buffer.getBlockMax().getZ(); z++) {
                    height = perlinMaps.getAdjustedHeight(x, z);
                    if (world.getBiome(x, 0, z).equals(BiomeTypes.MESA_PLATEAU)) {
                        if (perlinMaps.getSurfaceTexture(x, z, 1) > 0.85)
                            buffer.setBlockType(x, (int) height, z, BlockTypes.DEADBUSH);
                        if (perlinMaps.getSurfaceTexture(x, z, 9) > 0.86)
                            buffer.setBlockType(x, (int) height, z, BlockTypes.GRAVEL);
                        if (perlinMaps.getSurfaceTexture(x, z, 9) > 0.80)
                            buffer.setBlockType(x, (int) height - 1, z, BlockTypes.GRAVEL);
                    }
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
