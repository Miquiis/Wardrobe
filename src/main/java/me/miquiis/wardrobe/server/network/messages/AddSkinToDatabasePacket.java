package me.miquiis.wardrobe.server.network.messages;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AddSkinToDatabasePacket {

   private final SkinLocation skinLocation;

   public AddSkinToDatabasePacket(SkinLocation skinLocation) {
      this.skinLocation = skinLocation;
   }

   public static AddSkinToDatabasePacket decodePacket(PacketBuffer buf) {
      return new AddSkinToDatabasePacket(SkinLocation.SKIN_LOCATION.read(buf));
   }

   public static void encodePacket(AddSkinToDatabasePacket packet, PacketBuffer buf) {
      SkinLocation.SKIN_LOCATION.write(buf, packet.skinLocation);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final AddSkinToDatabasePacket msg, Supplier<NetworkEvent.Context> ctx) {
      new Database().saveSkinURL(msg.skinLocation.getSkinId(), msg.skinLocation.getSkinURL(), msg.skinLocation.isSlim(), msg.skinLocation.isBaby());
   }
}