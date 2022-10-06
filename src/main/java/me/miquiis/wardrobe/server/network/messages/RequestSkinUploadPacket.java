package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.client.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSkinUploadPacket {

   private final byte[] skinHash;
   private final RequestSkinPacket.RequestSkinPacketType skinUploadPacketType;

   public RequestSkinUploadPacket(byte[] skinHash, RequestSkinPacket.RequestSkinPacketType skinUploadPacketType) {
      this.skinHash = skinHash;
      this.skinUploadPacketType = skinUploadPacketType;
   }

   public static void encodePacket(RequestSkinUploadPacket packet, PacketBuffer buf) {
      buf.writeByteArray(packet.skinHash);
      buf.writeEnumValue(packet.skinUploadPacketType);
   }

   public static RequestSkinUploadPacket decodePacket(PacketBuffer buf) {
      return new RequestSkinUploadPacket(buf.readByteArray(), buf.readEnumValue(RequestSkinPacket.RequestSkinPacketType.class));
   }

   public static void handlePacket(final RequestSkinUploadPacket msg, Supplier<NetworkEvent.Context> ctx) {
      PacketHandler.handleRequestSkinUploadPacket(msg);
   }

   public byte[] getSkinHash() {
      return skinHash;
   }

   public RequestSkinPacket.RequestSkinPacketType getSkinUploadPacketType() {
      return skinUploadPacketType;
   }
}