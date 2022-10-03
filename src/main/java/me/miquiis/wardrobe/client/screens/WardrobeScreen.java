package me.miquiis.wardrobe.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.miquiis.skinchangerapi.client.SkinChangerAPIClient;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.PersonalWardrobe;
import me.miquiis.wardrobe.client.renderers.CustomPlayerRenderer;
import me.miquiis.wardrobe.common.WardrobePage;
import me.miquiis.wardrobe.common.WardrobeTab;
import me.miquiis.wardrobe.database.LocalCache;
import me.miquiis.wardrobe.server.network.ModNetwork;
import me.miquiis.wardrobe.server.network.messages.LoadSkinPacket;
import me.miquiis.wardrobe.server.network.messages.RequestPagePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.*;

import java.util.Arrays;
import java.util.Optional;

public class WardrobeScreen extends Screen {

    public interface IDontRender {}

    public static class DontRenderButton extends Button implements IDontRender {
        public DontRenderButton(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction) {
            super(x, y, width, height, title, pressedAction);
        }
    }

    public static class WardrobeSkinButton extends Button implements IDontRender {

        private int buttonId;

        public WardrobeSkinButton(int x, int y, int width, int height, ITextComponent title, int buttonId, IPressable pressedAction) {
            super(x, y, width, height, title, pressedAction);
            this.buttonId = buttonId;
        }

        public int getButtonId() {
            return buttonId;
        }
    }

    private static final ResourceLocation WARDROBE_ICONS = new ResourceLocation(Wardrobe.MOD_ID, "textures/gui/wardrobe_icons.png");
    private static final ResourceLocation LOADING_SKIN = new ResourceLocation(Wardrobe.MOD_ID, "textures/skins/loading_skin.png");
    private static ClientPlayerEntity fakePlayer;

    private int guiLeft;
    private int guiTop;

    private WardrobeTab currentTab = WardrobeTab.PERSONAL_WARDROBE;
    private SkinLocation selectedSkin;

    private int currentPage;
    private WardrobePage pageContent;
    private WardrobePage.PageSort pageSort;
    private boolean isAscending;

    private String lastSearchField = "";
    private int lastTypedTick;
    private boolean hasSearchRefreshed = true;

    private TextFieldWidget searchField;
    private Button refreshButton;
    private Button filterButton;
    private Button serverWardrobeButton;
    private Button personalWardrobeButton;
    private Button wearSkinButton;
    private Button clearSkinButton;
    private Button sendToServerButton;
    private Button modifySkinButton;

    private boolean canRefresh = true;
    private boolean isLoading = true;
    private int loadingTick;

    private final int wardrobeWidth = 150;
    private final int wardrobeHeight = 230;

    public WardrobeScreen(ITextComponent titleIn) {
        super(titleIn);
    }

    @Override
    protected <T extends Widget> T addButton(T button) {
        if (button instanceof WardrobeSkinButton)
        {
            WardrobeSkinButton wardrobeSkinButton = (WardrobeSkinButton) button;
            if (buttons.stream().noneMatch(widget -> widget instanceof WardrobeSkinButton && ((WardrobeSkinButton)widget).getButtonId() == wardrobeSkinButton.getButtonId()))
            {
                return super.addButton(button);
            } else {
                return null;
            }
        }
        return super.addButton(button);
    }

