package org.moon.figura.gui.screens;

import net.minecraft.client.gui.screens.Screen;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.widgets.PianoWidget;
import org.moon.figura.gui.widgets.Button;
import org.moon.figura.gui.widgets.lists.SoundsList;
import org.moon.figura.utils.FiguraText;

public class SoundScreen extends AbstractPanelScreen {

    private final Screen sourcePanel;
    private PianoWidget piano;

    public SoundScreen(AbstractPanelScreen parentScreen) {
        super(parentScreen.parentScreen, FiguraText.of("gui.panels.title.sound"));
        sourcePanel = parentScreen;
    }

    @Override
    public Class<? extends Screen> getSelectedPanel() {
        return sourcePanel.getClass();
    }

    @Override
    protected void init() {
        super.init();

        Avatar owner = AvatarManager.getAvatarForPlayer(FiguraMod.getLocalPlayerUUID());

        //list
        int listWidth = Math.min(this.width - 8, 420);
        int listX = (this.width - listWidth) / 2;
        SoundsList list;
        addRenderableWidget(list = new SoundsList(listX, 28, listWidth, height - 120, owner));

        //keys
        addRenderableWidget(piano = new PianoWidget(listX, height - 88, listWidth, 60, list::getSound));

        //back
        addRenderableWidget(new Button(width / 2 - 60, height - 24, 120, 20, FiguraText.of("gui.done"), null,
                bx -> this.minecraft.setScreen(sourcePanel)
        ));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        piano.pressed = button == 0;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        piano.pressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
