package cn.wode490390.nukkit.theend.populator.theend;

import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.wode490390.nukkit.theend.generator.TheEndGenerator;

public class PopulatorEndGateway extends Populator {

    private final TheEndGenerator end;

    public PopulatorEndGateway(TheEndGenerator end) {
        this.end = end;
    }

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        if (Math.pow(chunkX, 2) + Math.pow(chunkZ, 2) <= 4096) {
            return;
        }

        if (this.end.getIslandHeight(chunkX, chunkZ) > 40 && random.nextBoundedInt(700) == 0) {
            int baseX = (chunkX << 4) + random.nextBoundedInt(16) + 8;
            int baseZ = (chunkZ << 4) + random.nextBoundedInt(16) + 8;
            int baseY = this.getHighestWorkableBlock(level, baseX, baseZ, chunk) + 3 + random.nextBoundedInt(7);

            if (baseY > 1 && baseY < 254) {
                for (int yOffset = -2; yOffset <= 2; yOffset++) {
                    for (int xOffset = -1; xOffset <= 1; xOffset++) {
                        for (int zOffset = -1; zOffset <= 1; zOffset++) {
                            int x = baseX + xOffset;
                            int y = baseY + yOffset;
                            int z = baseZ + zOffset;
                            boolean xCenter = x == baseX;
                            boolean yCenter = y == baseY;
                            boolean zCenter = z == baseZ;
                            boolean center = Math.abs(yOffset) == 2;

                            if (xCenter && yCenter && zCenter) {
                                level.setBlockAt(x, y, z, END_GATEWAY);
                            } else if (yCenter) {
                                level.setBlockAt(x, y, z, AIR);
                            } else if (center && xCenter && zCenter) {
                                level.setBlockAt(x, y, z, BEDROCK);
                            } else if ((xCenter || zCenter) && !center) {
                                level.setBlockAt(x, y, z, BEDROCK);
                            } else {
                                level.setBlockAt(x, y, z, AIR);
                            }
                        }
                    }
                }
            }
        }
    }
}
