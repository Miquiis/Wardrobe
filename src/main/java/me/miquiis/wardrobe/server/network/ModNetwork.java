package me.miquiis.wardrobe.server.network;

import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.server.network.messages.ClearSkinPacket;
import me.miquiis.wardrobe.server.network.messages.DownloadSkinPacket;
import me.miquiis.wardrobe.server.network.messages.LoadSkinPacket;
import me.miquiis.wardrobe.server.network.messages.RequestPagePacket;
import me.miquiis.wardrobe.server.network.messages.RequestSkinPacket;
import me.miquiis.wardrobe.server.network.messages.RequestSkinDownloadPacket;
import me.miquiis.wardrobe.server.network.messages.RequestSkinUploadPacket;
import me.miquiis.wardrobe.server.network.messages.SendPagePacket;
import me.miquiis.wardrobe.server.network.messages.SendSkinChangePacket;
import me.miquiis.wardrobe.server.network.messages.UploadSkinPacket;
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
        CHANNEL.registerMessage(3, ClearSkinPacket.class, ClearSkinPacket::encodePacket, ClearSkinPacket::decodePacket, ClearSkinPacket::handlePacket);
        CHANNEL.registerMessage(4, UploadSkinPacket.class, UploadSkinPacket::encodePacket, UploadSkinPacket::decodePacket, UploadSkinPacket::handlePacket);
        CHANNEL.registerMessage(5, RequestSkinPacket.class, RequestSkinPacket::encodePacket, RequestSkinPacket::decodePacket, RequestSkinPacket::handlePacket);
        CHANNEL.registerMessage(6, RequestSkinUploadPacket.class, RequestSkinUploadPacket::encodePacket, RequestSkinUploadPacket::decodePacket, RequestSkinUploadPacket::handlePacket);
        CHANNEL.registerMessage(7, DownloadSkinPacket.class, DownloadSkinPacket::encodePacket, DownloadSkinPacket::decodePacket, DownloadSkinPacket::handlePacket);
        CHANNEL.registerMessage(8, SendSkinChangePacket.class, SendSkinChangePacket::encodePacket, SendSkinChangePacket::decodePacket, SendSkinChangePacket::handlePacket);
        CHANNEL.registerMessage(9, RequestSkinDownloadPacket.class, RequestSkinDownloadPacket::encodePacket, RequestSkinDownloadPacket::decodePacket, RequestSkinDownloadPacket::handlePacket);
    }

}
