package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.matrix.MatrixStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.network.*;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class BalancePage extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/main.png");

    public BalancePage() {
        super(new TranslationTextComponent("screen.monetamoney.balance"));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, 0);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.personalbal", ClientProxy.personalBalance), width / 2, this.getBackgroundYStart() + 30, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
        drawCenteredString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.teambal", ClientProxy.teamBalance), width / 2, this.getBackgroundYStart() + 50, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
        drawCenteredString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.dailylistfeefull", ClientProxy.dailyListFee), width / 2, this.getBackgroundYStart() + 70, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
        drawCenteredString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.dailychunkfee", ClientProxy.dailyChunkFee), width / 2, this.getBackgroundYStart() + 90, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
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

    @Override
    public void closeScreen() {
        this.minecraft.displayGuiScreen(new MainPage());
    }

    private int getBackgroundXStart() {
        return (width - 194) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 136) / 2;
    }
}
