package me.miquiis.wardrobe.server.network.messages;

import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class ClearSkinPacket {

   public ClearSkinPacket() {
   }

   public static ClearSkinPacket decodePacket(PacketBuffer buf) {
      return new ClearSkinPacket();
   }

   public static void encodePacket(ClearSkinPacket packet, PacketBuffer buf) {
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public static void handlePacket(final ClearSkinPacket msg, Supplier<NetworkEvent.Context> ctx) {
      SkinChangerAPI.clearPlayerSkin(ctx.get().getSender());
   }
}