package cn.wode490390.nukkit.theend.task;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityEndCrystal;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.wode490390.nukkit.theend.TheEnd;

public class EndCrystalSpawnTask extends ActorSpawnTask {

    public EndCrystalSpawnTask(TheEnd owner, FullChunk chunk, CompoundTag nbt) {
        super(owner, chunk, nbt);
    }

    @Override
    protected void onCreate(Entity entity) {
        if (entity instanceof EntityEndCrystal) {
            ((EntityEndCrystal) entity).setShowBase(true);
        }
    }

    @Override
    protected String getType() {
        return String.valueOf(EntityEndCrystal.NETWORK_ID);
    }
}
