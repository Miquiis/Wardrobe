package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.client.PacketHandler;
import me.miquiis.wardrobe.common.WardrobePage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SendPagePacket {

   private CompoundNBT payload;

   public SendPagePacket(CompoundNBT payload)
   {
      this.payload = payload;
   }

   public static void encodePacket(SendPagePacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   public static SendPagePacket decodePacket(PacketBuffer buf) {
      return new SendPagePacket(buf.readCompoundTag());
   }

   public static void handlePacket(final SendPagePacket msg, Supplier<NetworkEvent.Context> ctx) {
      PacketHandler.handleSendPagePacket(msg);
   }

   public List<SkinLocation> getPageContents() {
      List<SkinLocation> skinLocations = new ArrayList<>();
      ListNBT listNBT = payload.getList("SkinLocations", Constants.NBT.TAG_COMPOUND);
      listNBT.forEach(inbt -> {
         skinLocations.add(SkinLocation.SKIN_LOCATION.read(inbt));
      });
      return skinLocations;
   }

   public String getFolderName() {
      return payload.getString("Folder");
   }

   public String getSearchBar() {
      return payload.getString("SearchBar");
   }

   public RequestPagePacket.RequestPagePacketType getRequestPagePacket() {
      return RequestPagePacket.RequestPagePacketType.values()[payload.getInt("RequestPageType")];
   }

   public WardrobePage.PageSort getPageSort() {
      return WardrobePage.PageSort.values()[payload.getInt("PageSort")];
   }

   public int getPage() {
      return payload.getInt("Page");
   }

   public int getStartsAt() {
      return payload.getInt("StartsAt");
   }

   public boolean hasNextPage() {
      return payload.getBoolean("HasNextPage");
   }

   public boolean isAscending() {
      return payload.getBoolean("IsAscending");
   }
}