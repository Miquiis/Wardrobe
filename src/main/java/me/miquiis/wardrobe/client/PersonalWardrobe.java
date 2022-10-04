package me.miquiis.wardrobe.client;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.managers.FileManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Wardrobe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PersonalWardrobe {

    private static FileManager skinsFileManager;
    private static File personalWardrobeFolder;
    private static File skinsFolder;
    private static File texturesFolder;

    private static final List<SkinLocation> personalWardrobe = new ArrayList<>();

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event)
    {
        personalWardrobeFolder = new File(event.getMinecraftSupplier().get().gameDir, "personal-wardrobe");
        texturesFolder = new File(personalWardrobeFolder, "textures");
        skinsFolder = new File(personalWardrobeFolder, "skins");
        if (!personalWardrobeFolder.exists()) personalWardrobeFolder.mkdir();
        if (!texturesFolder.exists()) texturesFolder.mkdir();
        if (!skinsFolder.exists()) skinsFolder.mkdir();

        init(true);
    }

    public static void modifySkin(SkinLocation skinLocation)
    {
    }

    private static void init(boolean forceInit)
    {
        if (forceInit) skinsFileManager = new FileManager("personal-wardrobe/skins");
        personalWardrobe.clear();
        skinsFileManager.saveObject("default", new SkinLocation("default"));
        List<SkinLocation> skinLocations = skinsFileManager.loadObjects(SkinLocation.class);
        personalWardrobe.addAll(skinLocations.stream().map(skinLocation -> {
            File skinFile = new File(texturesFolder, skinLocation.getSkinURL());
            return new SkinLocation(skinLocation.getSkinId(), skinFile.getAbsolutePath());
        }).collect(Collectors.toList()));
        autoGenerateSkins();
    }

    public static void refreshWardrobe()
    {
        init(false);
    }

    private static void autoGenerateSkins()
    {
        File[] textures = texturesFolder.listFiles();
        List<File> unusedTextures = Arrays.stream(textures).filter(file -> personalWardrobe.stream().noneMatch(skinLocation -> FilenameUtils.getName(skinLocation.getSkinURL()).equals(FilenameUtils.getName(file.getName())))).collect(Collectors.toList());
        unusedTextures.forEach(file -> {
            SkinLocation skinLocation = new SkinLocation(FilenameUtils.getBaseName(file.getName()), FilenameUtils.getName(file.getAbsolutePath()), null);
            skinsFileManager.saveObject(FilenameUtils.getBaseName(file.getName()), skinLocation);
        });
        if (unusedTextures.size() > 0) init(false);
    }

    public static List<SkinLocation> getPersonalWardrobe() {
        return new ArrayList<>(personalWardrobe);
    }
}