    @Override
    protected void init() {
        super.init();
        if (Minecraft.getInstance().player != null)
        {
            fakePlayer = new ClientPlayerEntity(Minecraft.getInstance(), Minecraft.getInstance().world, Minecraft.getInstance().getConnection(), null, null, false, false);
        }

        this.guiLeft = width / 2;
        this.guiTop = height / 2;
        currentPage = 1;
        pageSort = WardrobePage.PageSort.ALPHABETIC;
        isAscending = true;

        this.refreshButton = addButton(new Button(this.guiLeft - 48, this.guiTop - 115 - 21, 20, 20, new StringTextComponent(""), p_onPress_1_ -> {
            refreshPage(true);
        }));

        this.filterButton = addButton(new Button(this.guiLeft - 72, this.guiTop - 115 - 21, 20, 20, new StringTextComponent(""), p_onPress_1_ -> {
            if (hasShiftDown())
            {
                isAscending = !isAscending;
            } else {
                pageSort = WardrobePage.PageSort.values()[pageSort.ordinal() + 1 < WardrobePage.PageSort.values().length ? pageSort.ordinal() + 1 : 0];
            }
            refreshPage(false);
        }));

        this.serverWardrobeButton = addButton(new DontRenderButton(this.guiLeft - wardrobeWidth - 52, this.guiTop - wardrobeHeight / 2 + 10, 28, 28, new StringTextComponent("serverWardrobe"), p_onPress_1_ -> {
            currentTab = WardrobeTab.SERVER_WARDROBE;
            selectedSkin = null;
            refreshPage(false);
        }));

        this.personalWardrobeButton = addButton(new DontRenderButton(this.guiLeft - wardrobeWidth - 52, this.guiTop - wardrobeHeight / 2 + 42, 28, 28, new StringTextComponent("serverWardrobe"), p_onPress_1_ -> {
            currentTab = WardrobeTab.PERSONAL_WARDROBE;
            selectedSkin = null;
            refreshPage(false);
        }));

        this.wearSkinButton = addButton(new Button(guiLeft + 50 - 40, guiTop + 50 + 10, 80, 20, new StringTextComponent("Wear Skin"), p_onPress_1_ -> {

        }));

        this.clearSkinButton = addButton(new Button(guiLeft + 50 - 10 + 50, guiTop - 75, 20, 20, new StringTextComponent(""), p_onPress_1_ -> {

        }));

        this.modifySkinButton = addButton(new Button(guiLeft + 50 - 10 - 50, guiTop - 75, 20, 20, new StringTextComponent(""), p_onPress_1_ -> {
            PopUpScreen popUpScreen = new PopUpScreen(this, new SkinSettingsScreen(selectedSkin));
            minecraft.displayGuiScreen(popUpScreen);
        }));

        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.searchField = new TextFieldWidget(this.font, this.guiLeft + 82, this.guiTop + 6, 80, 9, new TranslationTextComponent("itemGroup.search"));
        this.searchField.setMaxStringLength(50);
        this.searchField.setEnableBackgroundDrawing(true);
        this.searchField.setVisible(false);
        this.searchField.setTextColor(16777215);
        this.searchField.setHeight(18);
        this.searchField.setWidth(90);

        this.children.add(searchField);

        this.wearSkinButton.visible = false;
        this.clearSkinButton.visible = false;
        this.modifySkinButton.visible = false;

        refreshPage(false);
    }

    public void refreshPage(boolean forceRefresh)
    {
        if (!canRefresh) return;
        Optional<LocalCache<WardrobePage>.Cached> cachedPage = Wardrobe.getInstance().getClientWardrobePageCache().getCache(cached -> cached.getValue().getSearchBar().equals(searchField.getText()) && cached.getValue().isAscending() == isAscending && cached.getValue().getPageSorted() == pageSort && cached.getValue().getPage() == currentPage && cached.getValue().getWardrobeTab() == currentTab);
        if (!forceRefresh && cachedPage.isPresent())
        {
            // Load
            pageContent = cachedPage.get().getValue();
            pageContent.getContents().forEach(SkinChangerAPIClient::loadSkin);
            isLoading = false;
        } else {
            // Request
            if (currentTab == WardrobeTab.PERSONAL_WARDROBE)
            {
                PersonalWardrobe.refreshWardrobe();
                Wardrobe.getInstance().getClientWardrobePageCache().cache(new WardrobePage(searchField.getText(),
                        pageSort, isAscending, PersonalWardrobe.getPersonalWardrobe(), WardrobeTab.PERSONAL_WARDROBE, currentPage
                ), cached -> cached.getValue().getSearchBar().equals(searchField.getText()) && cached.getValue().isAscending() == isAscending && cached.getValue().getPageSorted() == pageSort && cached.getValue().getPage() == currentPage && currentTab == cached.getValue().getWardrobeTab());
                isLoading = true;
                refreshPage(false);
            } else if (currentTab == WardrobeTab.SERVER_WARDROBE)
            {
                ModNetwork.CHANNEL.sendToServer(new RequestPagePacket(searchField.getText(), pageSort, isAscending, currentPage, 1));
                isLoading = true;
            }
        }
        resetSkinButtons();
    }

