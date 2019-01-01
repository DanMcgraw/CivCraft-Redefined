package CivcraftRedefined;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.text.Text;

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

    public Commands() {
        Sponge.getCommandManager().register(civcraftRedefined.getInstance(), unTempBan, "untempban");
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
