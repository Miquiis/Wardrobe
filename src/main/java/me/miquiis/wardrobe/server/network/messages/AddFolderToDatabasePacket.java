package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AddFolderToDatabasePacket {

   private final CompoundNBT payload;

   /**
    * Payload Structure:
    * {@code
    *     {
    *         "FolderName": String,
    *         "FolderIcon": String
    *     }
    * }
    */
   public AddFolderToDatabasePacket(CompoundNBT payload) {
      this.payload = payload;
   }

   public static AddFolderToDatabasePacket decodePacket(PacketBuffer buf) {
      return new AddFolderToDatabasePacket(buf.readCompoundTag());
   }

   public static void encodePacket(AddFolderToDatabasePacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final AddFolderToDatabasePacket msg, Supplier<NetworkEvent.Context> ctx) {
      new Database().createNewFolder(msg.payload.getString("FolderName"), msg.payload.getString("FolderIcon"));
   }
}