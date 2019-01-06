package CivcraftRedefined;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.*;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class functions {

    public static void playerMine(DataTypes.MineData mdata) {
        if (!mdata.ore.equals(MineBlock.COBLE)) {


        } else {
            int chance = (int) (Math.random() * 10000);
            BlockState result = blockChance(chance);
            if (result != null) {
                mdata.player.sendTitle(Title.of(Text.of(TextColors.GOLD, "Impure Iron Found")));
                generateOreCluster(result, 2, 1, mdata.location.getBlockRelative(mdata.dir).getBlockRelative(mdata.dir));

            }
        }
    }

    public static Item createOreItem(BlockState ore, int type, Location<World> loc) {
        ItemStack stack = ItemStack.builder().itemType(ItemTypes.IRON_ORE).build();
        stack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.GRAY, "Impure Iron Ore"));
        List<Text> lore = new ArrayList<Text>();
        lore.add(Text.of("The roughest, lowest quality Iron Ore."));
        lore.add(Text.of("Yields 1-2 Iron Nuggets by default."));
        stack.offer(Keys.ITEM_LORE, lore);
        Entity optItem = loc.getExtent().createEntity(EntityTypes.ITEM, loc.getPosition());
        Item item = (Item) optItem;
        item.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
        return item;
    }

    public static Item createMobDrop(EntityType entityType, Location<World> loc) {
        Class<?> type = entityType.getEntityClass();
        if (Animal.class.isAssignableFrom(type)) {
            ItemStack stack = ItemStack.builder().itemType(ItemTypes.BEEF).build();
            stack.offer(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Uncooked Meat"));
            List<Text> lore = new ArrayList<Text>();
            lore.add(Text.of("A chance at having a disease."));
            stack.offer(Keys.ITEM_LORE, lore);
            Entity optItem = loc.getExtent().createEntity(EntityTypes.ITEM, loc.getPosition());
            Item item = (Item) optItem;
            item.offer(Keys.REPRESENTED_ITEM, stack.createSnapshot());
            return item;
        }
        return null;
    }

    public static Point2D locToPoint(Location<World> loc) {
        double x, y;
        x = loc.getX();
        y = loc.getZ();
        return new Point2D.Double(x, y);
    }

    public static Direction dirFromYaw(int yaw) {
        yaw += 45;
        yaw = (yaw > 360) ? yaw - 360 : yaw;
        yaw /= 90;

        switch (yaw) {
            case 0:
                return Direction.SOUTH;
            case 1:
                return Direction.WEST;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.EAST;
        }

        return Direction.DOWN;
    }

    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    private static double lengthSq(double x, double z) {
        return (x * x) + (z * z);
    }

    public static void generateOreCluster(BlockState ore, int type, int radius, Location<World> loc) {
        double nextXn = 0;
        int chance;

        civcraftRedefined.getOreTypeMap().put(loc, type);
        Sponge.getScheduler().createTaskBuilder()
                .name(loc.getPosition().toString()).execute(() -> functions.removeOreTypeMapping(loc))
                .delay(25, TimeUnit.SECONDS)
                .submit(civcraftRedefined.getInstance());

        forX:
        for (int x = 0; x <= radius; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) / radius;
            chance = ((x == 2) ? 30 : ((x < 1) ? 80 : 50));
            double nextYn = 0;
            forY:
            for (int y = 0; y <= radius; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) / radius;
                double nextZn = 0;
                forZ:
                for (int z = 0; z <= radius; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) / radius;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    //if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                    //    continue;
                    //}

                    tryOre(loc.add(x, y, z), ore, chance);
                    tryOre(loc.add(-x, y, z), ore, chance);
                    tryOre(loc.add(x, -y, z), ore, chance);
                    tryOre(loc.add(x, y, -z), ore, chance);
                    tryOre(loc.add(-x, -y, z), ore, chance);
                    tryOre(loc.add(x, -y, -z), ore, chance);
                    tryOre(loc.add(-x, y, -z), ore, chance);
                    tryOre(loc.add(-x, -y, -z), ore, chance);
                }
            }
        }

    }

    //TODO: implement distance squared
    public static int findClosest(Location<World> miningLocation) {
        Location<World> closest = null;
        double closestDistance = 0;

        Set<Location<World>> keys = civcraftRedefined.getOreTypeMap().keySet();
        Object[] locations = keys.toArray();

        for (Object location : locations) {
            double distance = ((Location<World>) location).getPosition().distance(miningLocation.getPosition());
            if (closest == null || distance < closestDistance) {
                closest = (Location<World>) location;
                closestDistance = distance;
            }
        }
        if (closestDistance > 15)
            return -1;
        if (closest == null)
            return -1;
        return civcraftRedefined.getOreTypeMap().get(closest);
    }

    public static void removeOreTypeMapping(Location<World> location) {
        civcraftRedefined.getOreTypeMap().remove(location);
    }

    public static void tryOre(Location<World> loc, BlockState ore, int chance) {
        if (Math.random() * 500 < chance)
            if (loc.getBlock().equals(MineBlock.STONE)) {
                loc.setBlock(ore);
                addOreToMapping(loc);
                Sponge.getScheduler().createTaskBuilder()
                        .name(loc.getPosition().toString()).execute(() -> functions.revertBlock(loc))
                        .delay(25, TimeUnit.SECONDS)
                        .submit(civcraftRedefined.getInstance());
            }
    }

    public static void addOreToMapping(Location<World> loc) {
        civcraftRedefined.getDatabase().storeOreMapping(loc);
    }

    private static BlockState blockChance(int chance) {
        if (chance < 5000)
            return oreTypes(MineBlock.IRONO, 3);
        return null;
    }

    public static BlockState oreTypes(BlockState ore, int strength) {

        if (ore.equals(MineBlock.IRONO)) {


            return ore;
        }
        return MineBlock.STONE;
    }

    public static Task getTaskByLocCoords(String vector) {
        for (Task task : (Sponge.getScheduler().getScheduledTasks(civcraftRedefined.getInstance()))) {
            if (task.getName().contains(vector))
                return task;
        }
        return null;
    }

    public static void revertBlock(Location<World> loc) {
        if (loc != null)
            for (BlockState blockState : MineBlock.blockStates) {
                if (loc.getBlock().equals(blockState)) {

                    civcraftRedefined.getDatabase().removeOreMapping(loc);
                    loc.setBlock(MineBlock.STONE);
                }
            }
    }

    public static Transform safeRandom(int radiusInner, int radiusOuter) {
        Transform result = null;
        double angle = Math.random() * Math.PI * 2;
        double distance = (1 - Math.abs(Math.random() - Math.random())) * (radiusOuter - radiusInner);
        distance += radiusInner;

        Optional<Location<World>> resultLoc = Sponge.getGame().getTeleportHelper().getSafeLocation(new Location<World>(
                Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get(), (int) (distance * Math.cos(angle)), 128, (int) (distance * Math.sin(angle))), 240, 10);
        if (resultLoc.isPresent()) {
            result = new Transform<World>(resultLoc.get());
            if (resultLoc.get().getBlockRelative(Direction.DOWN).getBlockRelative(Direction.DOWN).getBlockType().equals(BlockTypes.WATER))
                return safeRandom(radiusInner, radiusOuter);
        } else
            return safeRandom(radiusInner, radiusOuter);
        return result;
    }

    public Collection<Entity> getEntities(final Player player, final int radius) {
        return player.getLocation().getExtent().getEntities(new Predicate<Entity>() {
            @Override
            public boolean test(Entity entity) {
                return entity.getLocation().getPosition().distance(player.getLocation().getPosition()) <= radius;
            }
        });
    }

    public static final class MineBlock {
        public static final BlockState STONE = ((BlockState) BlockTypes.STONE.getAllBlockStates().toArray()[0]);
        public static final BlockState ANDES = ((BlockState) BlockTypes.STONE.getAllBlockStates().toArray()[5]);
        public static final BlockState COBLE = ((BlockState) BlockTypes.COBBLESTONE.getAllBlockStates().toArray()[0]);
        public static final BlockState IRONO = ((BlockState) BlockTypes.IRON_ORE.getAllBlockStates().toArray()[0]);
        public static final BlockState[] blockStates = new BlockState[]{STONE, ANDES, COBLE, IRONO};
    }

}
