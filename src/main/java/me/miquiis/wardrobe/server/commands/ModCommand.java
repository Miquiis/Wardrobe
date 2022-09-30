package me.miquiis.wardrobe.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.database.server.Database;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.UUID;

public class ModCommand {

    public ModCommand(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(Commands.literal("wardrobe").requires(commandSource -> commandSource.hasPermissionLevel(1))
                .then(Commands.literal("load")
                        .then(Commands.argument("url", StringArgumentType.string()).executes(context -> {
                            ServerPlayerEntity serverPlayer = context.getSource().asPlayer();

                            SkinChangerAPI.setPlayerSkin(serverPlayer, new SkinLocation(UUID.randomUUID().toString(), StringArgumentType.getString(context, "url")));
                            return 1;
                        })
                        .then(Commands.argument("isSlim", BoolArgumentType.bool()).executes(context -> {
                            ServerPlayerEntity serverPlayer = context.getSource().asPlayer();
                            SkinChangerAPI.setPlayerSkin(serverPlayer, new SkinLocation(UUID.randomUUID().toString(), StringArgumentType.getString(context, "url"), BoolArgumentType.getBool(context, "isSlim")));
                            return 1;
                        }))
                ))
                .then(Commands.literal("reset").executes(context -> {
                    ServerPlayerEntity serverPlayer = context.getSource().asPlayer();
                    SkinChangerAPI.clearPlayerSkin(serverPlayer);
                    return 1;
                }))
                .then(Commands.literal("loadFromDatabase").then(Commands.argument("skinId", StringArgumentType.string()).executes(context -> {
                    new Database().fetchSkinLocation(StringArgumentType.getString(context, "skinId")).thenAcceptAsync(skinLocation ->
                    {
                        ServerPlayerEntity serverPlayer;
                        try {
                            serverPlayer = context.getSource().asPlayer();
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        SkinChangerAPI.setPlayerSkin(serverPlayer, skinLocation);
                    });
                    return 1;
                })))
                .then(Commands.literal("fetchSkin").then(Commands.argument("skinId", StringArgumentType.string()).executes(context -> {
                    new Database().fetchSkinURL(StringArgumentType.getString(context, "skinId")).thenAcceptAsync(s ->
                    {
                        try {
                            context.getSource().asPlayer().sendStatusMessage(new StringTextComponent(s), false);
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return 1;
                })))
                .then(Commands.literal("saveSkin").then(Commands.argument("skinId", StringArgumentType.string()).then(Commands.argument("skinURL", StringArgumentType.string()).then(Commands.argument("isSlim", BoolArgumentType.bool()).executes(context -> {
                    new Database().saveSkinURL(StringArgumentType.getString(context, "skinId"), StringArgumentType.getString(context, "skinURL"), BoolArgumentType.getBool(context, "isSlim"));
                    return 1;
                })))))
        );
    }

}
