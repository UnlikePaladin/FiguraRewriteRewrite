package org.moon.figura.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.moon.figura.gui.screens.AvatarScreen;
import org.moon.figura.model.rendering.EntityRenderMode;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

public class EntityPreview extends AbstractContainerElement {

    public static final ResourceLocation UNKNOWN = new FiguraIdentifier("textures/gui/unknown_entity.png");
    public static final ResourceLocation OVERLAY = new FiguraIdentifier("textures/gui/entity_overlay.png");

    //properties
    private LivingEntity entity;
    private final float pitch, yaw, scale;
    private SwitchButton button;

    //transformation data

    //rot
    private boolean isRotating = false;
    private float anchorX = 0f, anchorY = 0f;
    private float anchorAngleX = 0f, anchorAngleY = 0f;
    private float angleX, angleY;

    //scale
    private float scaledValue = 0f, scaledPrecise = 0f;
    private static final float SCALE_FACTOR = 1.1F;

    //pos
    private boolean isDragging = false;
    private int modelX, modelY;
    private float dragDeltaX, dragDeltaY;
    private float dragAnchorX, dragAnchorY;

    public EntityPreview(int x, int y, int width, int height, float scale, float pitch, float yaw, LivingEntity entity, Screen parentScreen) {
        super(x, y, width, height);

        this.scale = scale;
        this.pitch = pitch;
        this.yaw = yaw;
        this.entity = entity;

        modelX = width / 2;
        modelY = height / 2;
        angleX = pitch;
        angleY = yaw;

        //button
        children.add(button = new SwitchButton(
                x + 4, y + 4, 16, 16,
                0, 0, 16,
                new FiguraIdentifier("textures/gui/expand.png"),
                48, 32,
                FiguraText.of("gui.expand"),
                bx -> {
                    if (button.isToggled()) {
                        Minecraft.getInstance().setScreen(new AvatarScreen(scale, pitch, yaw, this.entity, parentScreen));
                    } else {
                        Minecraft.getInstance().setScreen(parentScreen);
                    }
                }));
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        if (!this.isVisible())
            return;

        if (!button.isToggled()) {
            //border
            UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE_FILL);
            //overlay
            UIHelper.renderTexture(stack, x + 1, y + 1, width - 2, height - 2, OVERLAY);
        }

        //scissors
        UIHelper.setupScissor(x + 1, y + 1, width - 2, height - 2);

        //render entity
        if (entity != null) {
            stack.pushPose();
            scaledValue = Mth.lerp((float) (1f - Math.pow(0.5f, delta)), scaledValue, scaledPrecise);
            UIHelper.drawEntity(x + modelX, y + modelY, scale + scaledValue, angleX, angleY, entity, stack, EntityRenderMode.FIGURA_GUI);
            stack.popPose();
        } else {
            stack.pushPose();

            //transforms
            stack.translate(x + modelX, y + modelY, 0f);
            float scale = this.scale / 35;
            stack.scale(scale, scale, scale);

            float xRot = Mth.wrapDegrees((angleX - pitch) * 2) / 2f;
            float yRot = Mth.wrapDegrees((angleY - yaw) * 2) / 2f;
            stack.mulPose(new Quaternionf().rotateXYZ((float) Math.toRadians(xRot), (float) Math.toRadians(yRot), 0f));

            //draw
            UIHelper.renderTexture(stack, -24, -32, 48, 64, UNKNOWN);
            stack.popPose();
        }

        UIHelper.disableScissor();

        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isVisible() || !this.isMouseOver(mouseX, mouseY))
            return false;

        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        switch (button) {
            //left click - rotate
            case 0 -> {
                //set anchor rotation

                //get starter mouse pos
                anchorX = (float) mouseX;
                anchorY = (float) mouseY;

                //get starter rotation angles
                anchorAngleX = angleX;
                anchorAngleY = angleY;

                isRotating = true;
                return true;
            }

            //right click - move
            case 1 -> {
                //get starter mouse pos
                dragDeltaX = (float) mouseX;
                dragDeltaY = (float) mouseY;

                //also get start node pos
                dragAnchorX = modelX;
                dragAnchorY = modelY;

                isDragging = true;
                return true;
            }

            //middle click - reset pos
            case 2 -> {
                isRotating = false;
                isDragging = false;
                anchorX = 0f;
                anchorY = 0f;
                anchorAngleX = 0f;
                anchorAngleY = 0f;
                angleX = pitch;
                angleY = yaw;
                scaledValue = 0f;
                scaledPrecise = 0f;
                modelX = width / 2;
                modelY = height / 2;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        //left click - stop rotating
        if (button == 0) {
            isRotating = false;
            return true;
        }

        //right click - stop dragging
        else if (button == 1) {
            isDragging = false;
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        //left click - rotate
        if (isRotating) {
            //get starter rotation angle then get hot much is moved and divided by a slow factor
            angleX = (float) (anchorAngleX + (anchorY - mouseY) / (3 / Minecraft.getInstance().getWindow().getGuiScale()));
            angleY = (float) (anchorAngleY - (anchorX - mouseX) / (3 / Minecraft.getInstance().getWindow().getGuiScale()));

            //cap to 360, so we don't get extremely high unnecessary rotation values
            if (angleX >= 360 || angleX <= -360) {
                anchorY = (float) mouseY;
                anchorAngleX = 0;
                angleX = 0;
            }
            if (angleY >= 360 || angleY <= -360) {
                anchorX = (float) mouseX;
                anchorAngleY = 0;
                angleY = 0;
            }

            return true;
        }

        //right click - move
        else if (isDragging) {
            //get how much it should move
            //get actual pos of the mouse, then subtract starter X,Y
            float x = (float) (mouseX - dragDeltaX);
            float y = (float) (mouseY - dragDeltaY);

            //move it
            modelX = (int) (dragAnchorX + x);
            modelY = (int) (dragAnchorY + y);

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!this.isVisible())
            return false;

        if (super.mouseScrolled(mouseX, mouseY, amount))
            return true;

        //scroll - scale

        //set scale direction
        float scaleDir = (amount > 0) ? SCALE_FACTOR : 1 / SCALE_FACTOR;

        //determine scale
        scaledPrecise = ((scale + scaledPrecise) * scaleDir) - scale;

        return true;
    }

    public void setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    public void setToggled(boolean toggled) {
        this.button.setToggled(toggled);
        this.button.setTooltip(toggled ? FiguraText.of("gui.minimise") : FiguraText.of("gui.expand"));
    }
}
