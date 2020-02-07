package cn.wode490390.nukkit.theend.task;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityEndCrystal;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.PluginTask;
import cn.wode490390.nukkit.theend.TheEnd;

public class EndCrystalSpawnTask extends PluginTask<TheEnd> {

    private final FullChunk chunk;
    private final CompoundTag nbt;

    public EndCrystalSpawnTask(TheEnd owner, FullChunk chunk, CompoundTag nbt) {
        super(owner);
        this.chunk = chunk;
        this.nbt = nbt;
    }

    @Override
    public void onRun(int currentTick) {
        EntityEndCrystal crystal = (EntityEndCrystal) Entity.createEntity(String.valueOf(EntityEndCrystal.NETWORK_ID), this.chunk, this.nbt);
        if (crystal != null) {
            crystal.setShowBase(true);
        }
    }
}
