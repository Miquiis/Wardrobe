package me.miquiis.wardrobe.common;

import me.miquiis.wardrobe.Wardrobe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

public class WardrobeFolder {

    private final int wardrobeFolderId;
    private final String wardrobeFolderName;
    private final String wardrobeIconResource;
    private final WardrobeTab wardrobeTab;
    private final int wardrobeFolderPage;

    public WardrobeFolder(int wardrobeFolderId, String wardrobeFolderName, String wardrobeIconResource, WardrobeTab wardrobeTab, int wardrobeFolderPage) {
        this.wardrobeFolderId = wardrobeFolderId;
        this.wardrobeFolderName = wardrobeFolderName;
        this.wardrobeIconResource = wardrobeIconResource;
        this.wardrobeTab = wardrobeTab;
        this.wardrobeFolderPage = wardrobeFolderPage;
    }

    public int getWardrobeFolderId() {
        return wardrobeFolderId;
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
        compoundNBT.putInt("FolderId", wardrobeFolder.wardrobeFolderId);
        compoundNBT.putString("FolderName", wardrobeFolder.wardrobeFolderName);
        compoundNBT.putString("FolderIcon", wardrobeFolder.wardrobeIconResource);
        compoundNBT.putInt("FolderTab", wardrobeFolder.wardrobeTab.ordinal());
        compoundNBT.putInt("FolderPage", wardrobeFolder.wardrobeFolderPage);
        return compoundNBT;
    }

    public static WardrobeFolder read(INBT inbt) {
        CompoundNBT compoundNBT = (CompoundNBT)inbt;
        return new WardrobeFolder(compoundNBT.getInt("FolderId"), compoundNBT.getString("FolderName"), compoundNBT.getString("FolderIcon"), WardrobeTab.values()[compoundNBT.getInt("FolderTab")], compoundNBT.getInt("FolderPage"));
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
