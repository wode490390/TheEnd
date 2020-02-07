package cn.wode490390.nukkit.theend.populator.theend;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.wode490390.nukkit.theend.TheEnd;
import cn.wode490390.nukkit.theend.object.theend.ObsidianPillar;
import cn.wode490390.nukkit.theend.populator.PopulatorBlock;
import cn.wode490390.nukkit.theend.task.EndCrystalSpawnTask;

public class PopulatorObsidianPillar extends PopulatorBlock {

    private final ObsidianPillar obsidianPillar;

    public PopulatorObsidianPillar(ObsidianPillar obsidianPillar) {
        this.obsidianPillar = obsidianPillar;
    }

    @Override
    public void decorate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        int x = this.obsidianPillar.getCenterX();
        int z = this.obsidianPillar.getCenterZ();
        if (x >> 4 != chunkX || z >> 4 != chunkZ) {
            return;
        }

        int height = this.obsidianPillar.getHeight();
        int radius = this.obsidianPillar.getRadius();

        for (int i = 0; i < height; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = -radius; k <= radius; k++) {
                    if (j * j + k * k <= radius * radius + 1) {
                        level.setBlockAt(x + j, i, z + k, OBSIDIAN);
                    }
                }
            }
        }

        if (this.obsidianPillar.isGuarded()) {
            for (int i = -2; i <= 2; ++i) {
                for (int j = -2; j <= 2; ++j) {
                    if (Math.abs(i) == 2 || Math.abs(j) == 2) {
                        for (int k = 0; k < 3; ++k) {
                            level.setBlockAt(x + i, height + k, z + j, IRON_BARS);
                        }
                    }
                    level.setBlockAt(x + i, height + 3, z + j, IRON_BARS);
                }
            }
        }

        level.setBlockAt(x, height, z, BEDROCK);
        level.setBlockAt(x, height + 1, z, FIRE);

        Server.getInstance().getScheduler().scheduleTask(new EndCrystalSpawnTask(TheEnd.getInstance(), chunk, Entity.getDefaultNBT(new Vector3(x + 0.5,  height + 1, z + 0.5))));
    }
}
