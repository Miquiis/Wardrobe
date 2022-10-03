package me.miquiis.wardrobe.client;

import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.screens.WardrobeScreen;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.WardrobeTab;
import me.miquiis.wardrobe.server.network.messages.SendPagePacket;
import net.minecraft.client.Minecraft;

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
}
