package github.pitbox46.monetamoney.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.pitbox46.monetamoney.blocks.Anchor;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.client.CAnchorButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class AnchorScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/main.png");

    protected boolean active;

    public AnchorScreen(boolean active) {
        super(new TranslatableComponent("screen.monetamoney.anchor"));
        this.active = active;
    }

    @Override
    public void init() {
        super.init();
        if (active) {
            this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 19, this.getBackgroundYStart() + 20, 154, 23, 0, 136, 23, TEXTURE, (button) -> {
                PacketHandler.CHANNEL.sendToServer(new CAnchorButton(Anchor.lastOpenedAnchor, false));
            }, new TranslatableComponent("button.monetamoney.disable")));
        } else {
            this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 19, this.getBackgroundYStart() + 20, 154, 23, 0, 136, 23, TEXTURE, (button) -> {
                PacketHandler.CHANNEL.sendToServer(new CAnchorButton(Anchor.lastOpenedAnchor, true));
            }, new TranslatableComponent("button.monetamoney.enable")));
        }
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
