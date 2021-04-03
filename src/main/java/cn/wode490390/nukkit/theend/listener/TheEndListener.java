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
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getBlock();
            if (block.getId() == Block.END_PORTAL_FRAME && (block.getDamage() & 0x4) == 0) {
                Player player = event.getPlayer();
                Item item = player.getInventory().getItemInHand();
                if (item.getId() == Item.ENDER_EYE) {
                    event.setCancelled();

                    if (!player.isSneaking()) {
                        block.onActivate(item, player);
                    }

                    for (int i = 0; i < 4; i++) {
                        for (int j = -1; j <= 1; j++) {
                            Block test = block.getSide(BlockFace.fromHorizontalIndex(i), 2).getSide(BlockFace.fromHorizontalIndex((i + 1) % 4), j);
                            if (isCompletedPortal(test)) {
                                for (int x = -1; x <= 1; x++) {
                                    for (int z = -1; z <= 1; z++) {
                                        block.getLevel().setBlock(test.add(x, 0, z), Block.get(Block.END_PORTAL), true);
                                    }
                                }

                                block.getLevel().addLevelSoundEvent(block, LevelSoundEventPacket.SOUND_BLOCK_END_PORTAL_SPAWN);
                                return;
                            }
                        }
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
