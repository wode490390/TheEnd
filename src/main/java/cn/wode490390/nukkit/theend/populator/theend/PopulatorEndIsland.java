package cn.wode490390.nukkit.theend.populator.theend;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.wode490390.nukkit.theend.generator.TheEndGenerator;

public class PopulatorEndIsland extends Populator {

    private final TheEndGenerator end;

    private ChunkManager level;
    private NukkitRandom random;

    public PopulatorEndIsland(TheEndGenerator end) {
        this.end = end;
    }

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        if (Math.pow(chunkX, 2) + Math.pow(chunkZ, 2) <= 4096) {
            return;
        }
        this.random = random;

        if (this.end.getIslandHeight(chunkX, chunkZ) < -20 && this.random.nextBoundedInt(14) == 0) {
            this.level = level;
            int x = chunkX << 4;
            int z = chunkZ << 4;
            this.generate(x, z);

            if (this.random.nextBoundedInt(4) == 0) {
                this.generate(x, z);
            }
        }
    }

    private void generate(int x, int z) {
        x += this.random.nextBoundedInt(16) + 8;
        z += this.random.nextBoundedInt(16) + 8;
        int y = 55 + this.random.nextBoundedInt(16);

        float f = this.random.nextBoundedInt(3) + 4;
        for (int i = 0; f > 0.5; --i) {
            for (int j = (int) Math.floor(-f); j <= Math.ceil(f); ++j) {
                for (int k = (int) Math.floor(-f); k <= Math.ceil(f); ++k) {
                    if (Math.pow(j, 2) + Math.pow(k, 2) <= Math.pow(f + 1, 2)) {
                        this.level.setBlockAt(x + j, y + i, z + k, END_STONE);
                    }
                }
            }
            f -= this.random.nextBoundedInt(2) + 0.5;
        }
    }
}
