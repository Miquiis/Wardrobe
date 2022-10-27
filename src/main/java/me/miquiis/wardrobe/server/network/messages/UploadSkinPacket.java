package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import me.miquiis.wardrobe.common.cache.TextureCache;
import me.miquiis.wardrobe.server.ServerWardrobe;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public class UploadSkinPacket {

    private final byte[] skinBytes;
    private final byte[] skinHash;
    private final boolean skinIsSlim;
    private final boolean skinIsBaby;
    private final RequestSkinPacket.RequestSkinPacketType skinUploadPacketType;

    public UploadSkinPacket(byte[] skinBytes, byte[] skinHash, boolean skinIsSlim, boolean skinIsBaby, RequestSkinPacket.RequestSkinPacketType skinUploadPacketType) {
        this.skinBytes = skinBytes;
        this.skinHash = skinHash;
        this.skinIsSlim = skinIsSlim;
        this.skinIsBaby = skinIsBaby;
        this.skinUploadPacketType = skinUploadPacketType;
    }

    public static void encodePacket(UploadSkinPacket packet, PacketBuffer buf) {
        buf.writeByteArray(packet.skinBytes).writeByteArray(packet.skinHash).writeBoolean(packet.skinIsSlim).writeBoolean(packet.skinIsBaby);
        buf.writeEnumValue(packet.skinUploadPacketType);
    }

    public static UploadSkinPacket decodePacket(PacketBuffer buf) {
        return new UploadSkinPacket(buf.readByteArray(), buf.readByteArray(), buf.readBoolean(), buf.readBoolean(), buf.readEnumValue(RequestSkinPacket.RequestSkinPacketType.class));
    }

    public static void handlePacket(final UploadSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
        byte[] skinHash = msg.skinHash;
        if (!Wardrobe.getInstance().getServerTextureCache().hasCache(cached -> Arrays.equals(cached.getValue().getTextureHash(), skinHash)))
        {
            Wardrobe.getInstance().getServerTextureCache().cache(new TextureCache(msg.skinBytes, skinHash, msg.skinIsSlim, msg.skinIsBaby));
        }
        if (msg.skinUploadPacketType == RequestSkinPacket.RequestSkinPacketType.SEND_TO_CLIENT)
        {
            String skinHashHex = ImageUtils.byteToHex(msg.skinHash);
            SkinLocation skinLocation = new SkinLocation(skinHashHex, "hex:" + skinHashHex, msg.skinIsSlim, msg.skinIsBaby);
            SkinChangerAPI.setPlayerSkin(ctx.get().getSender(), skinLocation);
        } else if (msg.skinUploadPacketType == RequestSkinPacket.RequestSkinPacketType.SAVE_TO_SERVER)
        {
            String skinHashHex = ImageUtils.byteToHex(msg.skinHash);
            SkinLocation skinLocation = new SkinLocation(skinHashHex, "hex:" + skinHashHex, msg.skinIsSlim, msg.skinIsBaby);
            ServerWardrobe.addSkin(skinLocation);
        }
    }

}
