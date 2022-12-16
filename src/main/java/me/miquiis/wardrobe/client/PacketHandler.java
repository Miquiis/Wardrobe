package me.miquiis.wardrobe.client;

import me.miquiis.skinchangerapi.client.SkinChangerAPIClient;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.screens.WardrobeScreen;
import me.miquiis.wardrobe.common.WardrobeFolder;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.WardrobeTab;
import me.miquiis.wardrobe.common.cache.TextureCache;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import me.miquiis.wardrobe.server.network.ModNetwork;
import me.miquiis.wardrobe.server.network.messages.*;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PacketHandler {

    public static void handleSendPagePacket(SendPagePacket msg) {
        Wardrobe.getInstance().getClientWardrobePageCache().cache(new WardrobePage(msg.getSearchBar(),
                msg.getPageSort(), msg.isAscending(), msg.getPageContents(), msg.getRequestPagePacket() == RequestPagePacket.RequestPagePacketType.DATABASE ? WardrobeTab.DATABASE_WARDROBE : WardrobeTab.SERVER_WARDROBE, msg.getPage()
        ), cached -> cached.getValue().getSearchBar().equals(msg.getSearchBar()) && cached.getValue().isAscending() == msg.isAscending() && cached.getValue().getPageSorted() == msg.getPageSort() && cached.getValue().getPage() == msg.getPage());

        if (Minecraft.getInstance().currentScreen instanceof WardrobeScreen)
        {
            ((WardrobeScreen)Minecraft.getInstance().currentScreen).setHasNextPage(msg.hasNextPage());
            ((WardrobeScreen)Minecraft.getInstance().currentScreen).refreshPage(false);
        }
    }

    public static void handleRequestSkinUploadPacket(RequestSkinUploadPacket msg) {
        byte[] textureBytes = PersonalWardrobe.getSkinTextureByHash(msg.getSkinHash());
        if (textureBytes != null)
        {
            ModNetwork.CHANNEL.sendToServer(new UploadSkinPacket(textureBytes, msg.getSkinHash(), PersonalWardrobe.getSkinIsSlimByHash(msg.getSkinHash()), PersonalWardrobe.getSkinIsBabyByHash(msg.getSkinHash()), msg.getSkinUploadPacketType()));
        }
    }

    public static void handleSendSkinChangePacket(SendSkinChangePacket msg) {
        if (Wardrobe.getInstance().getClientTextureCache().hasCache(cached -> Arrays.equals(cached.getValue().getTextureHash(), msg.getSkinHash())))
        {
            // Change Skin
        } else {
            // Request Skin Download
        }
    }

    public static void handleDownloadSkinPacket(DownloadSkinPacket msg) {
        try
        {
            byte[] skinHash = ImageUtils.createImageHash(msg.getSkinBytes());
            if (!Wardrobe.getInstance().getClientTextureCache().hasCache(cached -> Arrays.equals(cached.getValue().getTextureHash(), skinHash) && cached.getValue().isTextureIsSlim() == msg.isSkinIsSlim() && cached.getValue().isTextureIsBaby() == msg.isSkinIsBaby()))
            {
                Wardrobe.getInstance().getClientTextureCache().cache(new TextureCache(msg.getSkinBytes(), skinHash, msg.isSkinIsSlim(), msg.isSkinIsBaby()));
                SkinLocation skinLocation = new SkinLocation(ImageUtils.byteToHex(skinHash), "hex:" + ImageUtils.byteToHex(skinHash), msg.isSkinIsSlim(), msg.isSkinIsBaby());
                Minecraft.getInstance().textureManager.deleteTexture(skinLocation.getSkinLocation());
                SkinChangerAPIClient.loadSkin(skinLocation);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void handleSendFoldersPacket(SendFoldersPacket msg) {
        Wardrobe.getInstance().getClientWardrobeFolderCache().decache(cached -> cached.getValue().getWardrobeFolderPage() == msg.getPayload().getInt("FolderPage"));

        ListNBT listNBT = msg.getPayload().getList("Folders", Constants.NBT.TAG_COMPOUND);
        listNBT.forEach(inbt -> {
            WardrobeFolder wardrobeFolder = WardrobeFolder.read(inbt);
            Wardrobe.getInstance().getClientWardrobeFolderCache().cache(wardrobeFolder, cached -> cached.getValue().equals(wardrobeFolder));
        });

        if (Minecraft.getInstance().currentScreen instanceof WardrobeScreen)
        {
            WardrobeScreen wardrobeScreen = (WardrobeScreen)Minecraft.getInstance().currentScreen;
            wardrobeScreen.refreshSection(true, false, false);
        }
    }
}
