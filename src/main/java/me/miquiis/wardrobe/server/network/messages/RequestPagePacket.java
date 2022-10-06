package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.database.server.Database;
import me.miquiis.wardrobe.server.ServerWardrobe;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class RequestPagePacket {

   public enum RequestPagePacketType {
      DATABASE,
      SERVER
   }

   private String searchBar;
   private WardrobePage.PageSort pageSort;
   private boolean isAscending;
   private int page;
   private int startsAt;
   private RequestPagePacketType requestPagePacket;

   public RequestPagePacket(String searchBar, WardrobePage.PageSort pageSort, boolean isAscending, int page, int startsAt, RequestPagePacketType requestPagePacket) {
      this.searchBar = searchBar;
      this.pageSort = pageSort;
      this.isAscending = isAscending;
      this.page = page;
      this.startsAt = startsAt;
      this.requestPagePacket = requestPagePacket;
   }

   public static void encodePacket(RequestPagePacket packet, PacketBuffer buf) {
      buf.writeString(packet.searchBar).writeEnumValue(packet.pageSort).writeBoolean(packet.isAscending).writeInt(packet.page).writeInt(packet.startsAt);
      buf.writeEnumValue(packet.requestPagePacket);
   }

   public static RequestPagePacket decodePacket(PacketBuffer buf) {
      return new RequestPagePacket(buf.readString(), buf.readEnumValue(WardrobePage.PageSort.class), buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readEnumValue(RequestPagePacketType.class));
   }

   public static void handlePacket(final RequestPagePacket msg, Supplier<NetworkEvent.Context> ctx) {
      if (msg.requestPagePacket == RequestPagePacketType.DATABASE)
      {
         new Database().fetchPage(msg.searchBar, msg.pageSort, msg.isAscending, msg.startsAt).thenAcceptAsync(skinLocations -> {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new SendPagePacket(skinLocations, msg.searchBar, msg.pageSort, msg.isAscending, msg.page, msg.requestPagePacket));
         });
      } else {
         ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new SendPagePacket(ServerWardrobe.getServerWardrobe(), msg.searchBar, msg.pageSort, msg.isAscending, msg.page, msg.requestPagePacket));
      }
   }
}