package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.client.CTransactionButton;
import github.pitbox46.monetamoney.network.client.CUpdateBalance;
import github.pitbox46.monetamoney.screen.IStatusable;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;

public class AuctionListItemPage extends AbstractContainerScreen<AuctionListItemContainer> implements IStatusable {
    protected static final int STATUS_TIMER = 100;
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/changebalance.png");
    protected EditBox input;
    protected Component status;
    protected long statusStart;

    public AuctionListItemPage(AuctionListItemContainer screenContainer, Inventory inv) {
        super(screenContainer, inv, new TranslatableComponent("screen.monetamoney.auctionlist"));
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
        this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 62, this.getBackgroundYStart() + 84, 100, 23, 0, 217, 23, TEXTURE, 256, 263, button -> {
            PacketHandler.CHANNEL.sendToServer(new CTransactionButton(parseInt(this.input.getValue()), CTransactionButton.Button.LIST_ITEM));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.listitem")));
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
        /* Balance String */
        drawString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.personalbal", ClientProxy.personalBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 5, FastColor.ARGB32.color(255, 255, 255, 255));

        TranslatableComponent fees = new TranslatableComponent("info.monetamoney.fees");
        drawString(matrixStack, this.font, fees, this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 18, FastColor.ARGB32.color(255, 255, 255, 255));

        TranslatableComponent listFee = new TranslatableComponent("info.monetamoney.listfee", ClientProxy.listFee);
        drawString(matrixStack, this.font, listFee, this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 28, FastColor.ARGB32.color(255, 255, 255, 255));

        TranslatableComponent dailyFee = new TranslatableComponent("info.monetamoney.dailylistfee", ClientProxy.dailyListFee);
        drawString(matrixStack, this.font, dailyFee, this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 38, FastColor.ARGB32.color(255, 255, 255, 255));

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
    public void onClose() {
        this.minecraft.setScreen(new MainPage());
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

    private int getBackgroundXStart() {
        return (width - 222) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 217) / 2;
    }
}
