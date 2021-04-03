package cn.wode490390.nukkit.theend.listener;

import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityEndCrystal;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDespawnEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.wode490390.nukkit.theend.TheEnd;
import cn.wode490390.nukkit.theend.scheduler.EnderDragonResummonTask;

public class ResummonListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.getItem().getId() == Item.END_CRYSTAL) {
            Block block = event.getBlock();
            int x = Math.abs(block.getFloorX());
            int z = Math.abs(block.getFloorZ());
            if (block.getId() == Block.BEDROCK && (x == 3 && z == 0 || x == 0 && z == 3)) {
                Level level = block.getLevel();
                if (level.getFolderName().equalsIgnoreCase(TheEnd.THE_END_LEVEL_NAME)) {
                    EnderDragonResummonTask.tryRespawn(level);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDespawn(EntityDespawnEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityEndCrystal) {
            EnderDragonResummonTask.onCrystalDestroyed((EntityEndCrystal) entity);
        }
    }
}
