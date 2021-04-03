package cn.wode490390.nukkit.theend.scheduler;

import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.IntPositionEntityData;
import cn.nukkit.entity.item.EntityEndCrystal;
import cn.nukkit.entity.mob.EntityEnderDragon;
import cn.nukkit.level.Explosion;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.scheduler.PluginTask;
import cn.wode490390.nukkit.theend.TheEnd;
import cn.wode490390.nukkit.theend.object.theend.ObsidianPillar;
import cn.wode490390.nukkit.theend.populator.theend.PopulatorObsidianPillar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnderDragonResummonAnimationSubTask extends PluginTask<TheEnd> {

    private static final Vector3 DRAGON_RESPAWN_POS = new Vector3(0, 128, 0);
    private static final Vector3 INVALID_BEAM_TARGET = new Vector3(0, 0, 0);

    private final Level level;
    private final List<EntityEndCrystal> respawnCrystals;

    private EnderDragonRespawnAnimation respawnStage = EnderDragonRespawnAnimation.START;
    private int respawnTime = 0;

    private final List<EntityEndCrystal> spikeCrystals = new ArrayList<>(10);

    public EnderDragonResummonAnimationSubTask(Level level, List<EntityEndCrystal> respawnCrystals) {
        super(TheEnd.getInstance());
        this.level = level;
        this.respawnCrystals = respawnCrystals;
    }

    @Override
    public void onRun(int currentTick) {
        if (this.respawnStage != null) {
            this.respawnStage.tick(this.level, this, this.respawnCrystals, this.respawnTime++);
        } else {
            this.cancel();
        }
    }

    public boolean onCrystalDestroyed(EntityEndCrystal crystal) {
        if (this.respawnStage != null && this.respawnCrystals.contains(crystal)) {
            this.respawnStage = null;
            this.respawnTime = 0;
            this.resetSpikeCrystals();
            return true;
        }
        return false;
    }

    private void setRespawnStage(EnderDragonRespawnAnimation stage) {
        this.respawnTime = 0;

        if (stage == EnderDragonRespawnAnimation.END) {
            this.respawnStage = null;

            Entity dragon = Entity.createEntity("EnderDragon", this.level.getChunk(0, 0), Entity.getDefaultNBT(new Vector3(.5,  128, .5)));
            if (dragon != null) {
                this.level.addLevelSoundEvent(dragon, LevelSoundEventPacket.SOUND_SPAWN, -1, EntityEnderDragon.NETWORK_ID);
                dragon.spawnToAll();
            }

            EnderDragonResummonTask.onAnimationEnd(this.level.getId());
        } else {
            this.respawnStage = stage;
        }
    }

    private void resetSpikeCrystals() {
        for (EntityEndCrystal crystal : this.spikeCrystals) {
            setBeamTarget(crystal, null);
        }
        this.spikeCrystals.clear();
    }

    private void emitDragonMadEvent() {
        this.level.addLevelSoundEvent(DRAGON_RESPAWN_POS, LevelSoundEventPacket.SOUND_MAD, -1, EntityEnderDragon.NETWORK_ID);
    }

    private static void setBeamTarget(EntityEndCrystal crystal, Vector3 pos) {
        crystal.setDataProperty(new IntPositionEntityData(Entity.DATA_BLOCK_TARGET, pos == null ? INVALID_BEAM_TARGET : pos));
    }

    enum EnderDragonRespawnAnimation {
        START {
            @Override
            public void tick(Level level, EnderDragonResummonAnimationSubTask animation, List<EntityEndCrystal> respawnCrystals, int respawnTime) {
                for (EntityEndCrystal crystal : respawnCrystals) {
                    setBeamTarget(crystal, DRAGON_RESPAWN_POS);
                }
                animation.setRespawnStage(PREPARING_TO_SUMMON_PILLARS);
            }
        },
        PREPARING_TO_SUMMON_PILLARS {
            @Override
            public void tick(Level level, EnderDragonResummonAnimationSubTask animation, List<EntityEndCrystal> respawnCrystals, int respawnTime) {
                if (respawnTime < 100) {
                    if (respawnTime == 0 || respawnTime == 50 || respawnTime == 51 || respawnTime == 52 || respawnTime >= 95) {
                        animation.emitDragonMadEvent();
                    }
                } else {
                    animation.setRespawnStage(SUMMONING_PILLARS);
                }
            }
        },
        SUMMONING_PILLARS {
            @Override
            public void tick(Level level, EnderDragonResummonAnimationSubTask animation, List<EntityEndCrystal> respawnCrystals, int respawnTime) {
                boolean changeBeamTarget = respawnTime % 40 == 0;
                if (changeBeamTarget || respawnTime % 40 == 39) {
                    List<ObsidianPillar> pillars = Arrays.asList(ObsidianPillar.getObsidianPillars(level.getSeed()));
                    int index = respawnTime / 40;
                    if (index < pillars.size()) {
                        ObsidianPillar pillar = pillars.get(index);
                        if (changeBeamTarget) {
                            for (EntityEndCrystal crystal : respawnCrystals) {
                                setBeamTarget(crystal, new Vector3(pillar.getCenterX(), pillar.getHeight() + 1, pillar.getCenterZ()));
                            }
                        } else {
                            for (int x = pillar.getCenterX() - 10; x <= pillar.getCenterX() + 10; x++) {
                                for (int z = pillar.getCenterZ() - 10; z <= pillar.getCenterZ() + 10; z++) {
                                    for (int y = pillar.getHeight() - 10; y <= pillar.getHeight() + 10; y++) {
                                        level.setBlock(x, y, z, Block.get(Block.AIR), true, true);
                                    }
                                }
                            }
                            Position pos = new Position(pillar.getCenterX() + 0.5, pillar.getHeight(), pillar.getCenterZ() + 0.5, level);

                            Explosion explode = new Explosion(pos, 5, null);
                            explode.explodeA();
                            explode.explodeB();

                            new PopulatorObsidianPillar(pillar).place(level);

                            Entity entity = Entity.createEntity("EndCrystal", pos.getChunk(), Entity.getDefaultNBT(pos.up(1)));
                            if (entity instanceof EntityEndCrystal) {
                                EntityEndCrystal crystal = (EntityEndCrystal) entity;
                                crystal.setShowBase(true);

                                setBeamTarget(crystal, DRAGON_RESPAWN_POS);

                                animation.spikeCrystals.add(crystal);
                            }
                        }
                    } else if (changeBeamTarget) {
                        animation.setRespawnStage(SUMMONING_DRAGON);
                    }
                }
            }
        },
        SUMMONING_DRAGON {
            @Override
            public void tick(Level level, EnderDragonResummonAnimationSubTask animation, List<EntityEndCrystal> respawnCrystals, int respawnTime) {
                if (respawnTime >= 100) {
                    animation.setRespawnStage(END);

                    animation.resetSpikeCrystals();
                    for (EntityEndCrystal crystal : respawnCrystals) {
                        setBeamTarget(crystal, null);
                        crystal.explode();
                    }
                } else if (respawnTime >= 80) {
                    animation.emitDragonMadEvent();
                } else if (respawnTime == 0) {
                    for (EntityEndCrystal crystal : respawnCrystals) {
                        setBeamTarget(crystal, DRAGON_RESPAWN_POS);
                    }
                } else if (respawnTime < 5) {
                    animation.emitDragonMadEvent();
                }
            }
        },
        END {
            @Override
            public void tick(Level level, EnderDragonResummonAnimationSubTask animation, List<EntityEndCrystal> respawnCrystals, int respawnTime) {
                // NOOP
            }
        };

        public abstract void tick(Level level, EnderDragonResummonAnimationSubTask animation, List<EntityEndCrystal> respawnCrystals, int respawnTime);
    }
}
