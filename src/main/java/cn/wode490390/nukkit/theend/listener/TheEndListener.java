package cn.wode490390.nukkit.theend.listener;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;
import cn.nukkit.network.protocol.LevelSoundEventPacket;

public class TheEndListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block;
        Player player;
        Item item;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (block = event.getBlock()).getId() == Block.END_PORTAL_FRAME && (block.getDamage() & 0x4) == 0 && (item = (player = event.getPlayer()).getInventory().getItemInHand()).getId() == Item.ENDER_EYE) {
            event.setCancelled();
            if (!player.isSneaking()) {
                block.onActivate(item, player);
            }
            for (int i = 0; i < 4; i++) {
                for (int j = -1; j <= 1; j++) {
                    Block t = block.getSide(BlockFace.fromHorizontalIndex(i), 2).getSide(BlockFace.fromHorizontalIndex((i + 1) % 4), j);
                    if (isCompletedPortal(t)) {
                        for (int k = -1; k <= 1; k++) {
                            for (int l = -1; l <= 1; l++) {
                                block.getLevel().setBlock(t.add(k, 0, l), Block.get(Block.END_PORTAL), true);
                            }
                        }
                        block.getLevel().addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_BLOCK_END_PORTAL_SPAWN);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Check whether there is a completed portal with the specified center.
     */
    private static boolean isCompletedPortal(Block center) {
        for (int i = 0; i < 4; i++) {
            for (int j = -1; j <= 1; j++) {
                Block block = center.getSide(BlockFace.fromHorizontalIndex(i), 2).getSide(BlockFace.fromHorizontalIndex((i + 1) % 4), j);
                if (block.getId() != Block.END_PORTAL_FRAME || (block.getDamage() & 0x4) == 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
