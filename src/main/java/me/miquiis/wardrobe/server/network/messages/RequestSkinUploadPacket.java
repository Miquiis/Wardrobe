package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public class RequestSkinUploadPacket {

   private byte[] skinHash;

   public RequestSkinUploadPacket(byte[] skinHash) {
      this.skinHash = skinHash;
   }

   public static void encodePacket(RequestSkinUploadPacket packet, PacketBuffer buf) {
      buf.writeByteArray(packet.skinHash);
   }

   public static RequestSkinUploadPacket decodePacket(PacketBuffer buf) {
      return new RequestSkinUploadPacket(buf.readByteArray());
   }

   public static void handlePacket(final RequestSkinUploadPacket msg, Supplier<NetworkEvent.Context> ctx) {
      PacketHandler.handleRequestSkinUploadPacket(msg);
   }

   public byte[] getSkinHash() {
      return skinHash;
   }
}