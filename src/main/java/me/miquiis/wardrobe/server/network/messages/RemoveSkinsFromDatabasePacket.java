package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RemoveSkinsFromDatabasePacket {

   private final CompoundNBT payload;

   public RemoveSkinsFromDatabasePacket(CompoundNBT payload) {
      this.payload = payload;
   }

   public static RemoveSkinsFromDatabasePacket decodePacket(PacketBuffer buf) {
      return new RemoveSkinsFromDatabasePacket(buf.readCompoundTag());
   }

   public static void encodePacket(RemoveSkinsFromDatabasePacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final RemoveSkinsFromDatabasePacket msg, Supplier<NetworkEvent.Context> ctx) {
      List<SkinLocation> skinLocationList = new ArrayList<>();
      ListNBT listNBT = msg.payload.getList("SkinLocations", Constants.NBT.TAG_COMPOUND);
      listNBT.forEach(inbt -> {
         SkinLocation skinLocation = SkinLocation.SKIN_LOCATION.read(inbt);
         skinLocationList.add(skinLocation);
      });
      new Database().bulkDeleteSkin(skinLocationList);
   }
}