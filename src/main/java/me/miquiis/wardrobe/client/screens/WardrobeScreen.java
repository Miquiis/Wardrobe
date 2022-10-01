package me.miquiis.wardrobe.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.miquiis.skinchangerapi.client.SkinChangerAPIClient;
import me.miquiis.skinchangerapi.common.SkinLocation;
import me.miquiis.wardrobe.Wardrobe;
import me.miquiis.wardrobe.client.renderers.CustomPlayerRenderer;
import me.miquiis.wardrobe.common.WardrobePage;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.*;

import java.util.Arrays;
import java.util.Optional;

public class WardrobeScreen extends Screen {
    public interface IDontRender {}

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

    private boolean canRefresh = true;
    private boolean isLoading = true;
    private int loadingTick;

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

        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.searchField = new TextFieldWidget(this.font, this.guiLeft + 82, this.guiTop + 6, 80, 9, new TranslationTextComponent("itemGroup.search"));
        this.searchField.setMaxStringLength(50);
        this.searchField.setEnableBackgroundDrawing(true);
        this.searchField.setVisible(false);
        this.searchField.setTextColor(16777215);
        this.searchField.setHeight(18);
        this.searchField.setWidth(90);

        this.children.add(searchField);

        refreshPage(false);
    }

    public void refreshPage(boolean forceRefresh)
    {
        if (!canRefresh) return;
        Optional<LocalCache<WardrobePage>.Cached> cachedPage = Wardrobe.getInstance().getClientWardrobePageCache().getCache(cached -> cached.getValue().getSearchBar().equals(searchField.getText()) && cached.getValue().isAscending() == isAscending && cached.getValue().getPageSorted() == pageSort && cached.getValue().getPage() == currentPage);
        System.out.println(cachedPage.isPresent());
        if (!forceRefresh && cachedPage.isPresent())
        {
            // Load
            System.out.println("Loading from Cache");
            pageContent = cachedPage.get().getValue();
            pageContent.getContents().forEach(SkinChangerAPIClient::loadSkin);
            isLoading = false;
        } else {
            // Request
            System.out.println("Requesting from Server");
            ModNetwork.CHANNEL.sendToServer(new RequestPagePacket(searchField.getText(), pageSort, isAscending, currentPage, 1));
            isLoading = true;
        }
        resetSkinButtons();
    }

    private void resetSkinButtons() {
        this.buttons.removeIf(widget -> widget instanceof WardrobeSkinButton);
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
        int wardrobeWidth = 150;
        int wardrobeHeight = 230;
        blit(matrixStack, width / 2 - wardrobeWidth / 2 - 100, height / 2 - wardrobeHeight / 2, wardrobeWidth, wardrobeHeight, 1, 1, 147, 166, 256, 256);
        blit(matrixStack, this.guiLeft + 4 - 48, this.guiTop + 4 - 115 - 21, 12, 12, 1, 170, 12, 12, 256, 256);
        blit(matrixStack, this.guiLeft + 4 - 72, this.guiTop + 4 - 115 - 21, 12, 12, 13, 170, 12, 12, 256, 256);

        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        searchField.x = this.guiLeft - wardrobeWidth - 100 + searchField.getWidth() - 12;
        searchField.y = this.guiTop - wardrobeHeight / 2 - searchField.getHeight() - 2;
        this.searchField.setVisible(true);
        this.searchField.setCanLoseFocus(false);
        this.searchField.setFocused2(true);

        if (filterButton.isHovered())
        {
            renderWrappedToolTip(matrixStack, Arrays.asList(getSortTextComponent(), getOrderTextComponent()), mouseX, mouseY, font);
        }

        if (refreshButton.isHovered())
        {
            renderTooltip(matrixStack, new TranslationTextComponent("wardrobe.screen.hover.refresh_button"), mouseX, mouseY);
        }

        buttons.stream().filter(widget -> widget instanceof WardrobeSkinButton).forEach(widget -> {
            if (mouseX >= widget.x && mouseY >= widget.y && mouseX < widget.x + widget.getWidth() && mouseY < widget.y + widget.getHeight())
            {
                renderTooltip(matrixStack, widget.getMessage(), mouseX, mouseY);
            }
        });

        if (isLoading) {
            drawPlayerOnScreen(width / 2 - (wardrobeWidth - 50), (height / 2 + 50), 50, mouseX, mouseY, LOADING_SKIN, true, 1f - ((loadingTick % 50) / 50f));
        } else {
            for (int i = 0; i < 4; i++)
            {
                for (int j = 0; j < 4; j++)
                {
                    int currentId = ((4 * (j + 1))-4) + i;
                    if (pageContent != null)
                    {
                        if (pageContent.getContents().size() > currentId)
                        {
                            SkinLocation skinLocation = pageContent.getContents().get(currentId);
                            addButton(new WardrobeSkinButton(width / 2 - wardrobeWidth + 33 * i - 12, height / 2 - 52 + 50 * j - 50, 25, 50, new StringTextComponent(skinLocation.getSkinId()), currentId, p_onPress_1_ -> {
                                SkinLocation skin = pageContent.getContents().get(((WardrobeSkinButton)p_onPress_1_).buttonId);
                                System.out.println(skin.getSkinLocation());
                                ModNetwork.CHANNEL.sendToServer(new LoadSkinPacket(skin));
                            }));
                            drawPlayerOnScreen(width / 2 - wardrobeWidth + 33 * i, height / 2 - 52 + 50 * j, 25, mouseX, mouseY, skinLocation.getSkinLocation(), skinLocation.isSlim());
                        }
                    }
                }
            }
        }

        int playerX = width / 2 + 50;
        int playerY = height / 2 + 50;
        InventoryScreen.drawEntityOnScreen(playerX, playerY, 50, -mouseX + playerX, -mouseY + playerY - 80, minecraft.player);
    }

    public static void drawPlayerOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, ResourceLocation skinTexture, boolean isSlim)
    {
        drawPlayerOnScreen(posX, posY, scale, mouseX, mouseY, skinTexture, isSlim, 1f);
    }

    public static void drawPlayerOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, ResourceLocation skinTexture, boolean isSlim, float progress) {
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
//            if (rendererManager.options.entityShadows && !entityIn.isInvisible()) {
//                double d1 = rendererManager.getDistanceToCamera(entityIn.getPosX(), entityIn.getPosY(), entityIn.getPosZ());
//                float f = (float)((1.0D - d1 / 256.0D) * 1f);
//                if (f > 0.0F) {
//                    rendererManager.renderShadow(matrixStackIn, bufferIn, entityIn, f, partialTicks, entityIn.world, 0.5f);
//                }
//            }

//            if (this.debugBoundingBox && !entityIn.isInvisible() && !Minecraft.getInstance().isReducedDebug()) {
//                this.renderDebugBoundingBox(matrixStackIn, bufferIn.getBuffer(RenderType.getLines()), entityIn, partialTicks);
//            }

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
