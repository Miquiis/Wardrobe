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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

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
    private static ClientPlayerEntity fakePlayer;

    private int currentPage;
    private WardrobePage pageContent;
    private WardrobePage.PageSort pageSort;
    private boolean isAscending;

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
        currentPage = 1;
        pageSort = WardrobePage.PageSort.ALPHABETIC;
        isAscending = true;

        // Request and Load Page
        Optional<LocalCache<WardrobePage>.Cached> cachedPage = Wardrobe.getInstance().getClientWardrobePageCache().getCache(cached -> cached.getValue().isAscending() && isAscending && cached.getValue().getPageSorted() == pageSort && cached.getValue().getPage() == currentPage);
        if (cachedPage.isPresent())
        {
            // Load
            System.out.println("Loading from Cache");
            pageContent = cachedPage.get().getValue();
            pageContent.getContents().forEach(SkinChangerAPIClient::loadSkin);
        } else {
            // Request
            System.out.println("Requesting from Server");
            ModNetwork.CHANNEL.sendToServer(new RequestPagePacket(pageSort, isAscending, currentPage, 1));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        minecraft.textureManager.bindTexture(WARDROBE_ICONS);
        int wardrobeWidth = 150;
        int wardrobeHeight = 230;
        blit(matrixStack, width / 2 - wardrobeWidth / 2 - 100, height / 2 - wardrobeHeight / 2, wardrobeWidth, wardrobeHeight, 1, 1, 147, 166, 256, 256);

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
                        addButton(new WardrobeSkinButton(width / 2 - wardrobeWidth + 33 * i - 12, height / 2 - 52 + 50 * j - 50, 25, 50, new StringTextComponent("skin"), currentId, p_onPress_1_ -> {
                            SkinLocation skin = pageContent.getContents().get(((WardrobeSkinButton)p_onPress_1_).buttonId);
                            System.out.println(skin.getSkinLocation());
                            ModNetwork.CHANNEL.sendToServer(new LoadSkinPacket(skin));
                        }));
                        drawPlayerOnScreen(width / 2 - wardrobeWidth + 33 * i, height / 2 - 52 + 50 * j, 25, mouseX, mouseY, skinLocation.getSkinLocation());
                    }
                }
            }
        }

        int playerX = width / 2 + 50;
        int playerY = height / 2 + 50;
        InventoryScreen.drawEntityOnScreen(playerX, playerY, 50, -mouseX + playerX, -mouseY + playerY - 80, minecraft.player);

        for(int i = 0; i < this.buttons.size(); ++i) {
            if (buttons.get(i) instanceof IDontRender) continue;
            this.buttons.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    public static void drawPlayerOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, ResourceLocation skinTexture) {
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
            renderPlayerStatic(fakePlayer, skinTexture, entityrenderermanager, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        });
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        RenderSystem.popMatrix();
    }

    private static void renderPlayerStatic(AbstractClientPlayerEntity entityIn, ResourceLocation skin, EntityRendererManager rendererManager, double xIn, double yIn, double zIn, float rotationYawIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        CustomPlayerRenderer entityrenderer = new CustomPlayerRenderer(rendererManager, skin);
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
