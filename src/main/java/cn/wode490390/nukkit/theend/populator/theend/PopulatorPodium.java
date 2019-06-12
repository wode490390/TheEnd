package cn.wode490390.nukkit.theend.populator.theend;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;

public class PopulatorPodium extends Populator {

    private boolean actived;

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

        for (int i = -1; i <= 32; i++) {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    int dy = y + i;
                    double distance = new Vector3(0, y).distance(new Vector3(x, dy, z));
                    if (distance <= 3.5) {
                        if (dy < y) {
                            if (distance <= 2.5) {
                                level.setBlockAt(x, dy, z, BEDROCK);
                            } else if (dy < y) {
                                level.setBlockAt(x, dy, z, END_STONE);
                            }
                        } else if (dy > y) {
                            level.setBlockAt(x, dy, z, AIR);
                        } else if (distance > 2.5) {
                            level.setBlockAt(x, dy, z, BEDROCK);
                        } else if (this.actived) {
                            level.setBlockAt(x, dy, z, END_PORTAL);
                        } else {
                            level.setBlockAt(x, dy, z, AIR);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 4; ++i) {
            level.setBlockAt(0, y + i, 0, BEDROCK);
        }

        int torch = y + 2;
        level.setBlockAt(1, torch, 0, TORCH, 1);
        level.setBlockAt(-1, torch, 0, TORCH, 2);
        level.setBlockAt(0, torch, 1, TORCH, 3);
        level.setBlockAt(0, torch, -1, TORCH, 4);
    }
}
