package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ModifySkinToDatabasePacket {

   private final CompoundNBT payload;

   /**
    * Payload Structure:
    * {@code
    *     {
    *         "PrevSkinLocation": INBT<SkinLocation>,
    *         "SkinLocation": INBT<SkinLocation>,
    *         "FolderName": String,
    *     }
    * }
    */
   public ModifySkinToDatabasePacket(CompoundNBT payload) {
      this.payload = payload;
   }

   public static ModifySkinToDatabasePacket decodePacket(PacketBuffer buf) {
      return new ModifySkinToDatabasePacket(buf.readCompoundTag());
   }

   public static void encodePacket(ModifySkinToDatabasePacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final ModifySkinToDatabasePacket msg, Supplier<NetworkEvent.Context> ctx) {
      SkinLocation prevSkinLocation = SkinLocation.SKIN_LOCATION.read(msg.payload.get("PrevSkinLocation"));
      SkinLocation skinLocation = SkinLocation.SKIN_LOCATION.read(msg.payload.get("SkinLocation"));
      String folderName = msg.payload.getString("FolderName");
      new Database().updateExistingSkin(prevSkinLocation.getSkinId(), skinLocation.getSkinId(), skinLocation.getSkinURL(), folderName, skinLocation.isSlim(), skinLocation.isBaby());
   }
}