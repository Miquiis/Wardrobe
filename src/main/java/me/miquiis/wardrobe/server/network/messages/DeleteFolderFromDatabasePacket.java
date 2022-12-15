package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DeleteFolderFromDatabasePacket {

   private final CompoundNBT payload;

   /**
    * Payload Structure:
    * {@code
    *     {
    *         "FolderName": String
    *     }
    * }
    */
   public DeleteFolderFromDatabasePacket(CompoundNBT payload) {
      this.payload = payload;
   }

   public static DeleteFolderFromDatabasePacket decodePacket(PacketBuffer buf) {
      return new DeleteFolderFromDatabasePacket(buf.readCompoundTag());
   }

   public static void encodePacket(DeleteFolderFromDatabasePacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final DeleteFolderFromDatabasePacket msg, Supplier<NetworkEvent.Context> ctx) {
      new Database().deleteFolder(msg.payload.getString("FolderName"));
   }
}