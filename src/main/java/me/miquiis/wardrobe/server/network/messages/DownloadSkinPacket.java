package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.client.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DownloadSkinPacket {

    private final byte[] skinBytes;
    private final boolean skinIsSlim;
    private final boolean skinIsBaby;

    public DownloadSkinPacket(byte[] skinBytes, boolean skinIsSlim, boolean skinIsBaby) {
        this.skinBytes = skinBytes;
        this.skinIsSlim = skinIsSlim;
        this.skinIsBaby = skinIsBaby;
    }

    public static void encodePacket(DownloadSkinPacket packet, PacketBuffer buf) {
        buf.writeByteArray(packet.skinBytes).writeBoolean(packet.skinIsSlim).writeBoolean(packet.skinIsBaby);
    }

    public static DownloadSkinPacket decodePacket(PacketBuffer buf) {
        return new DownloadSkinPacket(buf.readByteArray(), buf.readBoolean(), buf.readBoolean());
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

    public boolean isSkinIsBaby() {
        return skinIsBaby;
    }
}
