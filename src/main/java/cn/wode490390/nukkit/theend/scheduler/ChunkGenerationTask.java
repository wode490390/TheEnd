package cn.wode490390.nukkit.theend.scheduler;

import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.SimpleChunkManager;
import cn.nukkit.scheduler.AsyncTask;

public class ChunkGenerationTask extends AsyncTask {

    public boolean state = true;

    private final Level level;
    private BaseFullChunk chunk;

    private final ChunkListener callback;

    public ChunkGenerationTask(Level level, BaseFullChunk chunk, ChunkListener callback) {
        this.chunk = chunk;
        this.level = level;
        this.callback = callback;
    }

    @Override
    public void onRun() {
        this.state = false;
        Generator generator = this.level.getGenerator();
        if (generator != null) {
            SimpleChunkManager manager = (SimpleChunkManager) generator.getChunkManager();
            if (manager != null) {
                manager.cleanChunks(this.level.getSeed());
                synchronized (manager) {
                    try {
                        BaseFullChunk chunk = this.chunk;
                        if (chunk != null) {
                            synchronized (chunk) {
                                if (!chunk.isGenerated()) {
                                    manager.setChunk(chunk.getX(), chunk.getZ(), chunk);
                                    generator.generateChunk(chunk.getX(), chunk.getZ());
                                    chunk = manager.getChunk(chunk.getX(), chunk.getZ());
                                    chunk.setGenerated();
                                }
                            }
                            this.chunk = chunk;
                            this.state = true;
                        }
                    } finally {
                        manager.cleanChunks(this.level.getSeed());
                    }
                }
            }
        }

        if (this.state && this.chunk != null && this.callback != null) {
            this.callback.onChunkGenerated(this.chunk.getX(), this.chunk.getZ());
        }
    }

    public interface ChunkListener {

        void onChunkGenerated(int chunkX, int chunkZ);
    }
}