    private void resetSkinButtons() {
        this.buttons.removeIf(widget -> widget instanceof WardrobeSkinButton);
        this.getEventListeners().removeIf(widget -> widget instanceof WardrobeSkinButton);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        canRefresh = false;
        String lastSearchField = searchField.getText();
        WardrobePage.PageSort lastPageSort = pageSort;
        boolean lastIsAscending = isAscending;
        int lastPage = currentPage;
        super.resize(minecraft, width, height);
        searchField.setText(lastSearchField);
        pageSort = lastPageSort;
        isAscending = lastIsAscending;
        currentPage = lastPage;
        canRefresh = true;
        refreshPage(false);
    }

    @Override
    public void tick() {
        super.tick();
        this.searchField.tick();

        wearSkinButton.visible = selectedSkin != null;
        clearSkinButton.visible = selectedSkin != null;
        modifySkinButton.visible = selectedSkin != null;

        if (!lastSearchField.equals(searchField.getText()))
        {
            lastTypedTick = 0;
            lastSearchField = searchField.getText();
            hasSearchRefreshed = false;
        } else {
            lastTypedTick = Math.min(lastTypedTick + 1, 5);
        }

        if (lastTypedTick >= 5)
        {
            if (!hasSearchRefreshed)
            {
                refreshPage(false);
                hasSearchRefreshed = true;
            }
        }

        if (isLoading)
        {
            loadingTick++;
        } else {
            loadingTick = 0;
        }
    }

    private IFormattableTextComponent getSortTextComponent()
    {
        return new TranslationTextComponent("wardrobe.screen.hover.sort_button.sort").appendSibling(new TranslationTextComponent("wardrobe.screen.hover.sort_button." + pageSort.name().toLowerCase()));
    }

