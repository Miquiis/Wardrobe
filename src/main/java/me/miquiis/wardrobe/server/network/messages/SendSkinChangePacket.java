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

public class SendSkinChangePacket {

   private byte[] skinHash;

   public SendSkinChangePacket(byte[] skinHash) {
      this.skinHash = skinHash;
   }

   public static void encodePacket(SendSkinChangePacket packet, PacketBuffer buf) {
      buf.writeByteArray(packet.skinHash);
   }

   public static SendSkinChangePacket decodePacket(PacketBuffer buf) {
      return new SendSkinChangePacket(buf.readByteArray());
   }

   public static void handlePacket(final SendSkinChangePacket msg, Supplier<NetworkEvent.Context> ctx) {
      PacketHandler.handleSendSkinChangePacket(msg);
   }

   public byte[] getSkinHash() {
      return skinHash;
   }
}