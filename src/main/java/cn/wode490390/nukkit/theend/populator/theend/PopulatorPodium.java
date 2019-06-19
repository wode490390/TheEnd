package cn.wode490390.nukkit.theend.populator.theend;

import cn.nukkit.Server;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.ServerScheduler;
import cn.wode490390.nukkit.theend.task.GenerationTask;

public class PopulatorPodium extends Populator {

    private Level level;
    private boolean zero_negativeOne = false;
    private boolean negativeOne_negativeOne = false;
    private boolean negativeOne_zero = false;
    private boolean generated = false;

    private boolean actived;
    private int y;

    public PopulatorPodium() {
        this(false);
    }

    public PopulatorPodium(boolean actived) {
        this.actived = actived;
    }

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        if (chunkX >> 4 != 0 || chunkZ >> 4 != 0) {
            return;
        }
        int y = this.getHighestWorkableBlock(level, 0, 0, chunk);
        if (level.getBlockIdAt(0, y, 0) != END_STONE) {
            return;
        }
        level.setBlockAt(0, y, 0, BEDROCK);
        this.level = chunk.getProvider().getLevel();
        this.y = y;

        ServerScheduler scheduler = Server.getInstance().getScheduler();
        BaseFullChunk zero_negativeOne = this.level.getChunk(0, -1, true);
        if (!zero_negativeOne.isGenerated()) {
            scheduler.scheduleAsyncTask(null, new GenerationTask(this.level, zero_negativeOne, this));
        } else {
            this.zero_negativeOne = true;
        }
        BaseFullChunk negativeOne_negativeOne = this.level.getChunk(-1, -1, true);
        if (!negativeOne_negativeOne.isGenerated()) {
            scheduler.scheduleAsyncTask(null, new GenerationTask(this.level, negativeOne_negativeOne, this));
        } else {
            this.negativeOne_negativeOne = true;
        }
        BaseFullChunk negativeOne_zero = this.level.getChunk(-1, 0, true);
        if (!negativeOne_zero.isGenerated()) {
            scheduler.scheduleAsyncTask(null, new GenerationTask(this.level, negativeOne_zero, this));
        } else {
            this.negativeOne_zero = true;
        }
        this.generate();
    }

    public void generateChunkCallback(int chunkX, int chunkZ) {
        if (chunkX == 0 && chunkZ == -1) {
            this.zero_negativeOne = true;
        } else if (chunkX == -1 && chunkZ == -1) {
            this.negativeOne_negativeOne = true;
        } else if (chunkX == -1 && chunkZ == 0) {
            this.negativeOne_zero = true;
        }
        this.generate();
    }

    private synchronized void generate() {
        if (this.zero_negativeOne && this.negativeOne_negativeOne && this.negativeOne_zero && !this.generated) {
            this.generated = true;
            for (int i = -1; i <= 32; i++) {
                for (int x = -4; x <= 4; x++) {
                    for (int z = -4; z <= 4; z++) {
                        int dy = this.y + i;
                        double distance = new Vector3(0, this.y).distance(new Vector3(x, dy, z));
                        if (distance <= 3.5) {
                            if (dy < this.y) {
                                if (distance <= 2.5) {
                                    this.level.setBlockAt(x, dy, z, BEDROCK);
                                } else if (dy < this.y) {
                                    this.level.setBlockAt(x, dy, z, END_STONE);
                                }
                            } else if (dy > this.y) {
                                this.level.setBlockAt(x, dy, z, AIR);
                            } else if (distance > 2.5) {
                                this.level.setBlockAt(x, dy, z, BEDROCK);
                            } else if (this.actived) {
                                this.level.setBlockAt(x, dy, z, END_PORTAL);
                            } else {
                                this.level.setBlockAt(x, dy, z, AIR);
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < 4; ++i) {
                this.level.setBlockAt(0, this.y + i, 0, BEDROCK);
            }
            int torch = this.y + 2;
            this.level.setBlockAt(1, torch, 0, TORCH, 1);
            this.level.setBlockAt(-1, torch, 0, TORCH, 2);
            this.level.setBlockAt(0, torch, 1, TORCH, 3);
            this.level.setBlockAt(0, torch, -1, TORCH, 4);
        }
    }
}
