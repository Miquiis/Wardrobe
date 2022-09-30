package me.miquiis.wardrobe.server.network;

import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.server.network.messages.LoadSkinPacket;
import me.miquiis.wardrobe.server.network.messages.RequestPagePacket;
import me.miquiis.wardrobe.server.network.messages.SendPagePacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ModNetwork {

    public static final String NETWORK_VERSION = "1.0.0";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Wardrobe.MOD_ID, "network"), () -> NETWORK_VERSION,
            version -> version.equals(NETWORK_VERSION), version -> version.equals(NETWORK_VERSION)
    );

    public static void init() {
        CHANNEL.registerMessage(0, LoadSkinPacket.class, LoadSkinPacket::encodePacket, LoadSkinPacket::decodePacket, LoadSkinPacket::handlePacket);
        CHANNEL.registerMessage(1, RequestPagePacket.class, RequestPagePacket::encodePacket, RequestPagePacket::decodePacket, RequestPagePacket::handlePacket);
        CHANNEL.registerMessage(2, SendPagePacket.class, SendPagePacket::encodePacket, SendPagePacket::decodePacket, SendPagePacket::handlePacket);
    }

}
