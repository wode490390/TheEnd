package cn.wode490390.nukkit.theend.populator.theend;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.ChunkManager;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.math.NukkitRandom;
import cn.nukkit.math.Vector3;
import cn.wode490390.nukkit.theend.TheEnd;
import cn.wode490390.nukkit.theend.scheduler.ChunkGenerationTask;
import cn.wode490390.nukkit.theend.scheduler.ChunkGenerationTask.ChunkListener;
import cn.wode490390.nukkit.theend.scheduler.EnderDragonSpawnTask;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

public class PopulatorPodium extends Populator implements ChunkListener {

    private Level level;
    private FullChunk baseChunk;

    private final Set<Long> waitingChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicBoolean generated = new AtomicBoolean(false);

    private final boolean activated;
    private final boolean spawnDragon;

    private int y;

    public PopulatorPodium() {
        this(false, false);
    }

    public PopulatorPodium(boolean activated) {
        this(activated, false);
    }

    public PopulatorPodium(boolean activated, boolean spawnDragon) {
        this.activated = activated;
        this.spawnDragon = spawnDragon;
    }

    @Override
    public void populate(ChunkManager level, int chunkX, int chunkZ, NukkitRandom random, FullChunk chunk) {
        if (chunkX >> 4 != 0 || chunkZ >> 4 != 0 || this.generated.get()) {
            return;
        }
        this.y = this.getHighestWorkableBlock(level, 0, 0, chunk);
        if (level.getBlockIdAt(0, this.y, 0) != END_STONE) {
            return;
        }
        level.setBlockAt(0, this.y, 0, BEDROCK);
        this.level = chunk.getProvider().getLevel();
        this.baseChunk = chunk;

        Set<BaseFullChunk> chunks = new HashSet<>();
        for (int x = -1; x < 1; x++) {
            for (int z = -1; z < 1; z++) {
                BaseFullChunk ck = level.getChunk(x, z);
                if (ck == null) {
                    ck = this.level.getChunk(x, z, true);
                }
                if (!ck.isGenerated()) {
                    chunks.add(ck);
                    this.waitingChunks.add(Level.chunkHash(x, z));
                }
            }
        }
        if (!chunks.isEmpty()) {
            chunks.forEach(ck -> Server.getInstance().getScheduler().scheduleAsyncTask(TheEnd.getInstance(), new ChunkGenerationTask(this.level, ck, this)));
            return;
        }

        this.generate();
    }

    @Override
    public void onChunkGenerated(int chunkX, int chunkZ) {
        this.waitingChunks.remove(Level.chunkHash(chunkX, chunkZ));
        if (this.waitingChunks.isEmpty()) {
            this.generate();
        }
    }

    private synchronized void generate() {
        if (!this.generated.getAndSet(true)) {
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
                            } else if (this.activated) {
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

            if (this.spawnDragon && this.baseChunk != null) {
                Server.getInstance().getScheduler().scheduleTask(new EnderDragonSpawnTask(this.baseChunk, Entity.getDefaultNBT(new Vector3(0.5,  Math.min(this.y + 5, 128), 0.5))));
            }
        }
    }
}
