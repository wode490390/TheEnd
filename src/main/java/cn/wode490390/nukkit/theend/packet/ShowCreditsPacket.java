package cn.wode490390.nukkit.theend.packet;

public class ShowCreditsPacket extends cn.nukkit.network.protocol.ShowCreditsPacket {

    @Override
    public void decode() {
        this.eid = this.getEntityRuntimeId();
        this.status = this.getVarInt();
    }
}
