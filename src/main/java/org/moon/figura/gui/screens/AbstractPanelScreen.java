package org.moon.figura.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.config.Configs;
import org.moon.figura.gui.widgets.ContextMenu;
import org.moon.figura.gui.widgets.FiguraRemovable;
import org.moon.figura.gui.widgets.FiguraTickable;
import org.moon.figura.gui.widgets.PanelSelectorWidget;
import org.moon.figura.mixin.gui.ScreenAccessor;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

import java.util.List;

public abstract class AbstractPanelScreen extends Screen {

    public static final ResourceLocation BACKGROUND = new FiguraIdentifier("textures/gui/background.png");

    //variables
    protected final Screen parentScreen;
    public PanelSelectorWidget panels;

    //overlays
    public ContextMenu contextMenu;
    public Component tooltip;

    //stuff :3
    private static final String EGG = "ĉĉĈĈćĆćĆBAā";
    private String egg = EGG;

    protected AbstractPanelScreen(Screen parentScreen, Component title) {
        super(title);
        this.parentScreen = parentScreen;
    }

    public Class<? extends Screen> getSelectedPanel() {
        return this.getClass();
    };

    @Override
    protected void init() {
        super.init();

        //add panel selector
        this.addRenderableWidget(panels = new PanelSelectorWidget(parentScreen, 0, 0, width, getSelectedPanel()));

        //clear overlays
        contextMenu = null;
        tooltip = null;
    }

    @Override
    public void tick() {
        for (Renderable renderable : this.renderables()) {
            if (renderable instanceof FiguraTickable tickable)
                tickable.tick();
        }

        renderables().removeIf(r -> r instanceof FiguraRemovable removable && removable.isRemoved());

        super.tick();
    }

    public List<Renderable> renderables() {
        return ((ScreenAccessor) this).getRenderables();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //setup figura framebuffer
        //UIHelper.useFiguraGuiFramebuffer();

        //render background
        this.renderBackground(stack, delta);

        //render contents
        super.render(stack, mouseX, mouseY, delta);

        //render overlays
        this.renderOverlays(stack, mouseX, mouseY, delta);

        //restore vanilla framebuffer
        //UIHelper.useVanillaFramebuffer();
    }

    public void renderBackground(PoseStack stack, float delta) {
        //render
        double speed = Configs.BACKGROUND_SCROLL_SPEED.tempValue * 0.5;
        UIHelper.renderAnimatedBackground(stack, BACKGROUND, 0, 0, this.width, this.height, 64, 64, speed, delta);
    }

    public void renderOverlays(PoseStack stack, int mouseX, int mouseY, float delta) {
        //render context
        if (contextMenu != null && contextMenu.isVisible()) {
            //translate the stack here because of nested contexts
            stack.pushPose();
            stack.translate(0f, 0f, 500f);
            contextMenu.render(stack, mouseX, mouseY, delta);
            stack.popPose();
        }
        //render tooltip
        else if (tooltip != null)
            UIHelper.renderTooltip(stack, tooltip, mouseX, mouseY, true);

        tooltip = null;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //context menu first
        if (this.contextMenuClick(mouseX, mouseY, button))
            return true;

        GuiEventListener widget = null;

        //update children focused
        for (GuiEventListener children : List.copyOf(this.children())) {
            boolean clicked = children.mouseClicked(mouseX, mouseY, button);
            children.setFocused(clicked);
            if (clicked) widget = children;
        }

        //set this focused
        if (getFocused() != widget)
            setFocused(widget);

        if (widget != null) {
            if (button == 0) this.setDragging(true);
            return true;
        }

        return false;
    }

    public boolean contextMenuClick(double mouseX, double mouseY, int button) {
        //attempt to run context first
        if (contextMenu != null && contextMenu.isVisible()) {
            //attempt to click on the context menu
            boolean clicked = contextMenu.mouseClicked(mouseX, mouseY, button);

            //then try to click on the category container and suppress it
            //let the category handle the context menu visibility
            if (!clicked && contextMenu.parent != null && contextMenu.parent.mouseClicked(mouseX, mouseY, button))
                return true;

            //otherwise, remove visibility and suppress the click only if we clicked on the context
            contextMenu.setVisible(false);
            return clicked;
        }

        //no interaction was made
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        //yeet mouse 0 and isDragging check
        return this.getFocused() != null && this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //better check for mouse released when outside element boundaries
        boolean bool = this.getFocused() != null && this.getFocused().mouseReleased(mouseX, mouseY, button);

        //remove focused when clicking
        if (bool) setFocused(null);

        this.setDragging(false);
        return bool;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        //hide previous context
        if (contextMenu != null)
            contextMenu.setVisible(false);

        //fix scrolling targeting only one child
        boolean ret = false;
        for (GuiEventListener child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY))
                ret = ret || child.mouseScrolled(mouseX, mouseY, amount);
        }
        return ret;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        egg += (char) keyCode;
        egg = egg.substring(1);
        if (EGG.equals(egg)) {
            Minecraft.getInstance().setScreen(new GameScreen(this));
            return true;
        }

        if (children().contains(panels) && panels.cycleTab(keyCode))
            return true;

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
