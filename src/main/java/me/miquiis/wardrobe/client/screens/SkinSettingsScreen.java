package me.miquiis.wardrobe.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.PersonalWardrobe;
import me.miquiis.wardrobe.common.WardrobeFolder;
import me.miquiis.wardrobe.common.WardrobeTab;
import me.miquiis.wardrobe.common.utils.Payload;
import me.miquiis.wardrobe.database.server.Database;
import me.miquiis.wardrobe.server.network.ModNetwork;
import me.miquiis.wardrobe.server.network.messages.AddSkinToDatabasePacket;
import me.miquiis.wardrobe.server.network.messages.ModifySkinToDatabasePacket;
import me.miquiis.wardrobe.server.network.messages.RemoveSkinFromDatabasePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.io.FilenameUtils;

public class SkinSettingsScreen extends Screen implements PopUpScreen.IPopUpScreen {

    private static final ResourceLocation WARDROBE_SETTINGS = new ResourceLocation(Wardrobe.MOD_ID, "textures/gui/wardrobe_settings_icons.png");

    private PopUpScreen popUpScreen;
    private SkinLocation skinLocation;
    private WardrobeTab currentTab;
    private WardrobeFolder currentFolder;

    private int guiLeft;
    private int guiTop;

    private TextFieldWidget skinNameField;
    private TextFieldWidget skinUrlField;
    private CheckboxButton isSlimBox;
    private CheckboxButton isBabyBox;

    private Button saveButton;
    private Button deleteButton;

    public SkinSettingsScreen(SkinLocation skinLocation, WardrobeTab currentTab, WardrobeFolder currentFolder) {
        super(new StringTextComponent("Skin Settings"));
        this.skinLocation = skinLocation;
        this.currentTab = currentTab;
        this.currentFolder = currentFolder;
    }

