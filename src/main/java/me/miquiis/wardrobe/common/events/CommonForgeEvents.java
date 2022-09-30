package me.miquiis.wardrobe.common.events;

import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.server.commands.ModCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Wardrobe.MOD_ID)
public class CommonForgeEvents {
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event)
    {
        new ModCommand(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }
}
