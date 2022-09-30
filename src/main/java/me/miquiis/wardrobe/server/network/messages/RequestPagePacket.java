package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.database.server.Database;
import me.miquiis.wardrobe.database.server.MySQL;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class RequestPagePacket {

   private WardrobePage.PageSort pageSort;
   private boolean isAscending;
   private int page;
   private int startsAt;

   public RequestPagePacket(WardrobePage.PageSort pageSort, boolean isAscending, int page, int startsAt) {
      this.pageSort = pageSort;
      this.isAscending = isAscending;
      this.page = page;
      this.startsAt = startsAt;
   }

   public static void encodePacket(RequestPagePacket packet, PacketBuffer buf) {
      buf.writeEnumValue(packet.pageSort).writeBoolean(packet.isAscending).writeInt(packet.page).writeInt(packet.startsAt);
   }

   public static RequestPagePacket decodePacket(PacketBuffer buf) {
      return new RequestPagePacket(buf.readEnumValue(WardrobePage.PageSort.class), buf.readBoolean(), buf.readInt(), buf.readInt());
   }

   public static void handlePacket(final RequestPagePacket msg, Supplier<NetworkEvent.Context> ctx) {
      new Database().fetchPage(msg.pageSort, msg.isAscending, msg.startsAt).thenAcceptAsync(skinLocations -> {
         ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new SendPagePacket(skinLocations, msg.pageSort, msg.isAscending, msg.page));
      });
   }
}