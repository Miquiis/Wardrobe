package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.wardrobe.common.WardrobeFolder;
import me.miquiis.wardrobe.common.utils.Payload;
import me.miquiis.wardrobe.database.server.Database;
import me.miquiis.wardrobe.server.ServerWardrobe;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class RequestFoldersPacket {

   private CompoundNBT payload;

   /**
    * Payload Structure:
    * {@code
    *     {
    *         "StartingAt": Int
    *     }
    * }
    */
   public RequestFoldersPacket(CompoundNBT payload) {
      this.payload = payload;
   }

   public static void encodePacket(RequestFoldersPacket packet, PacketBuffer buf) {
      buf.writeCompoundTag(packet.payload);
   }

   public static RequestFoldersPacket decodePacket(PacketBuffer buf) {
      return new RequestFoldersPacket(buf.readCompoundTag());
   }

   public static void handlePacket(final RequestFoldersPacket msg, Supplier<NetworkEvent.Context> ctx) {
      new Database().fetchFolders(msg.payload.getInt("StartingAt")).thenAcceptAsync(wardrobeFolders -> {
         ListNBT listNBT = new ListNBT();
         wardrobeFolders.forEach(wardrobeFolder -> listNBT.add(WardrobeFolder.write(wardrobeFolder)));
         ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new SendFoldersPacket(new Payload().put("Folders", listNBT).getPayload()));
      });
   }
}