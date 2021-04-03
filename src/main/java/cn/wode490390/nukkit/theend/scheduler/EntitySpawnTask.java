package cn.wode490390.nukkit.theend.scheduler;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;
import cn.wode490390.nukkit.theend.TheEnd;

public abstract class EntitySpawnTask extends PluginTask<TheEnd> {

    protected final FullChunk chunk;
    protected final CompoundTag nbt;

    public EntitySpawnTask(FullChunk chunk, CompoundTag nbt) {
        super(TheEnd.getInstance());
        this.chunk = chunk;
        this.nbt = nbt;
    }

    @Override
    public void onRun(int currentTick) {
        Entity entity = Entity.createEntity(this.getType(), this.chunk, this.nbt);

        this.onCreated(entity);

        if (entity != null) {
            entity.spawnToAll();
        }
    }

    protected void onCreated(Entity entity) {

    }

    protected abstract String getType();
}
