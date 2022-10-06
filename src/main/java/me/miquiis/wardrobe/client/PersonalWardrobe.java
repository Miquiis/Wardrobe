package me.miquiis.wardrobe.client;

import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.client.SkinChangerAPIClient;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.managers.FileManager;
import me.miquiis.wardrobe.common.cache.TextureCache;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.CreeperChargeLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import sun.net.util.URLUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
                Wardrobe.getInstance().getClientTextureCache().cache(new TextureCache(Files.readAllBytes(skinFile.toPath()), ImageUtils.createImageHash(skinFile)));
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
            File skinFile = new File(texturesFolder, skinLocation.getSkinURL());
            if (skinFile.exists() && !FilenameUtils.getExtension(skinFile.getName()).isEmpty())
            {
                try {
                    Wardrobe.getInstance().getClientTextureCache().cache(new TextureCache(Files.readAllBytes(skinFile.toPath()), ImageUtils.createImageHash(skinFile)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return new SkinLocation(skinLocation.getSkinId(), skinFile.exists() ? skinFile.getAbsolutePath() : skinLocation.getSkinURL(), skinLocation.isSlim());
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
