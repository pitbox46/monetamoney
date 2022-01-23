package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.client.CTeamTransactionButton;
import github.pitbox46.monetamoney.network.client.CTransactionButton;
import github.pitbox46.monetamoney.network.client.CUpdateBalance;
import github.pitbox46.monetamoney.screen.IStatusable;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;

public class AccountTransactionPage extends AbstractContainerScreen<AccountTransactionContainer> implements IStatusable {
    protected static final int STATUS_TIMER = 100;
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/changebalance.png");
    protected EditBox input;
    protected Checkbox checkboxButton;
    protected Component status;
    protected long statusStart;

    public AccountTransactionPage(AccountTransactionContainer screenContainer, Inventory inv) {
        super(screenContainer, inv, new TranslatableComponent("screen.monetamoney.accounttransaction"));
        this.imageWidth = 222;
        this.imageHeight = 217;
    }

    private static int parseInt(String s) throws NumberFormatException {
        if (s.isEmpty()) return 0;

        int i = Integer.parseInt(s);
        return Math.max(i, 0);
    }

    @Override
    protected void init() {
        super.init();
        this.input = new EditBox(this.font, this.getBackgroundXStart() + 66, this.getBackgroundYStart() + 53, 90, 20, TextComponent.EMPTY);
        this.input.setMaxLength(9);
        this.input.setFilter(s -> {
            if (s.isEmpty()) return true;
            try {
                parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.addRenderableWidget(this.input);
        this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 10, this.getBackgroundYStart() + 84, 100, 23, 0, 217, 23, TEXTURE, 256, 263, button -> {
            if (checkboxButton.selected()) {
                PacketHandler.CHANNEL.sendToServer(new CTeamTransactionButton(Vault.lastOpenedVault, parseInt(this.input.getValue()), CTeamTransactionButton.Button.WITHDRAW));
            } else {
                PacketHandler.CHANNEL.sendToServer(new CTransactionButton(parseInt(this.input.getValue()), CTransactionButton.Button.WITHDRAW));
            }
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.withdraw")));

        this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 115, this.getBackgroundYStart() + 84, 100, 23, 0, 217, 23, TEXTURE, 256, 263, button -> {
            if (checkboxButton.selected()) {
                PacketHandler.CHANNEL.sendToServer(new CTeamTransactionButton(Vault.lastOpenedVault, parseInt(this.input.getValue()), CTeamTransactionButton.Button.DEPOSIT));
            } else {
                PacketHandler.CHANNEL.sendToServer(new CTransactionButton(parseInt(this.input.getValue()), CTransactionButton.Button.DEPOSIT));
            }
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.deposit")));

        this.checkboxButton = new Checkbox(this.getBackgroundXStart() + 126, this.getBackgroundYStart() + 19, 150, 20, new TranslatableComponent("button.monetamoney.isteams"), false, true);
        this.addRenderableWidget(this.checkboxButton);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        Component input = this.input.getMessage();
        super.resize(minecraft, width, height);
        this.input.setMessage(input);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.input.render(matrixStack, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        this.blit(matrixStack, this.getBackgroundXStart() + 63, this.getBackgroundYStart() + 50, 100, 217, 99, 29, 256, 263);

        drawString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.personalbal", ClientProxy.personalBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 5, FastColor.ARGB32.color(255, 255, 255, 255));
        drawString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.teambal", ClientProxy.teamBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 15, FastColor.ARGB32.color(255, 255, 255, 255));
        this.renderStatus(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
        this.renderBackground(matrixStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(matrixStack, this.getBackgroundXStart(), this.getBackgroundYStart(), 0, 0, 222, 217, 256, 263);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) {
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new MainPage());
    }

    protected void renderStatus(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (status != null && this.minecraft.level.getGameTime() - this.statusStart < STATUS_TIMER - 2) {
            int alpha = (int) (255 - 255 * (this.minecraft.level.getGameTime() - this.statusStart) / STATUS_TIMER);
            drawCenteredString(matrixStack, this.font, this.status, width / 2, getBackgroundYStart() + 200, FastColor.ARGB32.color(alpha, 255, 255, 255));
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

    private int getBackgroundXStart() {
        return (width - 222) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 217) / 2;
    }
}
