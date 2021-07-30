package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.matrix.MatrixStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.containers.vault.AuctionBuyContainer;
import github.pitbox46.monetamoney.network.*;
import github.pitbox46.monetamoney.network.client.CTransactionButton;
import github.pitbox46.monetamoney.network.client.CUpdateBalance;
import github.pitbox46.monetamoney.screen.IStatusable;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AuctionBuyPage extends ContainerScreen<AuctionBuyContainer> implements IStatusable {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/changebalance.png");
    protected static final int STATUS_TIMER = 100;

    protected ITextComponent status;
    protected long statusStart;
    protected boolean editMode;

    public AuctionBuyPage(AuctionBuyContainer screenContainer, PlayerInventory inv) {
        super(screenContainer, inv, new TranslationTextComponent("screen.monetamoney.auctionbuy"));
        this.xSize = 222;
        this.ySize = 217;
    }

    @Override
    protected void init() {
        super.init();
        this.editMode = this.container.handler.getStackInSlot(0).getOrCreateTag().getString("owner").equals(this.minecraft.player.getGameProfile().getName());
        if(!editMode) {
            this.addButton(new ImageTextButton(this.getBackgroundXStart() + 62, this.getBackgroundYStart() + 84, 100, 23, 0, 217, 23, TEXTURE, 256, 263, button -> {
                PacketHandler.CHANNEL.sendToServer(new CTransactionButton(this.container.handler.getStackInSlot(0).getOrCreateTag().getInt("price"), CTransactionButton.Button.PURCHASE));
                PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
            }, new TranslationTextComponent("button.monetamoney.buy")));
        } else {
            this.addButton(new ImageTextButton(this.getBackgroundXStart() + 62, this.getBackgroundYStart() + 84, 100, 23, 0, 217, 23, TEXTURE, 256, 263, button -> {
                PacketHandler.CHANNEL.sendToServer(new CTransactionButton(0, CTransactionButton.Button.REMOVE));
                PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
            }, new TranslationTextComponent("button.monetamoney.remove")));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.drawInfoStrings(matrixStack);
        this.renderStatus(matrixStack);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        this.minecraft.textureManager.bindTexture(TEXTURE);
        this.blit(matrixStack, this.getBackgroundXStart(), this.getBackgroundYStart(), 0, 0, 222, 217, 256, 263);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if(slotIn != null && slotIn.slotNumber < 36) {
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
        }
    }

    protected void drawInfoStrings(MatrixStack matrixStack) {
        if(!this.container.handler.getStackInSlot(0).isEmpty()) {
            drawCenteredString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.owner", this.container.handler.getStackInSlot(0).getTag().getString("owner")), width / 2, this.getBackgroundYStart() + 45, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
            drawCenteredString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.price", this.container.handler.getStackInSlot(0).getTag().getInt("price")), width / 2, this.getBackgroundYStart() + 55, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
        }
        drawString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.personalbal", ClientProxy.personalBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 5, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
    }

    protected void renderStatus(MatrixStack matrixStack) {
        if(status != null && this.minecraft.world.getGameTime() - this.statusStart < STATUS_TIMER - 2) {
            int alpha = (int) (255 - 255 * (this.minecraft.world.getGameTime() - this.statusStart) / STATUS_TIMER);
            drawCenteredString(matrixStack, this.font, this.status, width / 2, getBackgroundYStart() + 200, ColorHelper.PackedColor.packColor(alpha, 255, 255, 255));
        } else {
            status = null;
            statusStart = 0;
        }
    }

    public void setStatus(ITextComponent message) {
        this.status = message;
        this.statusStart = this.minecraft.world.getGameTime();
    }

    @Override
    public void closeScreen() {
        this.minecraft.displayGuiScreen(new MainPage());
    }

    private int getBackgroundXStart() {
        return (width - 222) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 217) / 2;
    }
}
