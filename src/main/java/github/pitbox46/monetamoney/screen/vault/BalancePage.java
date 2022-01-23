package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.pitbox46.monetamoney.network.ClientProxy;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class BalancePage extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/main.png");

    public BalancePage() {
        super(new TranslatableComponent("screen.monetamoney.balance"));
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.personalbal", ClientProxy.personalBalance), width / 2, this.getBackgroundYStart() + 30, FastColor.ARGB32.color(255, 255, 255, 255));
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.teambal", ClientProxy.teamBalance), width / 2, this.getBackgroundYStart() + 50, FastColor.ARGB32.color(255, 255, 255, 255));
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.dailylistfeefull", ClientProxy.dailyListFee), width / 2, this.getBackgroundYStart() + 70, FastColor.ARGB32.color(255, 255, 255, 255));
        drawCenteredString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.dailychunkfee", ClientProxy.dailyChunkFee), width / 2, this.getBackgroundYStart() + 90, FastColor.ARGB32.color(255, 255, 255, 255));
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

    @Override
    public void onClose() {
        this.minecraft.setScreen(new MainPage());
    }

    private int getBackgroundXStart() {
        return (width - 194) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 136) / 2;
    }
}
