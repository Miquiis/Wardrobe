package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.PacketHandler;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Arrays;
import java.util.function.Supplier;

public class RequestSkinDownloadPacket {

   private byte[] skinHash;

   public RequestSkinDownloadPacket(byte[] skinHash) {
      this.skinHash = skinHash;
   }

   public static void encodePacket(RequestSkinDownloadPacket packet, PacketBuffer buf) {
      buf.writeByteArray(packet.skinHash);
   }

   public static RequestSkinDownloadPacket decodePacket(PacketBuffer buf) {
      return new RequestSkinDownloadPacket(buf.readByteArray());
   }

   public static void handlePacket(final RequestSkinDownloadPacket msg, Supplier<NetworkEvent.Context> ctx) {
      Wardrobe.getInstance().getServerTextureCache().getCache(cached -> Arrays.equals(cached.getValue().getTextureHash(), msg.skinHash)).ifPresent(cached -> {
         System.out.println("Downloading");
         byte[] skinBytes = cached.getValue().getTextureBytes();
         ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new DownloadSkinPacket(skinBytes));
      });
   }

   public byte[] getSkinHash() {
      return skinHash;
   }
}