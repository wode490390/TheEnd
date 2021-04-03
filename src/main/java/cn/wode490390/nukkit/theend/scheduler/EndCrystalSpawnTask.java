package cn.wode490390.nukkit.theend.scheduler;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityEndCrystal;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EndCrystalSpawnTask extends EntitySpawnTask {

    public EndCrystalSpawnTask(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void onCreated(Entity entity) {
        if (entity instanceof EntityEndCrystal) {
            ((EntityEndCrystal) entity).setShowBase(true);
        }
    }

    @Override
    protected String getType() {
        return "EndCrystal";
    }
}
