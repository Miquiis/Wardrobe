package me.miquiis.wardrobe.server;

import me.miquiis.skinchangerapi.common.SkinLocation;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class ServerWardrobe {

    private static final List<SkinLocation> serverWardrobe = new ArrayList<>();

    public static void addSkin(SkinLocation skinLocation)
    {
        if (serverWardrobe.stream().noneMatch(skinLocation1 -> skinLocation1.getSkinId().equals(skinLocation.getSkinId())))
        {
            serverWardrobe.add(skinLocation);
        }
    }

    public static void removeSkin(SkinLocation skinLocation)
    {
        serverWardrobe.removeIf(skinLocation1 -> skinLocation1.getSkinId().equals(skinLocation.getSkinId()));
    }

    public static List<SkinLocation> getServerWardrobe()
    {
        return new ArrayList<>(serverWardrobe);
    }

}
