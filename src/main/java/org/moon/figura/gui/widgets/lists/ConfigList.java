package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;
import org.moon.figura.config.ConfigManager;
import org.moon.figura.config.ConfigType;
import org.moon.figura.gui.screens.ConfigScreen;
import org.moon.figura.gui.widgets.TextField;
import org.moon.figura.gui.widgets.config.CategoryWidget;
import org.moon.figura.gui.widgets.config.InputElement;
import org.moon.figura.utils.ui.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class ConfigList extends AbstractList {

    private final List<CategoryWidget> configs = new ArrayList<>();
    public final ConfigScreen parentScreen;
    public KeyMapping focusedBinding;

    private int totalHeight = 0;

    public ConfigList(int x, int y, int width, int height, ConfigScreen parentScreen) {
        super(x, y, width, height);
        this.parentScreen = parentScreen;
        updateList();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE_FILL);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);

        //scrollbar
        totalHeight = -4;
        for (CategoryWidget config : configs)
            totalHeight += config.getHeight() + 8;
        int entryHeight = configs.isEmpty() ? 0 : totalHeight / configs.size();

        scrollBar.visible = totalHeight > height;
        scrollBar.setScrollRatio(entryHeight, totalHeight - height);

        //render list
        int xOffset = scrollBar.visible ? 4 : 11;
        int yOffset = scrollBar.visible ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -4, totalHeight - height)) : 4;
        for (CategoryWidget config : configs) {
            config.setPos(x + xOffset, y + yOffset);
            yOffset += config.getHeight() + 8;
        }

        //children
        super.render(stack, mouseX, mouseY, delta);

        //reset scissor
        UIHelper.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //fix mojang focusing for text fields
        for (CategoryWidget categoryWidget : configs) {
            for (GuiEventListener children : categoryWidget.children()) {
                if (children instanceof InputElement inputElement) {
                    TextField field = inputElement.getTextField();
                    field.getField().setFocused(field.isEnabled() && field.isMouseOver(mouseX, mouseY));
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void updateList() {
        //clear old widgets
        for (CategoryWidget config : configs)
            children.remove(config);
        configs.clear();

        //add configs
        for (ConfigType.Category category : ConfigManager.CATEGORIES_REGISTRY.values()) {
            CategoryWidget widget = new CategoryWidget(width - 22, category, this);

            for (ConfigType<?> config : category.children)
                widget.addConfig(config);

            configs.add(widget);
            children.add(widget);
        }

        //fix expanded status
        for (CategoryWidget config : configs)
            config.setShowChildren(config.isShowingChildren());
    }

    public void updateScroll() {
        //store old scroll pos
        double pastScroll = (totalHeight - height) * scrollBar.getScrollProgress();

        //get new height
        totalHeight = -4;
        for (CategoryWidget config : configs)
            totalHeight += config.getHeight() + 8;

        //set new scroll percentage
        scrollBar.setScrollProgress(pastScroll / (totalHeight - height));
    }

    public boolean hasChanges() {
        for (CategoryWidget config : configs)
            if (config.isChanged())
                return true;
        return false;
    }

    public boolean updateKey(InputConstants.Key key) {
        if (focusedBinding == null)
            return false;

        focusedBinding.setKey(key);
        focusedBinding = null;

        updateKeybinds();
        return true;
    }

    public void updateKeybinds() {
        for (CategoryWidget widget : configs)
            widget.updateKeybinds();
    }
}
