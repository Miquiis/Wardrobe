package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.utils.Payload;
import me.miquiis.wardrobe.database.server.Database;
import me.miquiis.wardrobe.server.ServerWardrobe;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RequestPagePacket {

   public enum RequestPagePacketType {
      DATABASE,
      SERVER
   }

   private CompoundNBT payload;

   public RequestPagePacket(CompoundNBT payload) {
      this.payload = payload;
   }

   public static void encodePacket(RequestPagePacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   public static RequestPagePacket decodePacket(PacketBuffer buf) {
      return new RequestPagePacket(buf.readCompoundTag());
   }

   public static void handlePacket(final RequestPagePacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (msg.getRequestPagePacket() == RequestPagePacketType.DATABASE)
      {
         new Database().fetchPage(msg.getFolderName(), msg.getSearchBar(), msg.getPageSort(), msg.isAscending(), msg.getStartsAt()).thenAcceptAsync(skinLocations -> {
            new Database().hasNextPage(msg.getFolderName(), msg.getSearchBar(), msg.getPageSort(), msg.isAscending(), msg.getStartsAt() + 16).thenAcceptAsync(hasNextPage -> {
               ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new SendPagePacket(new Payload(msg.payload).putBoolean("HasNextPage", hasNextPage).putList("SkinLocations", skinLocations.stream().map(SkinLocation.SKIN_LOCATION::write).collect(Collectors.toList())).getPayload()));
            });
         });
      } else {
         ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new SendPagePacket(new Payload(msg.payload).putList("SkinLocations", ServerWardrobe.getServerWardrobe().stream().map(SkinLocation.SKIN_LOCATION::write).collect(Collectors.toList())).getPayload()));
      }
   }

   public String getFolderName() {
      return payload.getString("Folder");
   }

   public String getSearchBar() {
      return payload.getString("SearchBar");
   }

   public RequestPagePacketType getRequestPagePacket() {
      return RequestPagePacketType.values()[payload.getInt("RequestPageType")];
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

   public boolean isAscending() {
      return payload.getBoolean("IsAscending");
   }
}