package me.miquiis.wardrobe.client.events;

import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.screens.WardrobeScreen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Wardrobe.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onScreenOpen(GuiOpenEvent event)
    {
        if (event.getGui() instanceof ChestScreen)
        {
            event.setGui(new WardrobeScreen(new StringTextComponent("Wardrobe")));
        }
    }

}
