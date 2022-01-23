package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.client.CTeamButton;
import github.pitbox46.monetamoney.network.client.CUpdateBalance;
import github.pitbox46.monetamoney.network.server.SOpenTeamsPage;
import github.pitbox46.monetamoney.screen.IStatusable;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class TeamsPage extends Screen implements IStatusable {
    protected static final int STATUS_TIMER = 100;
    private final static ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/teams.png");
    protected Component status;
    protected long statusStart;
    protected Team team;
    protected SOpenTeamsPage.Type type;
    protected PlayerList list;

    public TeamsPage(Team team, SOpenTeamsPage.Type type) {
        super(new TranslatableComponent("screen.monetamoney.teams"));
        this.team = team;
        this.type = type;
    }

    @Override
    protected void init() {
        super.init();
        if (!this.team.isNull() && (this.type == SOpenTeamsPage.Type.INNONE || this.type == SOpenTeamsPage.Type.INSAME)) {
            this.list = new PlayerList(minecraft, team, 128, 72, this.getBackgroundXStart() + 19, this.getBackgroundYStart() + 21, 14);
            this.addRenderableWidget(list);
            if (this.team.leader.equals(this.minecraft.player.getGameProfile().getName())) {
                this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 16, this.getBackgroundYStart() + 104, 137, 23, 0, 136, 23, TEXTURE, (button) -> {
                    PacketHandler.CHANNEL.sendToServer(new CTeamButton(team.pos, CTeamButton.Button.REMOVE));
                    PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
                }, new TranslatableComponent("button.monetamoney.dismantle")));
                /* Lock Button */
                if (team.locked) {
                    this.addRenderableWidget(new ImageButton(this.getBackgroundXStart() + 153, this.getBackgroundYStart() + 109, 7, 9, 137, 150, 9, TEXTURE, (button) -> {
                        PacketHandler.CHANNEL.sendToServer(new CTeamButton(team.pos, CTeamButton.Button.UNLOCK));
                        PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
                    }));
                } else {
                    this.addRenderableWidget(new ImageButton(this.getBackgroundXStart() + 153, this.getBackgroundYStart() + 109, 7, 9, 137, 159, -9, TEXTURE, (button) -> {
                        PacketHandler.CHANNEL.sendToServer(new CTeamButton(team.pos, CTeamButton.Button.LOCK));
                        PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
                    }));
                }
            } else if (this.team.members.contains(this.minecraft.player.getGameProfile().getName())) {
                this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 16, this.getBackgroundYStart() + 104, 137, 23, 0, 136, 23, TEXTURE, (button) -> {
                    PacketHandler.CHANNEL.sendToServer(new CTeamButton(team.pos, CTeamButton.Button.LEAVE));
                    PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
                }, new TranslatableComponent("button.monetamoney.leave")));
            } else if (!this.team.locked) {
                this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 16, this.getBackgroundYStart() + 104, 137, 23, 0, 136, 23, TEXTURE, (button) -> {
                    PacketHandler.CHANNEL.sendToServer(new CTeamButton(team.pos, CTeamButton.Button.JOIN));
                    PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
                }, new TranslatableComponent("button.monetamoney.join")));
            } else {
                this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 16, this.getBackgroundYStart() + 104, 137, 23, 0, 136, 23, TEXTURE, (button) -> {
                    //Do nothing. Easier to make a button that does nothing than to blit the button's image then write text over it
                }, new TranslatableComponent("button.monetamoney.locked")));
            }
        } else if (!this.team.isNull() && this.type == SOpenTeamsPage.Type.INDIFFERENT) {
            this.list = new PlayerList(minecraft, team, 128, 72, this.getBackgroundXStart() + 19, this.getBackgroundYStart() + 21, 14);
            this.addRenderableWidget(list);
            if (!this.team.locked) {
                this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 16, this.getBackgroundYStart() + 104, 137, 23, 0, 136, 23, TEXTURE, (button) -> {
                    PacketHandler.CHANNEL.sendToServer(new CTeamButton(Vault.lastOpenedVault, CTeamButton.Button.SWITCH));
                    PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
                }, new TranslatableComponent("button.monetamoney.switchteam")));
            } else {
                this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 16, this.getBackgroundYStart() + 104, 137, 23, 0, 136, 23, TEXTURE, (button) -> {
                    //Do nothing. Easier to make a button that does nothing than to blit the button's image then write text over it
                }, new TranslatableComponent("button.monetamoney.locked")));
            }
        } else {
            this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 16, this.getBackgroundYStart() + 104, 137, 23, 0, 136, 23, TEXTURE, (button) -> {
                PacketHandler.CHANNEL.sendToServer(new CTeamButton(Vault.lastOpenedVault, CTeamButton.Button.CREATE));
                PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
            }, new TranslatableComponent("button.monetamoney.createteam")));
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        if (this.list != null) {
            this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        blit(matrixStack, this.getBackgroundXStart(), this.getBackgroundYStart(), 0, 0, 166, 136);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderStatus(matrixStack);
    }

    @Override
    public void renderBackground(PoseStack matrixStack, int vOffset) {
        super.renderBackground(matrixStack, vOffset);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(matrixStack, this.getBackgroundXStart() + 19, this.getBackgroundYStart() + 21, 0, 182, 128, 72);
    }

    protected void renderStatus(PoseStack matrixStack) {
        if (status != null && this.minecraft.level.getGameTime() - this.statusStart < STATUS_TIMER - 2) {
            int alpha = (int) (255 - 255 * (this.minecraft.level.getGameTime() - this.statusStart) / STATUS_TIMER);
            drawCenteredString(matrixStack, this.font, this.status, width / 2, getBackgroundYStart() + 100, FastColor.ARGB32.color(alpha, 255, 255, 255));
        } else {
            status = null;
            statusStart = 0;
        }
    }

    public void setStatus(Component message) {
        this.status = message;
        this.statusStart = this.minecraft.level.getGameTime();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new MainPage());
    }

    private int getBackgroundXStart() {
        return (width - 166) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 136) / 2;
    }

    class PlayerList extends ObjectSelectionList<PlayerList.PlayerEntry> {
        protected final Team team;

        public PlayerList(Minecraft mcIn, Team team, int widthIn, int heightIn, int leftIn, int topIn, int slotHeightIn) {
            super(mcIn, widthIn, heightIn, topIn, topIn + heightIn, slotHeightIn);
            this.team = team;
            this.x0 = leftIn;
            this.x1 = leftIn + widthIn;
            for (String player : this.team.members) {
                this.addEntry(new PlayerEntry(player));
            }

        }

        @Override
        public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            this.renderList(matrixStack, this.getRowLeft(), this.y0 + 4 - (int) this.getScrollAmount(), mouseX, mouseY, partialTicks);
        }

        @Override
        public int getRowWidth() {
            return 128;
        }

        class PlayerEntry extends ObjectSelectionList.Entry<PlayerEntry> {
            protected final String player;

            public PlayerEntry(String player) {
                this.player = player;
            }

            @Override
            public void render(PoseStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                if (minecraft.getConnection().getOnlinePlayers().stream().anyMatch(info -> info.getProfile().getName().equals(player))) {
                    //Online
                    blit(matrixStack, getRight() - 11, top + 4, 137, 143, 7, 7);
                } else {
                    //Offline
                    blit(matrixStack, getRight() - 11, top + 4, 137, 136, 7, 7);
                }
                drawString(matrixStack, TeamsPage.this.font, player, left + 5, top + 2, FastColor.ARGB32.color(255, 255, 255, 255));
            }

            @Override
            public Component getNarration() {
                return null;
            }
        }
    }
}
