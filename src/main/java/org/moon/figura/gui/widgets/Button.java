package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.ui.UIHelper;

public class Button extends net.minecraft.client.gui.components.Button {

    //default textures
    private static final ResourceLocation TEXTURE = new FiguraIdentifier("textures/gui/button.png");

    //texture data
    protected Integer u;
    protected Integer v;

    protected final Integer textureWidth;
    protected final Integer textureHeight;
    protected final Integer regionSize;
    protected final ResourceLocation texture;

    //extra fields
    protected Component tooltip;
    private boolean hasBackground = true;

    //texture and text constructor
    public Button(int x, int y, int width, int height, Integer u, Integer v, Integer regionSize, ResourceLocation texture, Integer textureWidth, Integer textureHeight, Component text, Component tooltip, OnPress pressAction) {
        super(x, y, width, height, text, pressAction, DEFAULT_NARRATION);

        this.u = u;
        this.v = v;
        this.regionSize = regionSize;
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.tooltip = tooltip;
    }

    //text constructor
    public Button(int x, int y, int width, int height, Component text, Component tooltip, OnPress pressAction) {
        this(x, y, width, height, null, null, null, null, null, null, text, tooltip, pressAction);
    }

    //texture constructor
    public Button(int x, int y, int width, int height, int u, int v, int regionSize, ResourceLocation texture, int textureWidth, int textureHeight, Component tooltip, OnPress pressAction) {
        this(x, y, width, height, u, v, regionSize, texture, textureWidth, textureHeight, Component.empty(), tooltip, pressAction);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.visible)
            return;

        //update hovered
        this.setHovered(this.isMouseOver(mouseX, mouseY));

         //render button
        this.renderWidget(stack, mouseX, mouseY, delta);
    }

    @Override
    public void renderWidget(PoseStack stack, int mouseX, int mouseY, float delta) {
        //render texture
        if (this.texture != null) {
            renderTexture(stack, delta);
        } else {
            renderDefaultTexture(stack, delta);
        }

        //render text
        renderText(stack, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.isHoveredOrFocused() && this.isMouseOver(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        boolean over = UIHelper.isMouseOver(getX(), getY(), width, height, mouseX, mouseY);
        if (over && this.tooltip != null)
            UIHelper.setTooltip(this.tooltip);
        return over;
    }

    protected void renderDefaultTexture(PoseStack stack, float delta) {
        UIHelper.renderSliced(stack, getX(), getY(), width, height, getUVStatus() * 16f, this.hasBackground ? 0f : 16f, 16, 16, 48, 32, TEXTURE);
    }

    protected void renderTexture(PoseStack stack, float delta) {
        //uv transforms
        int u = this.u + this.getUVStatus() * this.regionSize;
        int v = this.v + (this instanceof SwitchButton sw && sw.isToggled() ? this.regionSize : 0);

        //draw texture
        UIHelper.setupTexture(this.texture);

        int size = this.regionSize;
        blit(stack, this.getX() + this.width / 2 - size / 2, this.getY() + this.height / 2 - size / 2, u, v, size, size, this.textureWidth, this.textureHeight);
    }

    protected void renderText(PoseStack stack, float delta) {
        UIHelper.renderCenteredScrollingText(stack, getMessage(), getX() + 1, getY(), getWidth() - 2, getHeight(), getTextColor());
    }

    protected void renderVanillaBackground(PoseStack stack, int mouseX, int mouseY, float delta) {
        Component message = getMessage();
        setMessage(Component.empty());
        super.renderWidget(stack, mouseX, mouseY, delta);
        setMessage(message);
    }

    protected int getUVStatus() {
        if (!this.active)
            return 0;
        else if (this.isHoveredOrFocused())
            return 2;
        else
            return 1;
    }

    protected int getTextColor() {
        return (!this.active ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE).getColor();
    }

    public void setUV(int x, int y) {
        this.u = x;
        this.v = y;
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = tooltip;
    }

    public Component getTooltip() {
        return tooltip;
    }

    public void shouldHaveBackground(boolean bool) {
        this.hasBackground = bool;
    }

    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
    }

    public void run() {
        playDownSound(Minecraft.getInstance().getSoundManager());
        onPress();
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
