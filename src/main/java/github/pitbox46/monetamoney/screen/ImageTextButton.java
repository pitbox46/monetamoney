package github.pitbox46.monetamoney.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ImageTextButton extends ImageButton {
    protected final ITextComponent text;

    public ImageTextButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, IPressable onPressIn, ITextComponent text) {
        this(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, 256, 256, onPressIn, text);
    }

    public ImageTextButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int textureWidth, int textureHeight, IPressable onPressIn, ITextComponent text) {
        this(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, textureWidth, textureHeight, onPressIn, StringTextComponent.EMPTY, text);
    }

    public ImageTextButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, int textureWidth, int textureHeight, IPressable onPress, ITextComponent title, ITextComponent text) {
        this(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, textureWidth, textureHeight, onPress, EMPTY_TOOLTIP, title, text);
    }

    public ImageTextButton(int p_i244513_1_, int p_i244513_2_, int p_i244513_3_, int p_i244513_4_, int p_i244513_5_, int p_i244513_6_, int p_i244513_7_, ResourceLocation p_i244513_8_, int p_i244513_9_, int p_i244513_10_, IPressable p_i244513_11_, ITooltip p_i244513_12_, ITextComponent p_i244513_13_, ITextComponent text) {
        super(p_i244513_1_, p_i244513_2_, p_i244513_3_, p_i244513_4_, p_i244513_5_, p_i244513_6_, p_i244513_7_, p_i244513_8_, p_i244513_9_, p_i244513_10_, p_i244513_11_, p_i244513_12_, p_i244513_13_);
        this.text = text;
    }

    @Override
    public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, Minecraft.getInstance().fontRenderer, text, (x + width / 2) - 2, y + 6, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
    }
}
