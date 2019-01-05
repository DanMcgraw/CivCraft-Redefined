package CivcraftRedefined.WorldGen;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;

public class MutableBlockVolumeAdapter implements MutableBlockVolume {
    public final MutableBlockVolume delegate;
    public final int offsetY, minY, maxY;
    public final World world;
    public final PerlinMaps perlinMaps;

    public MutableBlockVolumeAdapter(MutableBlockVolume delegate, int offsetY, int minY, World world, PerlinMaps perlinMaps) {
        if (delegate == null)
            throw new IllegalArgumentException("delegate");

        this.world = world;
        this.perlinMaps = perlinMaps;
        this.delegate = delegate;
        this.offsetY = offsetY;
        this.minY = minY;
        this.maxY = this.delegate.getBlockMax().getY() - offsetY;
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block) {
        BiomeType type = world.getBiome(x, 0, z);
        if (block.equals(BlockTypes.WATER.getDefaultState())) {
            if (y > 64)
                block = BlockTypes.AIR.getDefaultState();
        } else if (block.equals(BlockTypes.AIR.getDefaultState())) {
            if (y <= 64)
                block = BlockTypes.WATER.getDefaultState();
        }
        if (y == 0)
            return this.delegate.setBlock(x, y, z, BlockTypes.BEDROCK.getDefaultState());


        if (type.equals(BiomeTypes.MESA_PLATEAU)) {
            if ((int) (perlinMaps.MESA.getNoiseLevelAtPosition(x, z) * 16) + 208 > y)
                return this.delegate.setBlock(x, y, z, BlockTypes.STONE.getDefaultState());
            else
                return this.delegate.setBlock(x, y, z, BlockTypes.AIR.getDefaultState());

        }
        if (type.equals(BiomeTypes.MESA) || type.equals(BiomeTypes.DESERT_HILLS)) {
            this.delegate.setBlock(x, (int) (Math.max(0, ((y + this.offsetY - 23) * 1.25) + 23)), z, block);
            return this.delegate.setBlock(x, (int) (Math.max(0, ((y + this.offsetY - 23) * 1.25) + 22)), z, block);

        }
        if (y < this.minY || y > this.maxY)
            return false;
        return this.delegate.setBlock(x, ((y + this.offsetY)), z, block);
    }

    @Override
    public MutableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableBlockVolume getBlockView(DiscreteTransform3 transform) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableBlockVolumeWorker<? extends MutableBlockVolume> getBlockWorker() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Vector3i getBlockMin() {
        return this.delegate.getBlockMin();
    }

    @Override
    public Vector3i getBlockMax() {
        return this.delegate.getBlockMax();
    }

    @Override
    public Vector3i getBlockSize() {
        return this.delegate.getBlockSize();
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return this.delegate.containsBlock(x, y, z);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        if (y < this.minY || y > this.maxY)
            return BlockTypes.AIR.getDefaultState();
        return this.delegate.getBlock(x, y + this.offsetY, z);
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        if (y < this.minY || y > this.maxY)
            return BlockTypes.AIR;
        return this.delegate.getBlockType(x, y + this.offsetY, z);
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        throw new UnsupportedOperationException();
    }
}
