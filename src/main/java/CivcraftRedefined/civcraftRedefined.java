package CivcraftRedefined;

import CivcraftRedefined.WorldGen.MapInterpretor;
import CivcraftRedefined.WorldGen.WorldGeneration;
import CivcraftRedefined.functions.MineBlock;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(id = "civcraftredefined", name = "Civcraft Redefined", version = "1.0")
public class civcraftRedefined {

    public static Commands commands;
    static civcraftRedefined instance;
    private static DatabaseCommands databaseCommands;
    private static Map<Location<World>, Integer> oreTypeMap;
    private static Map<UUID, Integer> playerDeathXP;
    private static MapInterpretor mapInterpretor;
    @Inject
    private Logger logger;
    private SpongeExecutorService asyncScheduler = null;
    private SpongeExecutorService syncScheduler = null;

    public static civcraftRedefined getInstance() {
        return instance;
    }

    public static DatabaseCommands getDatabase() {
        return databaseCommands;
    }

    public static Map<Location<World>, Integer> getOreTypeMap() {
        return oreTypeMap;
    }

    public static MapInterpretor getMapInterpretor() {
        return mapInterpretor;
    }

    public Logger getLogger() {
        return logger;
    }

    public PluginContainer getContainer() {
        return Sponge.getPluginManager().fromInstance(this).get();
    }

    @Listener
    public void onInitialize(GameInitializationEvent event) throws SQLException {
        instance = this;

        commands = new Commands();

        mapInterpretor = new MapInterpretor("config/civcraft/map.bmp");
        Sponge.getRegistry().register(WorldGeneratorModifier.class, new WorldGeneration().new SolidWorldGeneratorModifier());
        asyncScheduler = Sponge.getScheduler().createAsyncExecutor(this);
        syncScheduler = Sponge.getScheduler().createSyncExecutor(this);

        this.getLogger().info("Civcraft Redefined has been initialized");
        oreTypeMap = new HashMap<Location<World>, Integer>();
        playerDeathXP = new HashMap<UUID, Integer>();
        databaseCommands = new DatabaseCommands();
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event) {
        World world = event.getTargetWorld();
        logger.info("World loaded: " + world.getName());
        logger.info("Sea level: " + world.getSeaLevel());
        UUID uniqueId = world.getUniqueId();

        if (world.getName().contains("world")) {
            databaseCommands.initOreRevert();
            databaseCommands.initUnBanExpired();
        }
    }

    // @Listener
    // public void onPlayerInteract(InteractEvent e) {
    // this.getLogger().info(e.getContext().toString());
    // }

    @Listener
    public void onBlockPlaceEvent(ChangeBlockEvent.Place e, @Root Player player) {
        if (e.getTransactions().get(0).getFinal().getState().equals(MineBlock.IRONO)) {
            e.setCancelled(true);
            player.sendMessage(Text.of("There is no reason for you to place Iron Ore."));
        }
    }

    @Listener
    public void onBreakBlockEvent(ChangeBlockEvent.Break e, @Root Player player) {
        e.getTransactions().stream().filter(t -> (t.getOriginal().getState().getType().equals(BlockTypes.IRON_ORE))).forEach(t -> {
            BlockState result = null;
            BlockState original = t.getOriginal().getState();
            Location<World> loc = t.getOriginal().getLocation().get();
            String locString = loc.getPosition().toString();
            Task theTask = functions.getTaskByLocCoords(locString);
            int rot = player.getHeadRotation().getFloorY();
            Direction dir = functions.dirFromYaw(rot);
            functions.playerMine(new DataTypes.MineData(2, loc, dir, original, player));
        });
        e.getTransactions().stream().filter(t -> (t.getOriginal().getState().getType().equals(BlockTypes.STONE) || t.getOriginal().getState().getType().equals(BlockTypes.COBBLESTONE))).forEach(t -> {
            BlockState result = null;
            BlockState original = t.getOriginal().getState();
            Location<World> loc = t.getOriginal().getLocation().get();
            String locString = loc.getPosition().toString();
            Task theTask = functions.getTaskByLocCoords(locString);
            int rot = player.getHeadRotation().getFloorY();
            Direction dir = functions.dirFromYaw(rot);

            //functions.playerMine(new DataTypes.MineData(2, loc, dir, original));

            if (original.equals(MineBlock.STONE))
                result = MineBlock.ANDES;
            if (original.equals(MineBlock.ANDES))
                result = MineBlock.COBLE;
            if (original.equals(MineBlock.COBLE) && theTask != null)
                functions.playerMine(new DataTypes.MineData(2, loc, dir, original, player));

            if (theTask != null) {

                theTask.cancel();

            }
            if (original.equals(MineBlock.ANDES) || original.equals(MineBlock.STONE)) {
                Sponge.getScheduler().createTaskBuilder()
                        .name(locString).execute(() -> functions.revertBlock(loc))
                        .delay(4, TimeUnit.SECONDS)
                        .submit(getInstance());
                t.setCustom(

                        BlockSnapshot.builder()
                                .from(t.getFinal().getLocation().get())
                                .blockState(
                                        BlockState.builder().from(result).build()
                                )
                                .build()
                );
            }

        });
    }

