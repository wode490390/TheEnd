package cn.wode490390.nukkit.theend.populator.theend;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.wode490390.nukkit.theend.generator.TheEndGenerator;

public class PopulatorChorusPlant extends Populator {

    private final TheEndGenerator end;

    public PopulatorChorusPlant(TheEndGenerator end) {
        this.end = end;
    }

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        if (Math.pow(chunkX, 2) + Math.pow(chunkZ, 2) <= 4096) {
            return;
        }

        if (this.end.getIslandHeight(chunkX, chunkZ) > 40) {
            for (int i = 0; i < random.nextBoundedInt(5); ++i) {
                int x = (chunkX << 4) + random.nextBoundedInt(16) + 8;
                int z = (chunkZ << 4) + random.nextBoundedInt(16) + 8;
                int y = this.getHighestWorkableBlock(level, x, z, chunk);

                if (y > 0 && level.getBlockIdAt(x, y, z) == AIR && level.getBlockIdAt(x, y - 1, z) == END_STONE) {
                    this.generate(level, x, y, z, 8, random);
                }
            }
        }
    }

    private void generate(ChunkManager level, int x, int y, int z, int maxDistance, NukkitRandom random) {
        level.setBlockAt(x, y, z, CHORUS_PLANT);
        this.grow(level, x, y, z, x, y, z, maxDistance, 0, random);
    }

    private boolean canGrow(ChunkManager level, int x, int y, int z, int face) {
        if (face != 0 && level.getBlockIdAt(x - 1, y, z) != AIR) {
            return false;
        }
        if (face != 1 && level.getBlockIdAt(x + 1, y, z) != AIR) {
            return false;
        }
        if (face != 2 && level.getBlockIdAt(x, y, z - 1) != AIR) {
            return false;
        }
        return !(face != 3 && level.getBlockIdAt(x, y, z + 1) != AIR);
    }

    private void grow(ChunkManager level, int targetX, int targetY, int targetZ, int sourceX, int sourceY, int sourceZ, int maxDistance, int age, NukkitRandom random) {
        int height = random.nextBoundedInt(4) + 1;

        if (age == 0) {
            ++height;
        }

        for (int i = 0; i < height; ++i) {
            int y = targetY + i + 1;

            if (!this.canGrow(level, targetX, y, targetZ, -1)) {
                return;
            }

            level.setBlockAt(targetX, y, targetZ, CHORUS_PLANT);
        }

        boolean unripe = false;

        if (age < 4) {
            int h = random.nextBoundedInt(4);

            if (age == 0) {
                ++h;
            }

            for (int i = 0; i < h; ++i) {
                int x = targetX;
                int z = targetZ;
                int face = random.nextBoundedInt(3);
                switch (face) {
                    case 0:
                        x += 1;
                        break;
                    case 1:
                        x -= 1;
                        break;
                    case 2:
                        z += 1;
                        break;
                    case 3:
                        z -= 1;
                        break;
                }
                int y = targetY + height;

                if (Math.abs(x - sourceX) < maxDistance && Math.abs(z - sourceZ) < maxDistance && level.getBlockIdAt(x, y, z) == AIR
                        && level.getBlockIdAt(x, y - 1, z) == AIR && canGrow(level, x, y, z, face)) {
                    unripe = true;
                    level.setBlockAt(x, y, z, CHORUS_PLANT);
                    this.grow(level, x, y, z, sourceX, sourceY, sourceZ, maxDistance, age + 1, random);
                }
            }
        }

        if (!unripe) {
            level.setBlockAt(targetX, targetY + height, targetZ, CHORUS_FLOWER, 5);
        }
    }
}
