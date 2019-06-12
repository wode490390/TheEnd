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
            int x = (chunkX << 4) + random.nextBoundedInt(16) + 8;
            int z = (chunkZ << 4) + random.nextBoundedInt(16) + 8;
            int y = this.getHighestWorkableBlock(level, x, z, chunk) + 3 + random.nextBoundedInt(7);

            if (y > 1 && y < 254) {
                for (int i = -2; i <= 2; i++) {
                    for (int j = -1; j <= 1; j++) {
                        for (int k = -1; k <= 1; k++) {
                            int tempX = x + j;
                            int tempY = y + i;
                            int tempZ = z + k;
                            boolean xFlag = tempX == x;
                            boolean yFlag = tempY == y;
                            boolean zFlag = tempZ == z;
                            boolean flag = Math.abs(i) == 2;

                            if (xFlag && yFlag && zFlag) {
                                level.setBlockAt(tempX, tempY, tempZ, END_GATEWAY);
                            } else if (yFlag) {
                                level.setBlockAt(tempX, tempY, tempZ, AIR);
                            } else if (flag && xFlag && zFlag) {
                                level.setBlockAt(tempX, tempY, tempZ, BEDROCK);
                            } else if ((xFlag || zFlag) && !flag) {
                                level.setBlockAt(tempX, tempY, tempZ, BEDROCK);
                            } else {
                                level.setBlockAt(tempX, tempY, tempZ, AIR);
                            }
                        }
                    }
                }
            }
        }
    }
}