    private IFormattableTextComponent getOrderTextComponent()
    {
        return new TranslationTextComponent("wardrobe.screen.hover.sort_button.order").appendSibling(new TranslationTextComponent("wardrobe.screen.hover.sort_button." + (isAscending ? "ascend" : "descend")));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        for(int i = 0; i < this.buttons.size(); ++i) {
            if (buttons.get(i) instanceof IDontRender) continue;
            this.buttons.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
        }

        minecraft.textureManager.bindTexture(WARDROBE_ICONS);

        if (currentTab == WardrobeTab.SERVER_WARDROBE)
        {
            blit(matrixStack, this.guiLeft - wardrobeWidth - 52, this.guiTop - wardrobeHeight / 2 + 42, 28, 28, 87, 168, 28, 28, 256, 256);
        } else if (currentTab == WardrobeTab.PERSONAL_WARDROBE)
        {
            blit(matrixStack, this.guiLeft - wardrobeWidth - 52, this.guiTop - wardrobeHeight / 2 + 10, 28, 28, 87, 168, 28, 28, 256, 256);
        }

        if (selectedSkin != null)
        {
            blit(matrixStack, guiLeft + 50 - 10 + 50 + 5, guiTop - 75 + 5, 10, 10, 26, 170, 12, 12, 256, 256);
            blit(matrixStack, guiLeft + 50 - 10 - 50 + 4, guiTop - 75 + 4, 12, 12, 39, 170, 12, 12, 256, 256);
        }

        itemRenderer.renderItemIntoGUI(new ItemStack(Items.ENDER_CHEST), this.guiLeft - wardrobeWidth - 45, this.guiTop - wardrobeHeight / 2 + 16);
        itemRenderer.renderItemIntoGUI(new ItemStack(Items.CHEST), this.guiLeft - wardrobeWidth - 45, this.guiTop - wardrobeHeight / 2 + 48);

        minecraft.textureManager.bindTexture(WARDROBE_ICONS);

        blit(matrixStack, width / 2 - wardrobeWidth / 2 - 100, height / 2 - wardrobeHeight / 2, wardrobeWidth, wardrobeHeight, 1, 1, 147, 166, 256, 256);
        blit(matrixStack, this.guiLeft + 4 - 48, this.guiTop + 4 - 115 - 21, 12, 12, 1, 170, 12, 12, 256, 256);
        blit(matrixStack, this.guiLeft + 4 - 72, this.guiTop + 4 - 115 - 21, 12, 12, 13, 170, 12, 12, 256, 256);

        if (currentTab == WardrobeTab.SERVER_WARDROBE)
        {
            blit(matrixStack, this.guiLeft - wardrobeWidth - 53, this.guiTop - wardrobeHeight / 2 + 10, 32, 28, 116, 168, 32, 28, 256, 256);
        } else if (currentTab == WardrobeTab.PERSONAL_WARDROBE)
        {
            blit(matrixStack, this.guiLeft - wardrobeWidth - 53, this.guiTop - wardrobeHeight / 2 + 42, 32, 28, 116, 168, 32, 28, 256, 256);
        }

//        blit(matrixStack, this.guiLeft - wardrobeWidth - 53, this.guiTop, 32, 28, 116, 168, 32, 28, 256, 256);

        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        searchField.x = this.guiLeft - wardrobeWidth - 100 + searchField.getWidth() - 12;
        searchField.y = this.guiTop - wardrobeHeight / 2 - searchField.getHeight() - 2;
        this.searchField.setVisible(true);
        this.searchField.setCanLoseFocus(false);
        this.searchField.setFocused2(true);

        if (filterButton.isHovered() && filterButton.active)
        {
            renderWrappedToolTip(matrixStack, Arrays.asList(getSortTextComponent(), getOrderTextComponent()), mouseX, mouseY, font);
        }

        if (refreshButton.isHovered() && refreshButton.active)
        {
            renderTooltip(matrixStack, new TranslationTextComponent("wardrobe.screen.hover.refresh_button"), mouseX, mouseY);
        }

        if (isHovered(serverWardrobeButton, mouseX, mouseY))
        {
            renderTooltip(matrixStack, new TranslationTextComponent("wardrobe.screen.hover.server_wardrobe_button"), mouseX, mouseY);
        }

        if (isHovered(personalWardrobeButton, mouseX, mouseY))
        {
            renderTooltip(matrixStack, new TranslationTextComponent("wardrobe.screen.hover.personal_wardrobe_button"), mouseX, mouseY);
        }

        buttons.stream().filter(widget -> widget instanceof WardrobeSkinButton).forEach(widget -> {
            if (mouseX >= widget.x && mouseY >= widget.y && mouseX < widget.x + widget.getWidth() && mouseY < widget.y + widget.getHeight())
            {
                if (!widget.active) return;
                renderTooltip(matrixStack, widget.getMessage(), mouseX, mouseY);
            }
        });

        if (isLoading) {
            drawFakePlayerOnScreen(width / 2 - (wardrobeWidth - 50), (height / 2 + 50), 50, mouseX, mouseY, LOADING_SKIN, true, 1f - ((loadingTick % 50) / 50f));
        } else {
            for (int i = 0; i < 4; i++)
            {
                for (int j = 0; j < 4; j++)
                {
                    int currentId = ((4 * (j + 1))-4) + i;
                    if (pageContent != null && !isLoading)
                    {
                        if (pageContent.getContents().size() > currentId)
                        {
                            SkinLocation skinLocation = pageContent.getContents().get(currentId);
                            addButton(new WardrobeSkinButton(width / 2 - wardrobeWidth + 33 * i - 12, height / 2 - 52 + 50 * j - 50, 25, 50, new StringTextComponent(skinLocation.getSkinId()), currentId, p_onPress_1_ -> {
                                SkinLocation skin = pageContent.getContents().get(((WardrobeSkinButton)p_onPress_1_).buttonId);
//                                ModNetwork.CHANNEL.sendToServer(new LoadSkinPacket(skin));
                                selectedSkin = skin;
                            }));
                            drawFakePlayerOnScreen(width / 2 - wardrobeWidth + 33 * i, height / 2 - 52 + 50 * j, 25, mouseX, mouseY, skinLocation.getSkinLocation(), skinLocation.isSlim());
                        }
                    }
                }
            }
        }

        int playerX = width / 2 + 50;
        int playerY = height / 2 + 50;
        if (selectedSkin != null)
        {
            drawPlayerOnScreen(playerX, playerY, 50, -mouseX + playerX, -mouseY + playerY - 80, minecraft.player, selectedSkin);
        } else {
            InventoryScreen.drawEntityOnScreen(playerX, playerY, 50, -mouseX + playerX, -mouseY + playerY - 80, minecraft.player);
        }
    }

    private boolean isHovered(Widget widget, int mouseX, int mouseY)
    {
        return widget.active && mouseX >= widget.x && mouseY >= widget.y && mouseX < widget.x + widget.getWidth() && mouseY < widget.y + widget.getHeight();
    }

