package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.client.CTransactionButton;
import github.pitbox46.monetamoney.network.client.CUpdateBalance;
import github.pitbox46.monetamoney.screen.IStatusable;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class AuctionBuyPage extends AbstractContainerScreen<AuctionBuyContainer> implements IStatusable {
    protected static final int STATUS_TIMER = 100;
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/changebalance.png");
    protected Component status;
    protected long statusStart;
    protected boolean editMode;

    public AuctionBuyPage(AuctionBuyContainer screenContainer, Inventory inv) {
        super(screenContainer, inv, new TranslatableComponent("screen.monetamoney.auctionbuy"));
        this.imageWidth = 222;
        this.imageHeight = 217;
    }

    @Override
    protected void init() {
        super.init();
        this.editMode = this.menu.handler.getStackInSlot(0).getOrCreateTag().getString("owner").equals(this.minecraft.player.getGameProfile().getName());
        if (!editMode) {
            this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 62, this.getBackgroundYStart() + 84, 100, 23, 0, 217, 23, TEXTURE, 256, 263, button -> {
                PacketHandler.CHANNEL.sendToServer(new CTransactionButton(this.menu.getItemBuyPrice(), CTransactionButton.Button.BUY));
                PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
            }, new TranslatableComponent("button.monetamoney.buy")));
        } else {
            this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 62, this.getBackgroundYStart() + 84, 100, 23, 0, 217, 23, TEXTURE, 256, 263, button -> {
                PacketHandler.CHANNEL.sendToServer(new CTransactionButton(0, CTransactionButton.Button.REMOVE));
                PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
            }, new TranslatableComponent("button.monetamoney.remove")));
        }
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.drawInfoStrings(matrixStack);
        this.renderStatus(matrixStack);
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
    protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (slotIn != null && slotIn.index < 36) {
            super.slotClicked(slotIn, slotId, mouseButton, type);
        }
    }

    protected void drawInfoStrings(PoseStack matrixStack) {
        if (!this.menu.handler.getStackInSlot(0).isEmpty()) {
            drawCenteredString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.owner", this.menu.handler.getStackInSlot(0).getTag().getString("owner")), width / 2, this.getBackgroundYStart() + 45, FastColor.ARGB32.color(255, 255, 255, 255));
            drawCenteredString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.price", this.menu.getItemBuyPrice()), width / 2, this.getBackgroundYStart() + 55, FastColor.ARGB32.color(255, 255, 255, 255));
        }
        drawString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.personalbal", ClientProxy.personalBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 5, FastColor.ARGB32.color(255, 255, 255, 255));
    }

    protected void renderStatus(PoseStack matrixStack) {
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

    @Override
    public void onClose() {
        this.minecraft.setScreen(new MainPage());
    }

    private int getBackgroundXStart() {
        return (width - 222) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 217) / 2;
    }
}
