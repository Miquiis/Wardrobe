package me.miquiis.wardrobe.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class PopUpScreen extends Screen {

    public interface IPopUpScreen {
        PopUpScreen getPopUpScreen();
        void setPopUpScreen(PopUpScreen popUpScreen);
    }

    private Screen backgroundScreen;
    private Screen frontScreen;

    public PopUpScreen(Screen backgroundScreen, Screen frontScreen) {
        super(new StringTextComponent("PopUp"));
        this.backgroundScreen = backgroundScreen;
        this.frontScreen = frontScreen;
        if (frontScreen instanceof IPopUpScreen)
        {
            IPopUpScreen iPopUpScreen = (IPopUpScreen) frontScreen;
            iPopUpScreen.setPopUpScreen(this);
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        backgroundScreen.resize(minecraft, width, height);
        frontScreen.resize(minecraft, width, height);
        super.resize(minecraft, width, height);
    }

    @Override
    protected void init() {
        super.init();
        backgroundScreen.getEventListeners().forEach(iGuiEventListener -> {
            if (iGuiEventListener instanceof Button)
            {
                Button button = (Button) iGuiEventListener;
                button.active = false;
            }
        });
        frontScreen.init(minecraft, width, height);
        this.children.addAll(frontScreen.getEventListeners());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        frontScreen.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
//        frontScreen.keyReleased(keyCode, scanCode, modifiers);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
//        frontScreen.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
//        frontScreen.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
//        frontScreen.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
//        frontScreen.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
//        frontScreen.isMouseOver(mouseX, mouseY);
        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
//        frontScreen.mouseScrolled(mouseX, mouseY, delta);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        super.tick();
        backgroundScreen.tick();
        frontScreen.tick();
    }

    public void finish() {
        if (backgroundScreen instanceof WardrobeScreen)
        {
            WardrobeScreen wardrobeScreen = (WardrobeScreen) backgroundScreen;
            wardrobeScreen.refreshPage(true);
        }
        minecraft.displayGuiScreen(backgroundScreen);
    }

    @Override
    public void onClose() {
        System.out.println("Here");
        //super.onClose();
        //frontScreen.onClose();
    }

    @Override
    public void closeScreen() {
        System.out.println("Here 2");
        minecraft.displayGuiScreen(backgroundScreen);
//        System.out.println("Close");
//        frontScreen.onClose();
//        minecraft.displayGuiScreen(backgroundScreen);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        backgroundScreen.render(matrixStack, mouseX, mouseY, partialTicks);
        matrixStack.push();
        matrixStack.translate(0, 0, 100);
        frontScreen.render(matrixStack, mouseX, mouseY, partialTicks);
        matrixStack.pop();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
