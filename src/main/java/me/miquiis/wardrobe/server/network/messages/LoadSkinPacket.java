package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class LoadSkinPacket {

   private SkinLocation skinLocation;

   public LoadSkinPacket(SkinLocation skinLocation) {
      this.skinLocation = skinLocation;
   }

   public static LoadSkinPacket decodePacket(PacketBuffer buf) {
      return new LoadSkinPacket(SkinLocation.SKIN_LOCATION.read(buf));
   }

   public static void encodePacket(LoadSkinPacket packet, PacketBuffer buf) {
      SkinLocation.SKIN_LOCATION.write(buf, packet.skinLocation);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final LoadSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
      SkinChangerAPI.setPlayerSkin(ctx.get().getSender(), msg.skinLocation);
   }

   public SkinLocation getSkinLocation() {
      return skinLocation;
   }
}