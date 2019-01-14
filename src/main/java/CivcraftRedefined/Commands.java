package CivcraftRedefined;

import CivcraftRedefined.WorldGen.VillageGen.StructGenrat;
import CivcraftRedefined.WorldGen.VillageGen.StructIntrp;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Commands {
    CommandSpec unTempBan = CommandSpec.builder()
            .description(Text.of("Unbans a tempbanned player"))
            .permission("civcraft.command.untempban")

            .arguments(
                    GenericArguments.onlyOne(GenericArguments.string(Text.of("player name"))))

            .executor((CommandSource src, CommandContext args) -> {

                String playerName = args.<String>getOne("player name").get();

                civcraftRedefined.getDatabase().unbanPlayer(getUser(playerName));

                return CommandResult.success();
            })
            .build();

    CommandSpec createStructure = CommandSpec.builder()
            .description(Text.of("Creates the structure nearby"))
            .permission("civcraft.command.struct")

            .arguments(
                    GenericArguments.optional(GenericArguments.string(Text.of("biome"))))

            .executor((CommandSource src, CommandContext args) -> {

                String playerName;

                if (args.getOne("biome").isPresent())
                    playerName = args.<String>getOne("biome").get();

                if (src instanceof Player) {
                    Player player = (Player) src;
                    Location<World> location = player.getLocation();

                    location.add(new Vector3i(20, 0, 0));
                    Vector3i loc = location.getPosition().toInt();
                    StructGenrat structGenrat = new StructGenrat(player.getWorld());
                    StructIntrp.Structure struct = structGenrat.createStructure(0, 0, 0);
                    for (int x = 0; x < struct.wHL.getX(); x++)
                        for (int y = 0; y < struct.wHL.getY(); y++)
                            for (int z = 0; z < struct.wHL.getZ(); z++)
                                if (struct.blockData[x][y][z] != null)
                                    player.getWorld().setBlock(loc.getX() + x, loc.getY() + y, loc.getZ() + z, struct.blockData[x][y][z]);
                }

                //civcraftRedefined.getDatabase().unbanPlayer(getUser(playerName));


                return CommandResult.success();
            })
            .build();

    CommandSpec biomeStats = CommandSpec.builder()
            .description(Text.of("View information about biome"))
            .permission("civcraft.command.biomeinfo")

            .arguments(
                    GenericArguments.optional(GenericArguments.string(Text.of("biome"))))

            .executor((CommandSource src, CommandContext args) -> {

                String playerName;

                if (args.getOne("biome").isPresent())
                    playerName = args.<String>getOne("biome").get();

                if (src instanceof Player) {
                    Player player = (Player) src;
                    player.getLocation().getExtent().getProperties().getSeed();
                    player.sendMessage(Text.of("Current Biome:    " + player.getLocation().getBiome().getName()));
                    player.sendMessage(Text.of("Current Temp:     " + player.getLocation().getExtent().getProperties(player.getLocation().getPosition().toInt()).toString()));
                    player.sendMessage(Text.of("Current Humidity: " + player.getLocation().getBiome().getHumidity()));
                }

                //civcraftRedefined.getDatabase().unbanPlayer(getUser(playerName));


                return CommandResult.success();
            })
            .build();

    public Commands() {
        Sponge.getCommandManager().register(civcraftRedefined.getInstance(), unTempBan, "untempban");
        Sponge.getCommandManager().register(civcraftRedefined.getInstance(), biomeStats, "bio");
        Sponge.getCommandManager().register(civcraftRedefined.getInstance(), createStructure, "struct");
    }

    public UUID getUser(String name) {
        GameProfileManager profileManager = Sponge.getServer().getGameProfileManager();
        CompletableFuture<GameProfile> futureGameProfile = profileManager.get(name);
        try {
            GameProfile profile = futureGameProfile.get();
            return profile.getUniqueId();
        } catch (Exception e) {
            return null;
            // this part is executed when an exception (in this example InterruptedException) occurs
        }
    }
//Sponge.getCommandManager().register(plugin, myCommandSpec, "message", "msg", "m");
}
