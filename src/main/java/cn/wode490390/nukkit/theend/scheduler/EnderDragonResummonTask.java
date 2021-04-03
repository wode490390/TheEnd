package cn.wode490390.nukkit.theend.scheduler;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityEndCrystal;
import cn.nukkit.entity.mob.EntityEnderDragon;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.PluginTask;
import cn.wode490390.nukkit.theend.TheEnd;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Arrays;
import java.util.Map;

public class EnderDragonResummonTask extends PluginTask<TheEnd> {

    private static final IntSet taskQueue = new IntOpenHashSet();
    private static final Int2ObjectMap<EnderDragonResummonAnimationSubTask> animationTasks = new Int2ObjectOpenHashMap<>();

    private final Level level;

    private EnderDragonResummonTask(Level level) {
        super(TheEnd.getInstance());
        this.level = level;
    }

    @Override
    public void onRun(int currentTick) {
        taskQueue.remove(this.level.getId());

        for (Entity entity : this.level.getEntities()) {
            if (entity.getNetworkId() == EntityEnderDragon.NETWORK_ID && entity.isAlive() && !entity.isClosed()) {
                return;
            }
        }

        Int2ObjectMap<EntityEndCrystal[]> entities = new Int2ObjectOpenHashMap<>();

        Map<Long, Entity> entities10 = this. level.getChunkEntities(-1, 0, true);
        for (Entity entity : entities10.values()) {
            if (entity instanceof EntityEndCrystal && entity.isAlive() && !entity.isClosed()) {
                if (entity.getX() == -3 + .5) {
                    int y = entity.getFloorY();
                    if (y == entity.getY()) {
                        EntityEndCrystal[] crystals = entities.get(y);
                        if (crystals == null) {
                            crystals = new EntityEndCrystal[4];
                            entities.put(y, crystals);
                        }
                        if (crystals[0] == null) {
                            crystals[0] = (EntityEndCrystal) entity;
                        }
                    }
                }
            }
        }

        Map<Long, Entity> entities01 = this.level.getChunkEntities(0, -1, true);
        for (Entity entity : entities01.values()) {
            if (entity.getNetworkId() == EntityEndCrystal.NETWORK_ID && entity.isAlive() && !entity.isClosed()) {
                if (entity.getZ() == -3 + .5) {
                    int y = entity.getFloorY();
                    if (y == entity.getY()) {
                        EntityEndCrystal[] crystals = entities.get(y);
                        if (crystals == null) {
                            continue;
                        }
                        if (crystals[1] == null) {
                            crystals[1] = (EntityEndCrystal) entity;
                        }
                    }
                }
            }
        }

        Map<Long, Entity> entities00 = this.level.getChunkEntities(0, 0, true);
        for (Entity entity : entities00.values()) {
            if (entity.getNetworkId() == EntityEndCrystal.NETWORK_ID && entity.isAlive() && !entity.isClosed()) {
                if (entity.getX() == 3 + .5) {
                    int y = entity.getFloorY();
                    if (y == entity.getY()) {
                        EntityEndCrystal[] crystals = entities.get(y);
                        if (crystals == null) {
                            continue;
                        }
                        if (crystals[2] == null) {
                            crystals[2] = (EntityEndCrystal) entity;
                            if (crystals[3] != null) {
                                break;
                            }
                        }
                    }
                } else if (entity.getZ() == 3 + .5) {
                    int y = entity.getFloorY();
                    if (y == entity.getY()) {
                        EntityEndCrystal[] crystals = entities.get(y);
                        if (crystals == null) {
                            continue;
                        }
                        if (crystals[3] == null) {
                            crystals[3] = (EntityEndCrystal) entity;
                            if (crystals[2] != null) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        entities.int2ObjectEntrySet().stream().filter(entry -> {
            EntityEndCrystal[] crystals = entry.getValue();
            for (int i = 0; i < 4; i++) {
                if (crystals[i] == null) {
                    return false;
                }
            }
            return true;
        }).max(Map.Entry.comparingByKey()).ifPresent(entry -> {
            EnderDragonResummonAnimationSubTask animation = new EnderDragonResummonAnimationSubTask(this.level, Arrays.asList(entry.getValue()));
            this.level.getServer().getScheduler().scheduleRepeatingTask(TheEnd.getInstance(), animation, 1);
            animationTasks.put(this.level.getId(), animation);
        });
    }

    static void onAnimationEnd(int levelId) {
        animationTasks.remove(levelId);
    }

    public static void onCrystalDestroyed(EntityEndCrystal crystal) {
        int levelId = crystal.getLevel().getId();
        EnderDragonResummonAnimationSubTask animation = animationTasks.get(levelId);
        if (animation != null && animation.onCrystalDestroyed(crystal)) {
            animationTasks.remove(levelId);
        }
    }

    public static void tryRespawn(Level level) {
        int levelId = level.getId();
        if (!animationTasks.containsKey(levelId) && taskQueue.add(levelId)) {
            level.getServer().getScheduler().scheduleDelayedTask(TheEnd.getInstance(), new EnderDragonResummonTask(level), 1);
        }
    }
}
