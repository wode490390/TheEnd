package cn.wode490390.nukkit.theend.scheduler;

import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EnderDragonSpawnTask extends EntitySpawnTask {

    public EnderDragonSpawnTask(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected String getType() {
        return "EnderDragon";
    }
}
