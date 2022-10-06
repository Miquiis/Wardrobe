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

   private List<SkinLocation> pageContents;
   private String searchBar;
   private WardrobePage.PageSort pageSort;
   private boolean isAscending;
   private int page;
   private RequestPagePacket.RequestPagePacketType requestPagePacket;

   public SendPagePacket(List<SkinLocation> pageContents, String searchBar, WardrobePage.PageSort pageSort, boolean isAscending, int page, RequestPagePacket.RequestPagePacketType requestPagePacket) {
      this.pageContents = pageContents;
      this.searchBar = searchBar;
      this.pageSort = pageSort;
      this.isAscending = isAscending;
      this.page = page;
      this.requestPagePacket = requestPagePacket;
   }

   public static void encodePacket(SendPagePacket packet, PacketBuffer buf) {
      CompoundNBT compoundNBT = new CompoundNBT();
      ListNBT pageContents = new ListNBT();
      packet.pageContents.forEach(skinLocation -> {
         pageContents.add(SkinLocation.SKIN_LOCATION.write(skinLocation));
      });
      compoundNBT.put("PageContents", pageContents);
      buf.writeCompoundTag(compoundNBT).writeString(packet.searchBar).writeEnumValue(packet.pageSort).writeBoolean(packet.isAscending).writeInt(packet.page);
      buf.writeEnumValue(packet.requestPagePacket);
   }

   public static SendPagePacket decodePacket(PacketBuffer buf) {
      CompoundNBT compoundNBT = buf.readCompoundTag();
      ListNBT listNBT = compoundNBT.getList("PageContents", Constants.NBT.TAG_COMPOUND);
      List<SkinLocation> skinLocations = new ArrayList<>();
      listNBT.forEach(inbt -> {
         skinLocations.add(SkinLocation.SKIN_LOCATION.read(inbt));
      });
      return new SendPagePacket(skinLocations, buf.readString(), buf.readEnumValue(WardrobePage.PageSort.class), buf.readBoolean(), buf.readInt(), buf.readEnumValue(RequestPagePacket.RequestPagePacketType.class));
   }

   public static void handlePacket(final SendPagePacket msg, Supplier<NetworkEvent.Context> ctx) {
      PacketHandler.handleSendPagePacket(msg);
   }

   public String getSearchBar() {
      return searchBar;
   }

   public int getPage() {
      return page;
   }

   public List<SkinLocation> getPageContents() {
      return pageContents;
   }

   public WardrobePage.PageSort getPageSort() {
      return pageSort;
   }

   public boolean isAscending() {
      return isAscending;
   }

   public RequestPagePacket.RequestPagePacketType getRequestPagePacket() {
      return requestPagePacket;
   }
}