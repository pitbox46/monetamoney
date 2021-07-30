package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.matrix.MatrixStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.network.*;
import github.pitbox46.monetamoney.network.client.*;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class MainPage extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/main.png");

    public MainPage() {
        super(new TranslationTextComponent("screen.monetamoney.main"));
    }

    @Override
    public void init() {
        super.init();
        int y = -60;
        int dy = 0;
        this.addButton(new ImageTextButton((width - 150) / 2, (height / 2) + y, 154, 23, 0, 136, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new COpenBalancePage(Vault.lastOpenedVault));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslationTextComponent("button.monetamoney.balance")));
        this.addButton(new ImageTextButton((width - 150) / 2, (height / 2) + y + (dy += 25), 154, 23, 0, 136, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new CTeamButton(Vault.lastOpenedVault, CTeamButton.Button.OPENPAGE));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslationTextComponent("button.monetamoney.teams")));
        this.addButton(new ImageTextButton((width - 150) / 2, (height / 2) + y + (dy += 25), 154, 23, 0, 136, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 3, (short) 0));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslationTextComponent("button.monetamoney.acctransactions")));
        this.addButton(new ImageTextButton((width - 150) / 2, (height / 2) + y + (dy += 25), 154, 23, 0, 136, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new COpenChunksPage(Vault.lastOpenedVault));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslationTextComponent("button.monetamoney.chunks")));
        this.addButton(new ImageTextButton((width - 150) / 2, (height / 2) + y + (dy += 25), 154, 23, 0, 136, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 5, (short) 0));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslationTextComponent("button.monetamoney.auction")));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int vOffset) {
        super.renderBackground(matrixStack, vOffset);
        this.minecraft.textureManager.bindTexture(TEXTURE);
        blit(matrixStack, this.getBackgroundXStart(), this.getBackgroundYStart(), 0, 0, 194, 136);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getBackgroundXStart() {
        return (width - 194) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 136) / 2;
    }
}
