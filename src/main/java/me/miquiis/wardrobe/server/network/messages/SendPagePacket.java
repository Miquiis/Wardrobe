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
   private WardrobePage.PageSort pageSort;
   private boolean isAscending;
   private int page;

   public SendPagePacket(List<SkinLocation> pageContents, WardrobePage.PageSort pageSort, boolean isAscending, int page) {
      this.pageContents = pageContents;
      this.pageSort = pageSort;
      this.isAscending = isAscending;
      this.page = page;
   }

   public static void encodePacket(SendPagePacket packet, PacketBuffer buf) {
      CompoundNBT compoundNBT = new CompoundNBT();
      ListNBT pageContents = new ListNBT();
      packet.pageContents.forEach(skinLocation -> {
         pageContents.add(SkinLocation.SKIN_LOCATION.write(skinLocation));
      });
      compoundNBT.put("PageContents", pageContents);
      buf.writeCompoundTag(compoundNBT).writeEnumValue(packet.pageSort).writeBoolean(packet.isAscending).writeInt(packet.page);
   }

   public static SendPagePacket decodePacket(PacketBuffer buf) {
      CompoundNBT compoundNBT = buf.readCompoundTag();
      ListNBT listNBT = compoundNBT.getList("PageContents", Constants.NBT.TAG_COMPOUND);
      List<SkinLocation> skinLocations = new ArrayList<>();
      listNBT.forEach(inbt -> {
         skinLocations.add(SkinLocation.SKIN_LOCATION.read(inbt));
      });
      return new SendPagePacket(skinLocations, buf.readEnumValue(WardrobePage.PageSort.class), buf.readBoolean(), buf.readInt());
   }

   public static void handlePacket(final SendPagePacket msg, Supplier<NetworkEvent.Context> ctx) {
      PacketHandler.handleSendPagePacket(msg);
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
}