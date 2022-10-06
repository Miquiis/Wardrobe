package me.miquiis.wardrobe.client;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.managers.FileManager;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.cache.TextureCache;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;
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

        if (skinsFolder.exists())
        {
            try {
                Files.setAttribute(skinsFolder.toPath(), "dos:hidden", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        init(true);
    }

    public static void modifySkin(SkinLocation previousSkinLocation, SkinLocation newSkinLocation)
    {
        deleteSkin(previousSkinLocation);
        addSkin(newSkinLocation);
    }

    public static void addSkin(SkinLocation skinLocation)
    {
        skinsFileManager.saveObject(skinLocation.getSkinId(), new SkinLocation(skinLocation.getSkinId(), skinLocation.getSkinURL(), null, skinLocation.isSlim()));
        File skinFile = new File(skinLocation.getSkinURL());
        if (!skinFile.exists()) skinFile = new File(texturesFolder, skinLocation.getSkinURL());
        if (skinFile.exists())
        {
            try {
                Wardrobe.getInstance().getClientTextureCache().cache(new TextureCache(Files.readAllBytes(skinFile.toPath()), ImageUtils.createImageHash(skinFile), skinLocation.isSlim()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteSkin(SkinLocation skinLocation)
    {
        skinsFileManager.deleteObject(skinLocation.getSkinId(), false);
        Minecraft.getInstance().textureManager.deleteTexture(skinLocation.getSkinLocation());
        File skinFile = new File(skinLocation.getSkinURL());
        Wardrobe.getInstance().getClientTextureCache().decache(cached -> Arrays.equals(cached.getValue().getTextureHash(), ImageUtils.createImageHash(skinFile)));
    }

    public static boolean getSkinIsSlimByHash(byte[] skinHash)
    {
        for (SkinLocation skinLocation : personalWardrobe) {
            File textureLocation = new File(skinLocation.getSkinURL());
            if (textureLocation.exists())
            {
                if (ImageUtils.checkImagesHashes(ImageUtils.createImageHash(textureLocation), skinHash))
                {
                    return skinLocation.isSlim();
                }
            }
        }
        return false;
    }

    public static byte[] getSkinTextureByHash(byte[] skinHash)
    {
        for (File file : texturesFolder.listFiles()) {
            try {
                if (Arrays.equals(ImageUtils.createImageHash(file), skinHash))
                {
                    return Files.readAllBytes(file.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void init(boolean forceInit)
    {
        if (forceInit) skinsFileManager = new FileManager("personal-wardrobe/skins");
        personalWardrobe.clear();
        Wardrobe.getInstance().getClientTextureCache().clearCache();
        skinsFileManager.saveObject("default", new SkinLocation("default", "", null, false));
        List<SkinLocation> skinLocations = skinsFileManager.loadObjects(SkinLocation.class);
        personalWardrobe.addAll(skinLocations.stream().map(skinLocation -> {
            File textureFile = new File(texturesFolder, skinLocation.getSkinURL());
            if (textureFile.exists() && !FilenameUtils.getExtension(textureFile.getName()).isEmpty())
            {
                try {
                    Wardrobe.getInstance().getClientTextureCache().cache(new TextureCache(Files.readAllBytes(textureFile.toPath()), ImageUtils.createImageHash(textureFile), skinLocation.isSlim()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try
            {
                return new SkinLocation(skinLocation.getSkinId(), textureFile.exists() ? textureFile.getAbsolutePath() : skinLocation.getSkinURL(), skinLocation.isSlim());
            } catch (Exception e)
            {
                return SkinLocation.EMPTY;
            }
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

    public static boolean hasNextPage(int lastIndex,  String filterName, WardrobePage.PageSort pageSort, boolean isAscending)
    {
        return getPersonalWardrobeFrom(lastIndex, filterName, pageSort, isAscending).size() > 0;
    }

    public static List<SkinLocation> getPersonalWardrobeFrom(int lastIndex, String filterName, WardrobePage.PageSort pageSort, boolean isAscending)
    {
        List<SkinLocation> sortedWardrobe = getSortedWardrobe(filterName, pageSort, isAscending);
        if (sortedWardrobe.size() >= lastIndex)
        {
            return new ArrayList<>(sortedWardrobe.subList(lastIndex, Math.min(lastIndex + 16, sortedWardrobe.size())));
        } else {
            return new ArrayList<>();
        }
    }

    private static List<SkinLocation> getSortedWardrobe(String filterName, WardrobePage.PageSort pageSort, boolean isAscending)
    {
        List<SkinLocation> currentWardrobe = personalWardrobe.stream().filter(
                skinLocation -> skinLocation.getSkinId().contains(filterName))
                .sorted((o1, o2) -> {
                    if (pageSort == WardrobePage.PageSort.ALPHABETIC)
                    {
                        return Comparator.comparing(SkinLocation::getSkinId).compare(o1, o2);
                    } else if (pageSort == WardrobePage.PageSort.SLIM)
                    {
                        return Comparator.comparing(SkinLocation::isSlim).compare(o1, o2);
                    } else if (pageSort == WardrobePage.PageSort.LAST_UPDATED)
                    {
                        try
                        {
                            FileTime o1Time = (FileTime) Files.getAttribute(new File(skinsFolder, o1.getSkinId() + ".json").toPath(), "creationTime");
                            FileTime o2Time = (FileTime) Files.getAttribute(new File(skinsFolder, o2.getSkinId() + ".json").toPath(), "creationTime");
                            return o2Time.compareTo(o1Time);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return 0;
                })
                .collect(Collectors.toList());
        if (!isAscending)
        {
            Collections.reverse(currentWardrobe);
        }
        return currentWardrobe;
    }

    public static List<SkinLocation> getPersonalWardrobe() {
        return new ArrayList<>(personalWardrobe);
    }
}
