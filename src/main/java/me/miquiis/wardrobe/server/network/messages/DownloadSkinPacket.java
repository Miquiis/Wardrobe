package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.PacketHandler;
import me.miquiis.wardrobe.common.cache.TextureCache;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Supplier;

public class DownloadSkinPacket {

    private byte[] skinBytes;

    public DownloadSkinPacket(byte[] skinBytes) {
        this.skinBytes = skinBytes;
    }

    public static void encodePacket(DownloadSkinPacket packet, PacketBuffer buf) {
        buf.writeByteArray(packet.skinBytes);
    }

    public static DownloadSkinPacket decodePacket(PacketBuffer buf) {
        return new DownloadSkinPacket(buf.readByteArray());
    }

    public static void handlePacket(final DownloadSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
        PacketHandler.handleDownloadSkinPacket(msg);
    }

    public byte[] getSkinBytes() {
        return skinBytes;
    }
}
