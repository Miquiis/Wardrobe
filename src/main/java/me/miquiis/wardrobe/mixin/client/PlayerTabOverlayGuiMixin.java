package me.miquiis.wardrobe.mixin.client;

import com.mojang.authlib.GameProfile;
import me.miquiis.skinchangerapi.SkinChangerAPI;
import me.miquiis.skinchangerapi.common.SkinLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerTabOverlayGui.class)
public class PlayerTabOverlayGuiMixin {

    @Shadow
    @Final
    private Minecraft mc;

    @Redirect(method = "func_238523_a_", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/play/NetworkPlayerInfo;getLocationSkin()Lnet/minecraft/util/ResourceLocation;"))
    private ResourceLocation _getLocationSkin(NetworkPlayerInfo instance)
    {
        GameProfile gameprofile = instance.getGameProfile();
        PlayerEntity playerentity = mc.world.getPlayerByUuid(gameprofile.getId());
        if (playerentity != null)
        {
            SkinLocation skinLocation = SkinChangerAPI.getPlayerSkin(playerentity);
            if (skinLocation != null && !skinLocation.equals(SkinLocation.EMPTY))
            {
                return skinLocation.getSkinLocation();
            }
        }
        return instance.getLocationSkin();
    }


}
