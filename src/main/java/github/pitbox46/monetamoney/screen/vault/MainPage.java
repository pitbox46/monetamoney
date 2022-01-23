package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.client.*;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class MainPage extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/main.png");

    public MainPage() {
        super(new TranslatableComponent("screen.monetamoney.main"));
    }

    @Override
    public void init() {
        super.init();
        int y = -60;
        int dy = 0;
        this.addRenderableWidget(new ImageTextButton((width - 150) / 2, (height / 2) + y, 154, 23, 0, 136, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new COpenBalancePage(Vault.lastOpenedVault));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.balance")));
        this.addRenderableWidget(new ImageTextButton((width - 150) / 2, (height / 2) + y + (dy += 25), 154, 23, 0, 136, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new CTeamButton(Vault.lastOpenedVault, CTeamButton.Button.OPENPAGE));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.teams")));
        this.addRenderableWidget(new ImageTextButton((width - 150) / 2, (height / 2) + y + (dy += 25), 154, 23, 0, 136, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 3, (short) 0));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.acctransactions")));
        this.addRenderableWidget(new ImageTextButton((width - 150) / 2, (height / 2) + y + (dy += 25), 154, 23, 0, 136, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new COpenChunksPage(Vault.lastOpenedVault));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.chunks")));
        this.addRenderableWidget(new ImageTextButton((width - 150) / 2, (height / 2) + y + (dy += 25), 76, 23, 0, 182, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 8, (short) 0));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.shop")));
        this.addRenderableWidget(new ImageTextButton((width + 4) / 2, (height / 2) + y + dy, 76, 23, 0, 182, 23, TEXTURE, (button) -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 5, (short) 0));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.auction")));

    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(PoseStack matrixStack, int vOffset) {
        super.renderBackground(matrixStack, vOffset);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
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
