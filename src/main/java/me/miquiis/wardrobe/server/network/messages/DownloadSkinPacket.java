package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.client.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DownloadSkinPacket {

    private final byte[] skinBytes;
    private final boolean skinIsSlim;

    public DownloadSkinPacket(byte[] skinBytes, boolean skinIsSlim) {
        this.skinBytes = skinBytes;
        this.skinIsSlim = skinIsSlim;
    }

    public static void encodePacket(DownloadSkinPacket packet, PacketBuffer buf) {
        buf.writeByteArray(packet.skinBytes).writeBoolean(packet.skinIsSlim);
    }

    public static DownloadSkinPacket decodePacket(PacketBuffer buf) {
        return new DownloadSkinPacket(buf.readByteArray(), buf.readBoolean());
    }

    public static void handlePacket(final DownloadSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleDownloadSkinPacket(msg);
    }

    public byte[] getSkinBytes() {
        return skinBytes;
    }

    public boolean isSkinIsSlim() {
        return skinIsSlim;
    }
}
