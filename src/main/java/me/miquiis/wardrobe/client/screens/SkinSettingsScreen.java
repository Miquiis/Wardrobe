package me.miquiis.wardrobe.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SkinSettingsScreen extends Screen {

    private static final ResourceLocation WARDROBE_SETTINGS = new ResourceLocation(Wardrobe.MOD_ID, "textures/gui/wardrobe_settings_icons.png");

    private SkinLocation skinLocation;

    private int guiLeft;
    private int guiTop;

    private TextFieldWidget skinNameField;
    private TextFieldWidget skinUrlField;
    private CheckboxButton isSlimBox;

    public SkinSettingsScreen(SkinLocation skinLocation) {
        super(new StringTextComponent("Skin Settings"));
        this.skinLocation = skinLocation;
    }

    @Override
    public void tick() {
        super.tick();
        skinNameField.tick();
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = width / 2;
        this.guiTop = height / 2;

        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.skinNameField = new TextFieldWidget(this.font, this.guiLeft, this.guiTop, 80, 9, new TranslationTextComponent("itemGroup.search"));
        this.skinNameField.setMaxStringLength(50);
        this.skinNameField.setEnableBackgroundDrawing(true);
        this.skinNameField.setVisible(true);
        this.skinNameField.setTextColor(16777215);
        this.skinNameField.setHeight(18);
        this.skinNameField.setWidth(150);
        this.skinNameField.setCanLoseFocus(true);
        this.skinNameField.x -= this.skinNameField.getAdjustedWidth() / 2 + 4;
        this.skinNameField.y -= this.skinNameField.getHeight() / 2 + 80;

        this.skinUrlField = new TextFieldWidget(this.font, this.guiLeft, this.guiTop, 80, 9, new TranslationTextComponent("itemGroup.search"));
        this.skinUrlField.setMaxStringLength(50);
        this.skinUrlField.setEnableBackgroundDrawing(true);
        this.skinUrlField.setVisible(true);
        this.skinUrlField.setTextColor(16777215);
        this.skinUrlField.setHeight(18);
        this.skinUrlField.setWidth(150);
        this.skinUrlField.setCanLoseFocus(true);
        this.skinUrlField.x -= this.skinUrlField.getAdjustedWidth() / 2 + 4;
        this.skinUrlField.y -= this.skinUrlField.getHeight() / 2 + 40;

        this.isSlimBox = new CheckboxButton(this.guiLeft, this.guiTop, 20, 20, new StringTextComponent(""), false);
        this.isSlimBox.visible = true;
        this.isSlimBox.active = true;
        this.isSlimBox.x -= this.isSlimBox.getWidth() / 2;
        this.isSlimBox.y -= this.isSlimBox.getHeight() / 2 + 16;

        this.children.add(skinNameField);
        this.children.add(skinUrlField);
        this.children.add(isSlimBox);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        minecraft.textureManager.bindTexture(WARDROBE_SETTINGS);
        blit(matrixStack, width / 2 - 176 /2, height / 2 - 222 / 2, 176, 222, 0, 0, 176, 222, 256, 256);

        skinNameField.render(matrixStack, mouseX, mouseY, partialTicks);
        skinUrlField.render(matrixStack, mouseX, mouseY, partialTicks);
        isSlimBox.render(matrixStack, mouseX, mouseY, partialTicks);

        if (isSlimBox.isHovered())
        {
            renderTooltip(matrixStack, new StringTextComponent("Has Small Arms?"), mouseX, mouseY);
        }

        WardrobeScreen.drawFakePlayerOnScreen(width / 2, height / 2 + 87, 40, mouseX, mouseY, skinLocation.getSkinLocation(), skinLocation.isSlim());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
