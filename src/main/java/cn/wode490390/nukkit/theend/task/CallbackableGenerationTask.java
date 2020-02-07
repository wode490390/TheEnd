package cn.wode490390.nukkit.theend.task;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.generic.BaseFullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.SimpleChunkManager;
import cn.nukkit.scheduler.AsyncTask;
import cn.wode490390.nukkit.theend.populator.theend.PopulatorPodium;

public class CallbackableGenerationTask extends AsyncTask {

    public boolean state = true;

    private final Level level;
    private BaseFullChunk chunk;

    private PopulatorPodium podium;

    public CallbackableGenerationTask(Level level, BaseFullChunk chunk, PopulatorPodium podium) {
        this.chunk = chunk;
        this.level = level;
        this.podium = podium;
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
    //}

    //@Override
    //public void onCompletion(Server server) {
        if (this.state && this.level != null) {
            if (this.chunk != null) {
                this.podium.generateChunkCallback(this.chunk.getX(), this.chunk.getZ());
            }
        }
    }
}
