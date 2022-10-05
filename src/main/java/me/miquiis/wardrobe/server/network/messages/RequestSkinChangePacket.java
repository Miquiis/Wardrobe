package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import me.miquiis.wardrobe.database.server.Database;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestSkinChangePacket {

   private byte[] skinHash;

   public RequestSkinChangePacket(byte[] skinHash) {
      this.skinHash = skinHash;
   }

   public static void encodePacket(RequestSkinChangePacket packet, PacketBuffer buf) {
      buf.writeByteArray(packet.skinHash);
   }

   public static RequestSkinChangePacket decodePacket(PacketBuffer buf) {
      return new RequestSkinChangePacket(buf.readByteArray());
   }

   public static void handlePacket(final RequestSkinChangePacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (!Wardrobe.getInstance().getServerTextureCache().hasCache(cached -> Arrays.equals(cached.getValue().getTextureHash(), msg.skinHash)))
      {
         ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new RequestSkinUploadPacket(msg.skinHash));
      }
      String skinHashHex = ImageUtils.byteToHex(msg.skinHash);
      SkinLocation skinLocation = new SkinLocation(skinHashHex, "hex:" + skinHashHex, false);
      SkinChangerAPI.setPlayerSkin(ctx.get().getSender(), skinLocation);
   }
}