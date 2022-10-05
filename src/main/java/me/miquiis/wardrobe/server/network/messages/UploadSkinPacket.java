package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import me.miquiis.wardrobe.common.cache.TextureCache;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Supplier;

public class UploadSkinPacket {

    private byte[] skinBytes;
    private byte[] skinHash;

    public UploadSkinPacket(byte[] skinBytes, byte[] skinHash) {
        this.skinBytes = skinBytes;
        this.skinHash = skinHash;
    }

    public static void encodePacket(UploadSkinPacket packet, PacketBuffer buf) {
        buf.writeByteArray(packet.skinBytes).writeByteArray(packet.skinHash);
    }

    public static UploadSkinPacket decodePacket(PacketBuffer buf) {
        return new UploadSkinPacket(buf.readByteArray(), buf.readByteArray());
    }

    public static void handlePacket(final UploadSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
        byte[] skinHash = msg.skinHash;
        if (!Wardrobe.getInstance().getServerTextureCache().hasCache(cached -> Arrays.equals(cached.getValue().getTextureHash(), skinHash)))
        {
            Wardrobe.getInstance().getServerTextureCache().cache(new TextureCache(msg.skinBytes, skinHash));
        }
    }

}