    public static void drawPlayerOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, AbstractClientPlayerEntity livingEntity, SkinLocation skinLocation) {
        float f = (float)Math.atan((double)(mouseX / 40.0F));
        float f1 = (float)Math.atan((double)(mouseY / 40.0F));
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)posX, (float)posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((float)scale, (float)scale, (float)scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.multiply(quaternion1);
        matrixstack.rotate(quaternion);
        float f2 = livingEntity.renderYawOffset;
        float f3 = livingEntity.rotationYaw;
        float f4 = livingEntity.rotationPitch;
        float f5 = livingEntity.prevRotationYawHead;
        float f6 = livingEntity.rotationYawHead;
        livingEntity.renderYawOffset = 180.0F + f * 20.0F;
        livingEntity.rotationYaw = 180.0F + f * 40.0F;
        livingEntity.rotationPitch = -f1 * 20.0F;
        livingEntity.rotationYawHead = livingEntity.rotationYaw;
        livingEntity.prevRotationYawHead = livingEntity.rotationYaw;
        EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        RenderSystem.runAsFancy(() -> {
            renderPlayerStatic(livingEntity, skinLocation.getSkinLocation(), skinLocation.isSlim(), 1f, entityrenderermanager, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        });
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        livingEntity.renderYawOffset = f2;
        livingEntity.rotationYaw = f3;
        livingEntity.rotationPitch = f4;
        livingEntity.prevRotationYawHead = f5;
        livingEntity.rotationYawHead = f6;
        RenderSystem.popMatrix();
    }

    public static void drawFakePlayerOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, ResourceLocation skinTexture, boolean isSlim)
    {
        drawFakePlayerOnScreen(posX, posY, scale, mouseX, mouseY, skinTexture, isSlim, 1f);
    }

    public static void drawFakePlayerOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, ResourceLocation skinTexture, boolean isSlim, float progress) {
        float f = (float)Math.atan((double)(mouseX / 40.0F));
        float f1 = (float)Math.atan((double)(mouseY / 40.0F));
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)posX, (float)posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((float)scale, (float)scale, (float)scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(0.0F);
        quaternion.multiply(quaternion1);
        matrixstack.rotate(quaternion);
        fakePlayer.renderYawOffset = 160.0F;
        fakePlayer.rotationYaw = 160.0F;
        fakePlayer.rotationPitch = 0;
        fakePlayer.ticksExisted = Minecraft.getInstance().player.ticksExisted;
        fakePlayer.rotationYawHead = fakePlayer.rotationYaw;
        fakePlayer.prevRotationYawHead = fakePlayer.rotationYaw;
        EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        RenderSystem.runAsFancy(() -> {
            renderPlayerStatic(fakePlayer, skinTexture, isSlim, progress, entityrenderermanager, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        });
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        RenderSystem.popMatrix();
    }

    private static void renderPlayerStatic(AbstractClientPlayerEntity entityIn, ResourceLocation skin, boolean isSlim, float progress, EntityRendererManager rendererManager, double xIn, double yIn, double zIn, float rotationYawIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        CustomPlayerRenderer entityrenderer = new CustomPlayerRenderer(rendererManager, skin, isSlim, progress);
        try {
            Vector3d vector3d = entityrenderer.getRenderOffset(entityIn, partialTicks);
            double d2 = xIn + vector3d.getX();
            double d3 = yIn + vector3d.getY();
            double d0 = zIn + vector3d.getZ();
            matrixStackIn.push();
            matrixStackIn.translate(d2, d3, d0);
            entityrenderer.render(entityIn, rotationYawIn, partialTicks, matrixStackIn, bufferIn, packedLightIn);
            matrixStackIn.translate(-vector3d.getX(), -vector3d.getY(), -vector3d.getZ());
            matrixStackIn.pop();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being rendered");
            entityIn.fillCrashReport(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.makeCategory("Renderer details");
            crashreportcategory1.addDetail("Assigned renderer", entityrenderer);
            crashreportcategory1.addDetail("Location", CrashReportCategory.getCoordinateInfo(xIn, yIn, zIn));
            crashreportcategory1.addDetail("Rotation", rotationYawIn);
            crashreportcategory1.addDetail("Delta", partialTicks);
            throw new ReportedException(crashreport);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
