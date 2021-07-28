package github.pitbox46.monetamoney.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import github.pitbox46.monetamoney.blocks.Anchor;
import github.pitbox46.monetamoney.network.CAnchorButton;
import github.pitbox46.monetamoney.network.PacketHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class AnchorScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/main.png");

    protected boolean active;

    public AnchorScreen(boolean active) {
        super(new TranslationTextComponent("screen.monetamoney.anchor"));
        this.active = active;
    }

    @Override
    public void init() {
        super.init();
        if(active) {
            this.addButton(new ImageTextButton(this.getBackgroundXStart() + 19, this.getBackgroundYStart() + 20, 154, 23, 0, 136, 23, TEXTURE, (button) -> {
                PacketHandler.CHANNEL.sendToServer(new CAnchorButton(Anchor.lastOpenedAnchor, false));
            }, new TranslationTextComponent("button.monetamoney.disable")));
        } else {
            this.addButton(new ImageTextButton(this.getBackgroundXStart() + 19, this.getBackgroundYStart() + 20, 154, 23, 0, 136, 23, TEXTURE, (button) -> {
                PacketHandler.CHANNEL.sendToServer(new CAnchorButton(Anchor.lastOpenedAnchor, true));
            }, new TranslationTextComponent("button.monetamoney.enable")));
        }
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
