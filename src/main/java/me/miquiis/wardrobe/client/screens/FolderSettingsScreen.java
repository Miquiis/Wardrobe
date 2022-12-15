package me.miquiis.wardrobe.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.common.WardrobeTab;
import me.miquiis.wardrobe.common.utils.Payload;
import me.miquiis.wardrobe.server.network.ModNetwork;
import me.miquiis.wardrobe.server.network.messages.AddFolderToDatabasePacket;
import me.miquiis.wardrobe.server.network.messages.DeleteFolderFromDatabasePacket;
import me.miquiis.wardrobe.server.network.messages.ModifyFolderFromDatabasePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public class FolderSettingsScreen extends Screen implements PopUpScreen.IPopUpScreen {

    private static final ResourceLocation WARDROBE_SETTINGS = new ResourceLocation(Wardrobe.MOD_ID, "textures/gui/folder_settings_icons.png");

    private PopUpScreen popUpScreen;

    private int guiLeft;
    private int guiTop;

    private TextFieldWidget folderNameField;
    private TextFieldWidget folderIconField;

    private Button saveButton;
    private Button deleteButton;

    private WardrobeTab currentTab;
    private boolean isEditing;

    public FolderSettingsScreen(WardrobeTab currentTab, boolean isEditing) {
        super(new StringTextComponent("Folder Settings"));
        this.currentTab = currentTab;
        this.isEditing = isEditing;
    }

    @Override
    public void tick() {
        super.tick();
        folderNameField.tick();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String prevTextName = folderNameField.getText();
        String prevURL = folderIconField.getText();
        super.resize(minecraft, width, height);
        folderNameField.setText(prevTextName);
        folderIconField.setText(prevURL);
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = width / 2;
        this.guiTop = height / 2;

        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.folderNameField = new TextFieldWidget(this.font, this.guiLeft, this.guiTop + 96 / 2 + 15, 80, 9, new TranslationTextComponent("itemGroup.search"));
        this.folderNameField.setMaxStringLength(50);
        this.folderNameField.setEnableBackgroundDrawing(true);
        this.folderNameField.setVisible(true);
        this.folderNameField.setTextColor(16777215);
        this.folderNameField.setHeight(18);
        this.folderNameField.setWidth(150);
        this.folderNameField.setCanLoseFocus(true);
        this.folderNameField.x -= this.folderNameField.getAdjustedWidth() / 2 + 4;
        this.folderNameField.y -= this.folderNameField.getHeight() / 2 + 80;

        this.folderIconField = new TextFieldWidget(this.font, this.guiLeft, this.guiTop + 96 / 2 + 15, 80, 9, new TranslationTextComponent("itemGroup.search"));
        this.folderIconField.setMaxStringLength(Integer.MAX_VALUE);
        this.folderIconField.setEnableBackgroundDrawing(true);
        this.folderIconField.setVisible(true);
        this.folderIconField.setTextColor(16777215);
        this.folderIconField.setHeight(18);
        this.folderIconField.setWidth(120);
        this.folderIconField.setCanLoseFocus(true);
        this.folderIconField.x -= this.folderIconField.getAdjustedWidth() - 37;
        this.folderIconField.y -= this.folderIconField.getHeight() / 2 + 40;

        this.saveButton = addButton(new Button(this.guiLeft - 176 / 2, guiTop + 96 / 2, 60, 20, new StringTextComponent(isEditing ? "Save" : "Create"), p_onPress_1_ -> {
            if (!isEditing)
            {
                if (currentTab == WardrobeTab.DATABASE_WARDROBE)
                {
                    ModNetwork.CHANNEL.sendToServer(new AddFolderToDatabasePacket(new Payload().putString("FolderName", folderNameField.getText()).putString("FolderIcon", folderIconField.getText()).getPayload()));
                }
            } else {
//                if (currentTab == WardrobeTab.DATABASE_WARDROBE)
//                {
//                    ModNetwork.CHANNEL.sendToServer(new ModifyFolderFromDatabasePacket(new Payload().putString("FolderName", folderNameField.getText()).putString("FolderIcon", folderIconField.getText()).getPayload()));
//                }
            }

            popUpScreen.finish();
        }));

        this.deleteButton = addButton(new Button(this.guiLeft + 176 / 2, guiTop + 96 / 2, 60, 20, new StringTextComponent((isEditing ? "\u00A7cDelete" : "Back")), p_onPress_1_ -> {
            if (isEditing)
            {
                if (currentTab == WardrobeTab.DATABASE_WARDROBE)
                {
                    ModNetwork.CHANNEL.sendToServer(new DeleteFolderFromDatabasePacket(new Payload().putString("FolderName", folderNameField.getText()).getPayload()));
                }
            }
            popUpScreen.finish();
        }));

        this.deleteButton.x -= this.deleteButton.getWidth();

        this.children.add(folderNameField);
        this.children.add(folderIconField);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        minecraft.textureManager.bindTexture(WARDROBE_SETTINGS);
        blit(matrixStack, width / 2 - 176 /2, height / 2 - 96 / 2, 176, 96, 0, 0, 176, 96, 256, 256);

        folderNameField.render(matrixStack, mouseX, mouseY, partialTicks);
        folderIconField.render(matrixStack, mouseX, mouseY, partialTicks);

        font.drawStringWithShadow(matrixStack, "Folder Name", folderNameField.x, folderNameField.y - 12, 0xFFFFFF);
        font.drawStringWithShadow(matrixStack, "Folder Icon", folderIconField.x, folderIconField.y - 12, 0xFFFFFF);

        if (ResourceLocation.isResouceNameValid(folderIconField.getText()))
        {
            Item foundItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(folderIconField.getText()));
            if (foundItem != null)
            {
                itemRenderer.renderItemIntoGUI(new ItemStack(foundItem), this.guiLeft + 54, guiTop + 15);
            }
        }
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
