package org.moon.figura.gui.widgets.lists;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.avatar.AvatarManager;
import org.moon.figura.gui.screens.PermissionsScreen;
import org.moon.figura.gui.widgets.SwitchButton;
import org.moon.figura.gui.widgets.TextField;
import org.moon.figura.gui.widgets.permissions.AbstractPermPackElement;
import org.moon.figura.gui.widgets.permissions.CategoryPermPackElement;
import org.moon.figura.gui.widgets.permissions.PlayerPermPackElement;
import org.moon.figura.permissions.PermissionManager;
import org.moon.figura.permissions.PermissionPack;
import org.moon.figura.utils.FiguraIdentifier;
import org.moon.figura.utils.FiguraText;
import org.moon.figura.utils.ui.UIHelper;

import java.util.*;

public class PlayerList extends AbstractList {

    private final HashMap<UUID, PlayerPermPackElement> players = new HashMap<>();
    private final HashSet<UUID> missingPlayers = new HashSet<>();

    private final ArrayList<AbstractPermPackElement> permissionsList = new ArrayList<>();

    public final PermissionsScreen parent;
    private final TextField searchBar;
    private final SwitchButton showFigura, showDisconnected;
    private static boolean showFiguraBl, showDisconnectedBl;
    private final int entryWidth;

    private int totalHeight = 0;
    private AbstractPermPackElement maxCategory;
    public AbstractPermPackElement selectedEntry;
    private String filter = "";

    public PlayerList(int x, int y, int width, int height, PermissionsScreen parent) {
        super(x, y, width, height);
        updateScissors(1, 24, -2, -25);

        this.parent = parent;
        this.entryWidth = Math.min(width - scrollBar.getWidth() - 12, 174);

        //fix scrollbar y and height
        scrollBar.setY(y + 28);
        scrollBar.setHeight(height - 32);

        //search bar
        children.add(searchBar = new TextField(x + 4, y + 4, width - 56, 20, TextField.HintType.SEARCH, s -> {
            if (!filter.equals(s))
                scrollBar.setScrollProgress(0f);
            filter = s;
        }));

        //show figura only button
        children.add(showFigura = new SwitchButton(x + width - 48, y + 4, 20, 20, 0, 0, 20, new FiguraIdentifier("textures/gui/show_figura.png"), 60, 40, FiguraText.of("gui.permissions.figura_only.tooltip"), button -> showFiguraBl = ((SwitchButton) button).isToggled()));
        showFigura.setToggled(showFiguraBl);

        //show disconnected button
        children.add(showDisconnected = new SwitchButton(x + width - 24, y + 4, 20, 20, 0, 0, 20, new FiguraIdentifier("textures/gui/show_disconnected.png"), 60, 40, FiguraText.of("gui.permissions.disconnected.tooltip"), button -> showDisconnectedBl = ((SwitchButton) button).isToggled()));
        showDisconnected.setToggled(showDisconnectedBl);

        //initial load
        loadGroups();
        loadPlayers();

        //select self
        selectLocalPlayer();
    }

    @Override
    public void tick() {
        //update players
        loadPlayers();
        super.tick();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        //background and scissors
        UIHelper.renderSliced(stack, x, y, width, height, UIHelper.OUTLINE_FILL);
        UIHelper.setupScissor(x + scissorsX, y + scissorsY, width + scissorsWidth, height + scissorsHeight);

        totalHeight = 0;
        for (AbstractPermPackElement pack : permissionsList) {
            if (pack.isVisible())
                totalHeight += pack.getHeight() + 8;
        }

        //scrollbar visible
        scrollBar.visible = totalHeight > height - 32;
        scrollBar.setScrollRatio(permissionsList.isEmpty() ? 0f : (float) totalHeight / permissionsList.size(), totalHeight - (height - 32));

        //render stuff
        int xOffset = width / 2 - 87 - (scrollBar.visible ? 7 : 0);
        int playerY = scrollBar.visible ? (int) -(Mth.lerp(scrollBar.getScrollProgress(), -32, totalHeight - height)) : 32;
        boolean hidden = false;

        for (AbstractPermPackElement pack : permissionsList) {
            if ((hidden || !pack.isVisible()) && (pack instanceof PlayerPermPackElement p && !p.dragged)) {
                pack.visible = false;
                continue;
            }

            pack.visible = true;
            pack.setX(x + Math.max(4, xOffset));
            pack.setY(y + playerY);

            if (pack.getY() + pack.getHeight() > y + scissorsY)
                pack.render(stack, mouseX, mouseY, delta);

            playerY += pack.getHeight() + 8;
            if (playerY > height)
                hidden = true;
        }

        //reset scissor
        UIHelper.disableScissor();

        //render children
        super.render(stack, mouseX, mouseY, delta);
    }

