package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ModifySkinToDatabasePacket {

   private final SkinLocation prevSkinLocation;
   private final SkinLocation skinLocation;

   public ModifySkinToDatabasePacket(SkinLocation prevSkinLocation, SkinLocation skinLocation) {
      this.prevSkinLocation = prevSkinLocation;
      this.skinLocation = skinLocation;
   }

   public static ModifySkinToDatabasePacket decodePacket(PacketBuffer buf) {
      return new ModifySkinToDatabasePacket(SkinLocation.SKIN_LOCATION.read(buf), SkinLocation.SKIN_LOCATION.read(buf));
   }

   public static void encodePacket(ModifySkinToDatabasePacket packet, PacketBuffer buf) {
      SkinLocation.SKIN_LOCATION.write(buf, packet.prevSkinLocation);
      SkinLocation.SKIN_LOCATION.write(buf, packet.skinLocation);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final ModifySkinToDatabasePacket msg, Supplier<NetworkEvent.Context> ctx) {
      new Database().updateSkinURL(msg.prevSkinLocation.getSkinId(), msg.skinLocation.getSkinId(), msg.skinLocation.getSkinURL(), msg.skinLocation.isSlim(), msg.skinLocation.isBaby());
   }
}