    @Listener
    public void onItemDrop(DropItemEvent.Destruct event, @First Player player) {
        event.getCause().first(BlockSnapshot.class).ifPresent(block -> {
            if (block.getState().getType().equals(BlockTypes.STONE)) {
                event.setCancelled(true);
            }
            if (block.getState().getType().equals(BlockTypes.IRON_ORE)) {
                Location<World> loc = block.getLocation().get();
                Item item = functions.createOreItem(block.getState(), functions.findClosest(loc), loc);

                block.getLocation().get().getExtent().spawnEntity(item);
                event.setCancelled(true);
            }
        });
    }

    @Listener
    public void onXPDrop(SpawnEntityEvent event) {
        if (!event.getEntities().isEmpty())
            if (event.getEntities().get(0) instanceof ExperienceOrb) {
                int random = (int) (Math.random() * 100);
                if (random > 100)
                    event.getEntities().clear();
                else
                    event.getEntities().get(0).offer(Keys.CONTAINED_EXPERIENCE, 1);

            }
    }


    @Listener
    public void onMobDrop(@Nonnull DropItemEvent.Destruct event) {
        if (!event.getCause().allOf(Player.class).isEmpty()) {
            Player player = event.getCause().allOf(Player.class).get(0);
            Entity entity = !event.getCause().allOf(Animal.class).isEmpty() ? (event.getCause().allOf(Animal.class).get(0)) : (!event.getCause().allOf(Monster.class).isEmpty() ? (event.getCause().allOf(Monster.class).get(0)) : (null));
            if (event.getCause().after(Player.class).isPresent() && entity != null) {
                Location<World> loc = entity.getLocation();
                player.sendMessage(Text.of("You killed a " + entity.getType().getName()));
                event.getEntities().clear();
                Item item = functions.createMobDrop(entity.getType(), loc);
                if (item != null)
                    event.getEntities().add(item);
            }
        }
    }

//    @Listener
//    public void onMobDie(DestructEntityEvent.Death event){
//        if(functions.typeOfEntity(event.getTargetEntity().getType())==1){
//            this.getLogger().info("die: "+event.getTargetEntity().getContainers().toString());
//        }
//    }

    @Listener
    public void onXPGain(CollideEntityEvent event) {
        event.getEntities().stream().filter(xp -> (xp instanceof ExperienceOrb)).forEach(xp -> {
            xp.remove();
        });
        event.getEntities().removeIf(xp -> (xp instanceof ExperienceOrb));
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event) {
        if (event.getTargetEntity() instanceof Player) {
            Player player = (Player) event.getTargetEntity();
            civcraftRedefined.getInstance().getLogger().info(player.getName() + " just died, thought I'd let you know.");
            int level = player.get(Keys.EXPERIENCE_LEVEL).get();
            UUID id = player.getUniqueId();
            if (level == 0) {
                Timestamp now = new Timestamp(new Date().getTime());
                Timestamp time = new Timestamp(new Date().getTime());
                long duration = ((90) * 60) * 1000;
                time.setTime(time.getTime() + duration);
                databaseCommands.tempBanUser(player, time);
                player.kick(Text.of(TextSerializers.FORMATTING_CODE.deserialize("&9You have been temporarily banned until\n&3" + time + "\n&9for being a loser.\nThats " + DataTypes.timeDifference(now, time))));
            } else {
                playerDeathXP.put(id, --level);
            }

        }
    }

    @Listener
    public void onPlayerConnection(ClientConnectionEvent.Auth event) {
        UUID player = event.getProfile().getUniqueId();
        Timestamp unbanTime = databaseCommands.userBanCheck(player);
        if (unbanTime != null) {
            Timestamp now = new Timestamp(new Date().getTime());
            if (unbanTime.getTime() > now.getTime()) {
                event.setCancelled(true);
                String timeUntil = DataTypes.timeDifference(now, unbanTime);
                event.setMessage(Text.of(TextColors.DARK_BLUE, "You are temporarily banned until\n", TextColors.BLUE, timeUntil));
            } else {
                databaseCommands.unbanPlayer(player);
            }
        }
    }

    @Listener
    public void onPlayerRespawn(RespawnPlayerEvent e) {
        Player player = e.getTargetEntity();
        if (playerDeathXP.get(player.getUniqueId()) != null) {
            int level = playerDeathXP.get(player.getUniqueId());
            player.offer(Keys.EXPERIENCE_LEVEL, level);
            playerDeathXP.remove(player.getUniqueId());
        }

        e.setToTransform(functions.safeRandom(1000, 2000));
        player.sendMessage(Text.of("You start a new life in a random location.", TextColors.DARK_RED, "\n Beware, your level determines how much life you have left. Surpass this and you face a temp ban."));
    }
}