    @Override
    public void tick() {
        super.tick();
        skinNameField.tick();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String prevTextName = skinNameField.getText();
        String prevURL = skinUrlField.getText();
        boolean prevSlim = isSlimBox.isChecked();
        boolean prevBaby = isBabyBox.isChecked();
        super.resize(minecraft, width, height);
        skinNameField.setText(prevTextName);
        skinUrlField.setText(prevURL);
        if (prevSlim == !isSlimBox.isChecked()) isSlimBox.onPress();
        if (prevBaby == !isBabyBox.isChecked()) isBabyBox.onPress();
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
        this.skinNameField.setText(skinLocation.getSkinId());

        this.skinUrlField = new TextFieldWidget(this.font, this.guiLeft, this.guiTop, 80, 9, new TranslationTextComponent("itemGroup.search"));
        this.skinUrlField.setMaxStringLength(Integer.MAX_VALUE);
        this.skinUrlField.setEnableBackgroundDrawing(true);
        this.skinUrlField.setVisible(true);
        this.skinUrlField.setTextColor(16777215);
        this.skinUrlField.setHeight(18);
        this.skinUrlField.setWidth(150);
        this.skinUrlField.setCanLoseFocus(true);
        this.skinUrlField.x -= this.skinUrlField.getAdjustedWidth() / 2 + 4;
        this.skinUrlField.y -= this.skinUrlField.getHeight() / 2 + 40;
        this.skinUrlField.setText(currentTab == WardrobeTab.PERSONAL_WARDROBE ? FilenameUtils.getName(skinLocation.getSkinURL()) : skinLocation.getSkinURL());

        this.isSlimBox = new CheckboxButton(this.guiLeft, this.guiTop, 20, 20, new StringTextComponent(""), skinLocation.isSlim());
        this.isSlimBox.visible = true;
        this.isSlimBox.active = true;
        this.isSlimBox.x -= this.isSlimBox.getWidth() / 2 - 20;
        this.isSlimBox.y -= this.isSlimBox.getHeight() / 2 + 16;

        this.isBabyBox = new CheckboxButton(this.guiLeft, this.guiTop, 20, 20, new StringTextComponent(""), skinLocation.isBaby());
        this.isBabyBox.visible = true;
        this.isBabyBox.active = true;
        this.isBabyBox.x -= this.isBabyBox.getWidth() / 2 + 20;
        this.isBabyBox.y -= this.isBabyBox.getHeight() / 2 + 16;

        this.saveButton = addButton(new Button(this.guiLeft - 176 / 2, guiTop + 222 / 2, 60, 20, new StringTextComponent("Save"), p_onPress_1_ -> {
            if (currentTab == WardrobeTab.PERSONAL_WARDROBE)
            {
                SkinLocation newSkinLocation = new SkinLocation(skinNameField.getText(), skinUrlField.getText(), isSlimBox.isChecked(), isBabyBox.isChecked());
                PersonalWardrobe.modifySkin(skinLocation, newSkinLocation);
            } else if (currentTab == WardrobeTab.DATABASE_WARDROBE)
            {
                SkinLocation newSkinLocation = new SkinLocation(skinNameField.getText(), skinUrlField.getText(), isSlimBox.isChecked(), isBabyBox.isChecked());
                ModNetwork.CHANNEL.sendToServer(new ModifySkinToDatabasePacket(new Payload().put("PrevSkinLocation", SkinLocation.SKIN_LOCATION.write(skinLocation)).put("SkinLocation", SkinLocation.SKIN_LOCATION.write(newSkinLocation)).putString("FolderName", currentFolder.getWardrobeFolderName()).getPayload()));
            }
            popUpScreen.finish();
        }));

        this.deleteButton = addButton(new Button(this.guiLeft + 176 / 2, guiTop + 222 / 2, 60, 20, new StringTextComponent("\u00A7cDelete"), p_onPress_1_ -> {
            if (currentTab == WardrobeTab.PERSONAL_WARDROBE)
            {
                PersonalWardrobe.deleteSkin(skinLocation);
            } else if (currentTab == WardrobeTab.DATABASE_WARDROBE)
            {
                ModNetwork.CHANNEL.sendToServer(new RemoveSkinFromDatabasePacket(skinLocation));
            }
            popUpScreen.finish();
        }));

        this.deleteButton.x -= this.deleteButton.getWidth();

        this.children.add(skinNameField);
        this.children.add(skinUrlField);
        this.children.add(isSlimBox);
        this.children.add(isBabyBox);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        minecraft.textureManager.bindTexture(WARDROBE_SETTINGS);
        blit(matrixStack, width / 2 - 176 /2, height / 2 - 222 / 2, 176, 222, 0, 0, 176, 222, 256, 256);

        skinNameField.render(matrixStack, mouseX, mouseY, partialTicks);
        skinUrlField.render(matrixStack, mouseX, mouseY, partialTicks);
        isSlimBox.render(matrixStack, mouseX, mouseY, partialTicks);
        isBabyBox.render(matrixStack, mouseX, mouseY, partialTicks);

        if (isSlimBox.isHovered())
        {
            renderTooltip(matrixStack, new StringTextComponent("Has Small Arms?"), mouseX, mouseY);
        }

        if (isBabyBox.isHovered())
        {
            renderTooltip(matrixStack, new StringTextComponent("Is Baby?"), mouseX, mouseY);
        }

        font.drawStringWithShadow(matrixStack, "Skin Name", skinNameField.x, skinNameField.y - 12, 0xFFFFFF);
        font.drawStringWithShadow(matrixStack, "Skin URL", skinUrlField.x, skinUrlField.y - 12, 0xFFFFFF);

        WardrobeScreen.drawFakePlayerOnScreen(width / 2, height / 2 + 87, 40, mouseX, mouseY, skinLocation.getSkinLocation(), skinLocation.isSlim(), skinLocation.isBaby());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public PopUpScreen getPopUpScreen() {
        return popUpScreen;
    }

    @Override
    public void setPopUpScreen(PopUpScreen popUpScreen) {
        this.popUpScreen = popUpScreen;
    }
}