    @Override
    public List<? extends GuiEventListener> contents() {
        return permissionsList;
    }

    private void loadGroups() {
        for (PermissionPack container : PermissionManager.CATEGORIES.values()) {
            CategoryPermPackElement group = new CategoryPermPackElement(entryWidth, container, this);
            permissionsList.add(group);
            children.add(group);
            maxCategory = group;
        }
    }

    private void loadPlayers() {
        //reset missing players
        missingPlayers.clear();
        missingPlayers.addAll(players.keySet());

        //for all players
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        List<UUID> playerList = connection == null ? List.of() : new ArrayList<>(connection.getOnlinePlayerIds());
        for (UUID uuid : playerList) {
            //get player
            PlayerInfo player = connection.getPlayerInfo(uuid);
            if (player == null)
                continue;

            //get player data
            String name = player.getProfile().getName();
            ResourceLocation skin = player.getSkinLocation();
            Avatar avatar = AvatarManager.getAvatarForPlayer(uuid);

            //filter check
            if ((!name.toLowerCase().contains(filter.toLowerCase()) && !uuid.toString().contains(filter.toLowerCase())) || (showFigura.isToggled() && !FiguraMod.isLocal(uuid) && (avatar == null || avatar.nbt == null)))
                continue;

            //player is not missing
            missingPlayers.remove(uuid);

            PlayerPermPackElement element = players.computeIfAbsent(uuid, uuid1 -> {
                PlayerPermPackElement entry = new PlayerPermPackElement(entryWidth, name, PermissionManager.get(uuid1), skin, uuid1, this);

                permissionsList.add(entry);
                children.add(entry);

                return entry;
            });
            element.disconnected = false;
        }

        if (filter.isEmpty() && showDisconnected.isToggled()) {
            for (Avatar avatar : AvatarManager.getLoadedAvatars()) {
                UUID id = avatar.owner;

                if (playerList.contains(id))
                    continue;

                missingPlayers.remove(id);

                PlayerPermPackElement element = players.computeIfAbsent(id, uuid -> {
                    PlayerPermPackElement entry = new PlayerPermPackElement(entryWidth, avatar.entityName, PermissionManager.get(uuid), null, uuid, this);

                    permissionsList.add(entry);
                    children.add(entry);

                    return entry;
                });
                element.disconnected = true;
            }
        }

        //remove missing players
        for (UUID missingID : missingPlayers) {
            PlayerPermPackElement entry = players.remove(missingID);
            permissionsList.remove(entry);
            children.remove(entry);
        }

        sortList();

        //select local if current selected is missing
        if (selectedEntry instanceof PlayerPermPackElement player && missingPlayers.contains(player.getOwner()))
            selectLocalPlayer();
    }

    private void sortList() {
        permissionsList.sort(AbstractPermPackElement::compareTo);
        children.sort((element1, element2) -> {
            if (element1 instanceof AbstractPermPackElement container1 && element2 instanceof AbstractPermPackElement container2)
                return container1.compareTo(container2);
            return 0;
        });
    }

    private void selectLocalPlayer() {
        PlayerPermPackElement local = Minecraft.getInstance().player != null ? players.get(Minecraft.getInstance().player.getUUID()) : null;
        if (local != null) {
            local.onPress();
        } else {
            maxCategory.onPress();
        }

        scrollToSelected();
    }

    public void updateScroll() {
        //store old scroll pos
        double pastScroll = (totalHeight - height) * scrollBar.getScrollProgress();

        //get new height
        totalHeight = 0;
        for (AbstractPermPackElement pack : permissionsList) {
            if (pack.isVisible())
                totalHeight += pack.getHeight() + 8;
        }

        //set new scroll percentage
        scrollBar.setScrollProgress(pastScroll / (totalHeight - height));
    }

    public void setY(int y) {
        this.y = y;
        scrollBar.setY(y + 28);
        searchBar.setPos(searchBar.x, y + 4);
        showFigura.setY(y + 4);
        showDisconnected.setY(y + 4);
    }

    public int getCategoryAt(double y) {
        int ret = -1;
        for (AbstractPermPackElement element : permissionsList)
            if (element instanceof CategoryPermPackElement group && group.visible && y >= group.getY())
                ret++;
        return Math.max(ret, 0);
    }

    public void scrollToSelected() {
        double y = 0;

        //get height
        totalHeight = 0;
        for (AbstractPermPackElement pack : permissionsList) {
            if (pack instanceof PlayerPermPackElement && !pack.isVisible())
                continue;

            if (pack == selectedEntry)
                y = totalHeight;
            else
                totalHeight += pack.getHeight() + 8;
        }

        //set scroll
        scrollBar.setScrollProgressNoAnim(y / totalHeight);
    }

}
