package cn.wode490390.nukkit.theend.task;

import cn.nukkit.entity.mob.EntityEnderDragon;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.wode490390.nukkit.theend.TheEnd;

public class EnderDragonSpawnTask extends ActorSpawnTask {

    public EnderDragonSpawnTask(TheEnd owner, FullChunk chunk, CompoundTag nbt) {
        super(owner, chunk, nbt);
    }

    @Override
    protected String getType() {
        return String.valueOf(EntityEnderDragon.NETWORK_ID);
    }
}
