package me.miquiis.wardrobe.client.events;

import me.miquiis.skinchangerapi.client.DownloadingTexture;
import me.miquiis.skinchangerapi.client.LoadSkinTextureEvent;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.ClientKeybinds;
import me.miquiis.wardrobe.client.LoadingCachedTexture;
import me.miquiis.wardrobe.client.screens.WardrobeScreen;
import me.miquiis.wardrobe.server.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.BeeAngrySound;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Wardrobe.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onPress(final InputEvent.KeyInputEvent event) {
        final KeyBinding[] keyBindings = ClientKeybinds.keyBindings;
        if (keyBindings[0].isPressed()) {
            Minecraft.getInstance().player.playSound(SoundEvents.BLOCK_CHEST_OPEN, 0.2f, 1f);
            Minecraft.getInstance().displayGuiScreen(new WardrobeScreen(new StringTextComponent("Wardrobe")));
        }
    }

    @SubscribeEvent
    public static void onCustomLoadSkin(LoadSkinTextureEvent.Pre event)
    {
        if (event.getSkinLocation().getSkinURL().startsWith("hex:"))
        {
            event.setCanceled(true);
            Minecraft minecraft = Minecraft.getInstance();
            try (LoadingCachedTexture downloadingTexture = new LoadingCachedTexture(null, event.getSkinLocation().getSkinURL().replace("hex:", ""), event.getSkinLocation().isSlim(), DefaultPlayerSkin.getDefaultSkinLegacy(), true, null)) {
                minecraft.getTextureManager().loadTexture(event.getSkinLocation().getSkinLocation(), downloadingTexture);
            }
        }
    }

}
