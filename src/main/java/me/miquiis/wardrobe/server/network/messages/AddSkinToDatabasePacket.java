package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AddSkinToDatabasePacket {

   private final CompoundNBT payload;

   /**
    * Payload Structure:
    * {@code
    *     {
    *         "SkinLocation": INBT<SkinLocation>,
    *         "FolderName": String
    *     }
    * }
    */
   public AddSkinToDatabasePacket(CompoundNBT payload) {
      this.payload = payload;
   }

   public static AddSkinToDatabasePacket decodePacket(PacketBuffer buf) {
      return new AddSkinToDatabasePacket(buf.readCompoundTag());
   }

   public static void encodePacket(AddSkinToDatabasePacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final AddSkinToDatabasePacket msg, Supplier<NetworkEvent.Context> ctx) {
      SkinLocation skinLocation = SkinLocation.SKIN_LOCATION.read(msg.payload.get("SkinLocation"));
      new Database().createNewSkin(skinLocation.getSkinId(), skinLocation.getSkinURL(), msg.payload.getString("FolderName"), skinLocation.isSlim(), skinLocation.isBaby());
   }
}