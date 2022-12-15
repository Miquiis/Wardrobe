package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.client.PacketHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * Payload Structure:
 * {@code
 *     {
 *         "Folders": List of CompoundNBT<WardrobePage>
 *     }
 * }
 */
public class SendFoldersPacket {

   private CompoundNBT payload;

   /**
    * Payload Structure:
    * {@code
    *     {
    *         "Folders": List of CompoundNBT<WardrobePage>
    *     }
    * }
    */
   public SendFoldersPacket(CompoundNBT payload) {
      this.payload = payload;
   }

   public static void encodePacket(SendFoldersPacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   public static SendFoldersPacket decodePacket(PacketBuffer buf) {
      return new SendFoldersPacket(buf.readCompoundTag());
   }

   public static void handlePacket(final SendFoldersPacket msg, Supplier<NetworkEvent.Context> ctx) {
      PacketHandler.handleSendFoldersPacket(msg);
   }

   public CompoundNBT getPayload() {
      return payload;
   }
}