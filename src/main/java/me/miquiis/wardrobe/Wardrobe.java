package me.miquiis.wardrobe;

import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.Configs;
import me.miquiis.wardrobe.common.ref.ModInformation;
import me.miquiis.wardrobe.database.LocalCache;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

@Mod(ModInformation.MOD_ID)
public class Wardrobe
{
    private static Wardrobe instance;
    public static final String MOD_ID = ModInformation.MOD_ID;

    private LocalCache<SkinLocation> serverSkinLocationCache;
    private LocalCache<WardrobePage> clientWardrobePageCache;

    public Wardrobe() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Configs.SERVER_CONFIG_SPEC);
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        clientWardrobePageCache = new LocalCache<>();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        instance = this;
        ModNetwork.init();
    }

    @SubscribeEvent
    public void serverStartup(final FMLServerStartingEvent event)
    {
        serverSkinLocationCache = new LocalCache<>();
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    public LocalCache<SkinLocation> getServerSkinLocationCache() {
        return serverSkinLocationCache;
    }

    @OnlyIn(Dist.CLIENT)
    public LocalCache<WardrobePage> getClientWardrobePageCache() {
        return clientWardrobePageCache;
    }

    public static Wardrobe getInstance() {
        return instance;
    }
}
