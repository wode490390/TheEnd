package cn.wode490390.nukkit.theend.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockEndPortal;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityMotionEvent;
import cn.nukkit.event.entity.EntityPortalEnterEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.RespawnPacket;
import cn.nukkit.network.protocol.ShowCreditsPacket;
import cn.nukkit.scheduler.Task;
import cn.wode490390.nukkit.theend.generator.TheEndGenerator;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class PortalListener implements Listener {

    private final LongSet showing = new LongOpenHashSet();

    @EventHandler
    public void onDataPacketReceive(DataPacketReceiveEvent event) {
        Player player = event.getPlayer();
        DataPacket packet = event.getPacket();
        if (packet instanceof ShowCreditsPacket) {
            ShowCreditsPacket showCreditsPacket = (ShowCreditsPacket) packet;
            if (showCreditsPacket.status == ShowCreditsPacket.STATUS_END_CREDITS && this.showing.remove(player.getId())) {
                Position respawnPos = player.getSpawn();

                /*ChangeDimensionPacket changeDimensionPacket = new ChangeDimensionPacket();
                changeDimensionPacket.dimension = Level.DIMENSION_OVERWORLD;
                changeDimensionPacket.x = (float) respawnPos.x;
                changeDimensionPacket.y = 32767f; //???
                changeDimensionPacket.z = (float) respawnPos.z;
                changeDimensionPacket.respawn = true;
                player.dataPacket(changeDimensionPacket);*/

                RespawnPacket respawnPacket = new RespawnPacket();
                respawnPacket.x = (float) respawnPos.x;
                respawnPacket.y = (float) respawnPos.y;
                respawnPacket.z = (float) respawnPos.z;
                //respawnPacket.respawnState = RespawnPacket.STATE_SEARCHING_FOR_SPAWN;
                //respawnPacket.runtimeEntityId = player.getId(); //always 0, useless
                player.dataPacket(respawnPacket);

                player.removeAllEffects();

                //this is a dirty hack to prevent dying in a different level than the respawn point from breaking everything
                player.teleportImmediate(new Location(respawnPos.x, -100, respawnPos.z, respawnPos.level));
                player.teleport(respawnPos, null);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getLevel().getBlock(player.getFloorX(), player.getFloorY(), player.getFloorZ()) instanceof BlockEndPortal && player.y - player.getFloorY() < 0.75) {
            EntityPortalEnterEvent ev = new EntityPortalEnterEvent(player, EntityPortalEnterEvent.PortalType.END);
            Server.getInstance().getPluginManager().callEvent(ev);
            if (!ev.isCancelled()) {
                Position newPos = moveToTheEnd(player);
                if (newPos != null) {
                    for (int x = -2; x < 3; x++) {
                        for (int z = -2; z < 3; z++) {
                            int chunkX = (newPos.getFloorX() >> 4) + x;
                            int chunkZ = (newPos.getFloorZ() >> 4) + z;
                            FullChunk chunk = newPos.getLevel().getChunk(chunkX, chunkZ, false);
                            if (chunk == null || !(chunk.isGenerated() || chunk.isPopulated())) {
                                newPos.getLevel().generateChunk(chunkX, chunkZ, true);
                            }
                        }
                    }
                    if (newPos.getLevel().getDimension() == Level.DIMENSION_THE_END) {
                        player.teleport(newPos);
                        Server.getInstance().getScheduler().scheduleDelayedTask(new Task() {
                            @Override
                            public void onRun(int currentTick) {
                                // dirty hack to make sure chunks are loaded and generated before spawning player
                                TheEndGenerator.spawnPlatform(newPos);
                                player.teleport(newPos);
                            }
                        }, 20);
                    } else {
                        long id = player.getId();
                        if (this.showing.add(id)) {
                            ShowCreditsPacket pk = new ShowCreditsPacket();
                            pk.eid = id;
                            pk.status = ShowCreditsPacket.STATUS_START_CREDITS;
                            player.dataPacket(pk);
                        }
                    }
                }
            }
        }
    }

    //@EventHandler //TODO: bug
    public void onEntityMotion(EntityMotionEvent event) {
        Entity entity = event.getEntity();
        if (entity.getLevel().getBlock(entity.getFloorX(), entity.getFloorY(), entity.getFloorZ()) instanceof BlockEndPortal && entity.y - entity.getFloorY() < 0.75) {
            EntityPortalEnterEvent ev = new EntityPortalEnterEvent(entity, EntityPortalEnterEvent.PortalType.END);
            Server.getInstance().getPluginManager().callEvent(ev);
            if (!ev.isCancelled()) {
                Position newPos = moveToTheEnd(entity);
                if (newPos != null) {
                    for (int x = -2; x < 3; x++) {
                        for (int z = -2; z < 3; z++) {
                            int chunkX = (newPos.getFloorX() >> 4) + x;
                            int chunkZ = (newPos.getFloorZ() >> 4) + z;
                            FullChunk chunk = newPos.getLevel().getChunk(chunkX, chunkZ, false);
                            if (chunk == null || !(chunk.isGenerated() || chunk.isPopulated())) {
                                newPos.getLevel().generateChunk(chunkX, chunkZ, true);
                            }
                        }
                    }
                    entity.teleport(newPos);
                    Server.getInstance().getScheduler().scheduleDelayedTask(new Task() {
                        @Override
                        public void onRun(int currentTick) {
                            // dirty hack to make sure chunks are loaded and generated before spawning player
                            if (newPos.getLevel().getDimension() == Level.DIMENSION_THE_END) {
                                TheEndGenerator.spawnPlatform(newPos);
                            }
                            entity.teleport(newPos);
                        }
                    }, 20);
                }
            }
        }
    }

    @EventHandler
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (event.getPortalType() == EntityPortalEnterEvent.PortalType.NETHER) {
            Entity entity = event.getEntity();
            if (entity.level == entity.getServer().getLevelByName("the_end")) {
                event.setCancelled();
            }
        }
    }

    private static Position moveToTheEnd(Position current) {
        Preconditions.checkNotNull(current);

        if (!Server.getInstance().loadLevel("the_end")) {
            Server.getInstance().getLogger().info("No level called 'the_end' found, creating default the end level.");
            Class<? extends Generator> generator = Generator.getGenerator("the_end");
            Server.getInstance().generateLevel("the_end", System.currentTimeMillis(), generator);
            if (!Server.getInstance().isLevelLoaded("the_end")) {
                Server.getInstance().loadLevel("the_end");
            }
        }

        Level the_end = Server.getInstance().getLevelByName("the_end");
        if (the_end != null) {
            if (current.level == the_end) {
                return Server.getInstance().getDefaultLevel().getSpawnLocation();
            } else {
                return new Position(100.5, 49, 0.5, the_end);
            }
        }

        return null;
    }
}
