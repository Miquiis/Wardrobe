package me.miquiis.wardrobe.common.events;

import com.mrcrayfish.obfuscate.common.data.SyncedPlayerData;
import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.utils.ImageUtils;
import me.miquiis.wardrobe.server.commands.ModCommand;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;
import org.apache.commons.codec.DecoderException;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Wardrobe.MOD_ID)
public class CommonForgeEvents {
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event)
    {
        new ModCommand(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerJoinEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (!event.getPlayer().world.isRemote)
        {
            SkinLocation skinLocation = SkinChangerAPI.getPlayerSkin(event.getPlayer());
            if (skinLocation.getSkinURL().startsWith("hex:"))
            {
                if (!Wardrobe.getInstance().getServerTextureCache().hasCache(cached -> {
                    try {
                        return ImageUtils.checkImagesHashes(ImageUtils.hexToBytes(skinLocation.getSkinURL().replace("hex:", "")), cached.getValue().getTextureHash());
                    } catch (DecoderException e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                {
                    SkinChangerAPI.clearPlayerSkin(event.getPlayer());
                }
            }
        }
    }
}
