package me.miquiis.wardrobe.client;

import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.client.SkinChangerAPIClient;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.screens.WardrobeScreen;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.WardrobeTab;
import me.miquiis.wardrobe.common.cache.TextureCache;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import me.miquiis.wardrobe.server.network.ModNetwork;
import me.miquiis.wardrobe.server.network.messages.DownloadSkinPacket;
import me.miquiis.wardrobe.server.network.messages.RequestSkinUploadPacket;
import me.miquiis.wardrobe.server.network.messages.SendPagePacket;
import me.miquiis.wardrobe.server.network.messages.SendSkinChangePacket;
import me.miquiis.wardrobe.server.network.messages.UploadSkinPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.codec.digest.DigestUtils;

import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PacketHandler {

    public static void handleSendPagePacket(SendPagePacket msg) {
        Wardrobe.getInstance().getClientWardrobePageCache().cache(new WardrobePage(msg.getSearchBar(),
                msg.getPageSort(), msg.isAscending(), msg.getPageContents(), WardrobeTab.SERVER_WARDROBE, msg.getPage()
        ), cached -> cached.getValue().getSearchBar().equals(msg.getSearchBar()) && cached.getValue().isAscending() == msg.isAscending() && cached.getValue().getPageSorted() == msg.getPageSort() && cached.getValue().getPage() == msg.getPage());

        if (Minecraft.getInstance().currentScreen instanceof WardrobeScreen)
        {
            ((WardrobeScreen)Minecraft.getInstance().currentScreen).refreshPage(false);
        }
    }

    public static void handleRequestSkinUploadPacket(RequestSkinUploadPacket msg) {
        System.out.println("Handling Skin Upload Packet");
        byte[] textureBytes = PersonalWardrobe.getSkinTextureByHash(msg.getSkinHash());
        if (textureBytes != null)
        {
            System.out.println("Uploading Skin to Server");
            ModNetwork.CHANNEL.sendToServer(new UploadSkinPacket(textureBytes, msg.getSkinHash()));
        }
    }

    public static void handleSendSkinChangePacket(SendSkinChangePacket msg) {
        if (Wardrobe.getInstance().getClientTextureCache().hasCache(cached -> Arrays.equals(cached.getValue().getTextureHash(), msg.getSkinHash())))
        {
            // Change Skin
        } else {
            // Request Skin Download
            System.out.println("Requesting Skin from Server");
        }
    }

    public static void handleDownloadSkinPacket(DownloadSkinPacket msg) {
        try
        {
            byte[] skinHash = ImageUtils.createImageHash(msg.getSkinBytes());
            if (!Wardrobe.getInstance().getClientTextureCache().hasCache(cached -> Arrays.equals(cached.getValue().getTextureHash(), skinHash)))
            {
                Wardrobe.getInstance().getClientTextureCache().cache(new TextureCache(msg.getSkinBytes(), skinHash));
                SkinLocation skinLocation = new SkinLocation(ImageUtils.byteToHex(skinHash), "hex:" + ImageUtils.byteToHex(skinHash), false);
                Minecraft.getInstance().textureManager.deleteTexture(skinLocation.getSkinLocation());
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
