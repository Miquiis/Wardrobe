package me.miquiis.wardrobe;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.client.ClientKeybinds;
import me.miquiis.wardrobe.common.WardrobeFolder;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.Configs;
import me.miquiis.wardrobe.common.ref.ModInformation;
import me.miquiis.wardrobe.database.LocalCache;
import me.miquiis.wardrobe.database.server.Database;
import me.miquiis.wardrobe.common.cache.TextureCache;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(ModInformation.MOD_ID)
public class Wardrobe
{
    private static Wardrobe instance;
    public static final String MOD_ID = ModInformation.MOD_ID;

    private LocalCache<SkinLocation> serverSkinLocationCache;
    private LocalCache<TextureCache> serverTextureCache;

    private LocalCache<WardrobeFolder> clientWardrobeFolderCache;
    private LocalCache<WardrobePage> clientWardrobePageCache;
    private LocalCache<TextureCache> clientTextureCache;

    public Wardrobe() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.DATABASE_CONFIG_SPEC);
        Configs.loadConfig(Configs.DATABASE_CONFIG_SPEC, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-common.toml").toString());
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        ClientKeybinds.registerBindings();
        clientWardrobePageCache = new LocalCache<>();
        clientTextureCache = new LocalCache<>();
        clientWardrobeFolderCache = new LocalCache<>();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        instance = this;
        ModNetwork.init();
    }

    @SubscribeEvent
    public void serverStartup(final FMLServerStartingEvent event)
    {
        try
        {
            new Database().firstBoot();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        serverSkinLocationCache = new LocalCache<>();
        serverTextureCache = new LocalCache<>();
    }

    public LocalCache<SkinLocation> getServerSkinLocationCache() {
        return serverSkinLocationCache;
    }

    public LocalCache<TextureCache> getServerTextureCache() {
        return serverTextureCache;
    }

    public LocalCache<TextureCache> getClientTextureCache() {
        return clientTextureCache;
    }

    public LocalCache<WardrobePage> getClientWardrobePageCache() {
        return clientWardrobePageCache;
    }

    public LocalCache<WardrobeFolder> getClientWardrobeFolderCache() {
        return clientWardrobeFolderCache;
    }

    public static Wardrobe getInstance() {
        return instance;
    }
}
