package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RemoveSkinFromDatabasePacket {

   private final SkinLocation skinLocation;

   public RemoveSkinFromDatabasePacket(SkinLocation skinLocation) {
      this.skinLocation = skinLocation;
   }

   public static RemoveSkinFromDatabasePacket decodePacket(PacketBuffer buf) {
      return new RemoveSkinFromDatabasePacket(SkinLocation.SKIN_LOCATION.read(buf));
   }

   public static void encodePacket(RemoveSkinFromDatabasePacket packet, PacketBuffer buf) {
      SkinLocation.SKIN_LOCATION.write(buf, packet.skinLocation);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final RemoveSkinFromDatabasePacket msg, Supplier<NetworkEvent.Context> ctx) {
      new Database().deleteSkin(msg.skinLocation.getSkinId());
   }
}