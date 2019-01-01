package CivcraftRedefined;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.nio.ByteBuffer;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

public class DataTypes {
    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    public static String timeDifference(Timestamp small, Timestamp large) {
        long difference = (large.getTime() - small.getTime()) / (60 * 1000);
        if (large.compareTo(small) >= 60)
            return difference / 60 + " hours and " + (difference - (difference / 60) * 60) + " minutes";
        return difference + " minutes";
    }

    public static class MineData {
        int pickLevel;
        Location<World> location;
        Direction dir;
        BlockState ore;
        Player player;

        public MineData(int pickLevel, Location<World> location, Direction dir, BlockState ore, Player player) {
            this.pickLevel = pickLevel;
            this.location = location;
            this.dir = dir;
            this.ore = ore;
            this.player = player;
            //if (location.getBiome().equals(BiomeTypes.DESERT))

        }
    }

    public static class TempBan {
        UUID player;
        Time unban;

        public TempBan(UUID player, Time unban) {
            this.player = player;
            this.unban = unban;
        }
    }
}
