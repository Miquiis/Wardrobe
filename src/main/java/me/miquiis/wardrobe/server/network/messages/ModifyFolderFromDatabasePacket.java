package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ModifyFolderFromDatabasePacket {

   private final CompoundNBT payload;

   /**
    * Payload Structure:
    * {@code
    *     {
    *         "FolderName": String,
    *         "FolderIcon": String,
    *         "OldFolderName": String
    *     }
    * }
    */
   public ModifyFolderFromDatabasePacket(CompoundNBT payload) {
      this.payload = payload;
   }

   public static ModifyFolderFromDatabasePacket decodePacket(PacketBuffer buf) {
      return new ModifyFolderFromDatabasePacket(buf.readCompoundTag());
   }

   public static void encodePacket(ModifyFolderFromDatabasePacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final ModifyFolderFromDatabasePacket msg, Supplier<NetworkEvent.Context> ctx) {
      new Database().updateExistingFolder(msg.payload.getString("OldFolderName"), msg.payload.getString("FolderName"), msg.payload.getString("FolderIcon"));
   }
}