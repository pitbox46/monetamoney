package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import github.pitbox46.monetamoney.Config;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.containers.vault.AuctionListItemContainer;
import github.pitbox46.monetamoney.network.client.CTransactionButton;
import github.pitbox46.monetamoney.network.client.CUpdateBalance;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.screen.IStatusable;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AuctionListItemPage extends ContainerScreen<AuctionListItemContainer> implements IStatusable {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/changebalance.png");
    protected static final int STATUS_TIMER = 100;

    protected TextFieldWidget input;
    protected ITextComponent status;
    protected long statusStart;

    public AuctionListItemPage(AuctionListItemContainer screenContainer, PlayerInventory inv) {
        super(screenContainer, inv, new TranslationTextComponent("screen.monetamoney.auctionlist"));
        this.xSize = 222;
        this.ySize = 217;
    }

    @Override
    protected void init() {
        super.init();
        this.input = new TextFieldWidget(this.font, this.getBackgroundXStart() + 66, this.getBackgroundYStart() + 53, 90, 20, StringTextComponent.EMPTY);
        this.input.setMaxStringLength(9);
        this.input.setValidator(s -> {
            if(s.isEmpty()) return true;
            try {
                parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.children.add(this.input);
        this.addButton(new ImageTextButton(this.getBackgroundXStart() + 62, this.getBackgroundYStart() + 84, 100, 23, 0, 217, 23, TEXTURE, 256, 263, button -> {
            PacketHandler.CHANNEL.sendToServer(new CTransactionButton(parseInt(this.input.getText()), CTransactionButton.Button.LIST_ITEM));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslationTextComponent("button.monetamoney.listitem")));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        ITextComponent input = this.input.getMessage();
        super.resize(minecraft, width, height);
        this.input.setMessage(input);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.input.render(matrixStack, mouseX, mouseY, partialTicks);
        this.minecraft.textureManager.bindTexture(TEXTURE);
        RenderSystem.enableBlend();
        this.blit(matrixStack, this.getBackgroundXStart() + 63, this.getBackgroundYStart() + 50, 100, 217, 99, 29, 256, 263);
        /* Balance String */
        drawString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.personalbal", ClientProxy.personalBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 5, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
        drawCenteredString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.listfee", Config.LIST_FEE.get()), this.width / 2, this.getBackgroundYStart() + 50, ColorHelper.PackedColor.packColor(255, 255, 255, 255));

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
    public void closeScreen() {
        this.minecraft.displayGuiScreen(new MainPage());
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

    private int getBackgroundXStart() {
        return (width - 222) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 217) / 2;
    }

    private static int parseInt(String s) throws NumberFormatException {
        if(s.isEmpty()) return 0;

        int i = Integer.parseInt(s);
        return Math.max(i, 0);
    }
}
