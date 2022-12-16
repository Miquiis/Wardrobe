package me.miquiis.wardrobe.common;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

public class WardrobeFolder {

    public static WardrobeFolder MAIN_FOLDER = new WardrobeFolder( "Main Folder", "chest", WardrobeTab.DATABASE_WARDROBE, 1);

    private final String wardrobeFolderName;
    private final String wardrobeIconResource;
    private final WardrobeTab wardrobeTab;
    private final int wardrobeFolderPage;

    public WardrobeFolder(String wardrobeFolderName, String wardrobeIconResource, WardrobeTab wardrobeTab, int wardrobeFolderPage) {
        this.wardrobeFolderName = wardrobeFolderName;
        this.wardrobeIconResource = wardrobeIconResource;
        this.wardrobeTab = wardrobeTab;
        this.wardrobeFolderPage = wardrobeFolderPage;
    }

    public String getWardrobeFolderName() {
        return wardrobeFolderName;
    }

    public String getWardrobeIconResource() {
        return wardrobeIconResource;
    }

    public WardrobeTab getWardrobeTab() {
        return wardrobeTab;
    }

    public int getWardrobeFolderPage() {
        return wardrobeFolderPage;
    }

    public static INBT write(WardrobeFolder wardrobeFolder) {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putString("FolderName", wardrobeFolder.wardrobeFolderName);
        compoundNBT.putString("FolderIcon", wardrobeFolder.wardrobeIconResource);
        compoundNBT.putInt("FolderTab", wardrobeFolder.wardrobeTab.ordinal());
        compoundNBT.putInt("FolderPage", wardrobeFolder.wardrobeFolderPage);
        return compoundNBT;
    }

    public static WardrobeFolder read(INBT inbt) {
        CompoundNBT compoundNBT = (CompoundNBT)inbt;
        return new WardrobeFolder(compoundNBT.getString("FolderName"), compoundNBT.getString("FolderIcon"), WardrobeTab.values()[compoundNBT.getInt("FolderTab")], compoundNBT.getInt("FolderPage"));
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj))
        {
            return true;
        } else if (obj instanceof WardrobeFolder)
        {
            return write(this).equals(write((WardrobeFolder)obj));
        }
        return false;
    }
}
