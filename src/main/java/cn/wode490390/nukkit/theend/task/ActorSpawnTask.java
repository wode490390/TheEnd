package cn.wode490390.nukkit.theend.task;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;
import cn.wode490390.nukkit.theend.TheEnd;

public abstract class ActorSpawnTask extends PluginTask<TheEnd> {

    protected final FullChunk chunk;
    protected final CompoundTag nbt;

    public ActorSpawnTask(TheEnd owner, FullChunk chunk, CompoundTag nbt) {
        super(owner);
        this.chunk = chunk;
        this.nbt = nbt;
    }

    @Override
    public void onRun(int currentTick) {
        this.onCreate(Entity.createEntity(this.getType(), this.chunk, this.nbt));
    }

    protected void onCreate(Entity entity) {

    }

    protected abstract String getType();
}
