package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import me.miquiis.wardrobe.server.ServerWardrobe;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Arrays;
import java.util.function.Supplier;

public class RequestSkinPacket {

   public enum RequestSkinPacketType {
      SEND_TO_CLIENT,
      SAVE_TO_SERVER
   }

   private final byte[] skinHash;
   private final boolean skinIsSlim;
   private final RequestSkinPacketType skinPacketType;

   public RequestSkinPacket(byte[] skinHash, boolean skinIsSlim, RequestSkinPacketType skinPacketType) {
      this.skinHash = skinHash;
      this.skinIsSlim = skinIsSlim;
      this.skinPacketType = skinPacketType;
   }

   public static void encodePacket(RequestSkinPacket packet, PacketBuffer buf) {
      buf.writeByteArray(packet.skinHash).writeBoolean(packet.skinIsSlim);
      buf.writeEnumValue(packet.skinPacketType);
   }

   public static RequestSkinPacket decodePacket(PacketBuffer buf) {
      return new RequestSkinPacket(buf.readByteArray(), buf.readBoolean(), buf.readEnumValue(RequestSkinPacketType.class));
   }

   public static void handlePacket(final RequestSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (!Wardrobe.getInstance().getServerTextureCache().hasCache(cached -> Arrays.equals(cached.getValue().getTextureHash(), msg.skinHash) && cached.getValue().isTextureIsSlim() == msg.skinIsSlim))
      {
         ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new RequestSkinUploadPacket(msg.skinHash, msg.skinPacketType));
      } else {
         if (msg.skinPacketType == RequestSkinPacketType.SEND_TO_CLIENT)
         {
            String skinHashHex = ImageUtils.byteToHex(msg.skinHash);
            SkinLocation skinLocation = new SkinLocation(skinHashHex, "hex:" + skinHashHex, msg.skinIsSlim);
            SkinChangerAPI.setPlayerSkin(ctx.get().getSender(), skinLocation);
         } else if (msg.skinPacketType == RequestSkinPacketType.SAVE_TO_SERVER)
         {
            String skinHashHex = ImageUtils.byteToHex(msg.skinHash);
            SkinLocation skinLocation = new SkinLocation(skinHashHex, "hex:" + skinHashHex, msg.skinIsSlim);
            ServerWardrobe.addSkin(skinLocation);
         }
      }
   }
}