package me.miquiis.wardrobe.client;

import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.server.network.messages.SendPagePacket;

public class PacketHandler {

    public static void handleSendPagePacket(SendPagePacket msg) {
        Wardrobe.getInstance().getClientWardrobePageCache().cache(new WardrobePage(
                msg.getPageSort(), msg.isAscending(), msg.getPageContents(), msg.getPage()
        ));
    }
}
