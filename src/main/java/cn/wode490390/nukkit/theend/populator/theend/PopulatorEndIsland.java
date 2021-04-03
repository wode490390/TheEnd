package cn.wode490390.nukkit.theend.populator.theend;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.wode490390.nukkit.theend.generator.TheEndGenerator;

public class PopulatorEndIsland extends Populator {

    private final TheEndGenerator end;

    public PopulatorEndIsland(TheEndGenerator end) {
        this.end = end;
    }

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        if (Math.pow(chunkX, 2) + Math.pow(chunkZ, 2) <= 4096) {
            return;
        }

        if (this.end.getIslandHeight(chunkX, chunkZ) < -20 && random.nextBoundedInt(14) == 0) {
            int x = chunkX << 4;
            int z = chunkZ << 4;
            this.generate(level, x, z, random);

            if (random.nextBoundedInt(4) == 0) {
                this.generate(level, x, z, random);
            }
        }
    }

    private void generate(ChunkManager level, int x, int z, NukkitRandom random) {
        x += random.nextBoundedInt(16) + 8;
        z += random.nextBoundedInt(16) + 8;
        int y = 55 + random.nextBoundedInt(16);

        float f = random.nextBoundedInt(3) + 4;
        for (int i = 0; f > 0.5; --i) {
            for (int j = (int) Math.floor(-f); j <= Math.ceil(f); ++j) {
                for (int k = (int) Math.floor(-f); k <= Math.ceil(f); ++k) {
                    if (Math.pow(j, 2) + Math.pow(k, 2) <= Math.pow(f + 1, 2)) {
                        level.setBlockAt(x + j, y + i, z + k, END_STONE);
                    }
                }
            }
            f -= random.nextBoundedInt(2) + 0.5;
        }
    }
}
