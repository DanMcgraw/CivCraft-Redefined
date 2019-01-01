package CivcraftRedefined;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

public class LocationSerialization {
    public static String serialize(Location<World> loc) {
        String result = "";
        result += loc.getExtent().getUniqueId().toString() + ",";
        result += loc.getBlockX() + ",";
        result += loc.getBlockY() + ",";
        result += loc.getBlockZ();
        return result;
    }

    public static Location<World> deserialize(String input) {
        String[] inputArray = input.split(",");
        UUID world = UUID.fromString(inputArray[0]);
        if (Sponge.getServer().getWorld(world).isPresent()) {
            Location<World> built =
                    Sponge.getServer().loadWorld(
                            world
                    ).get()
                            .getLocation(new Vector3d(Double.parseDouble(inputArray[1]), Double.parseDouble(inputArray[2]), Double.parseDouble(inputArray[3])));
            return built;

        } else {
            return null;
        }
    }
}
