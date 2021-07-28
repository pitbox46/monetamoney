package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.screen.IStatusable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.*;

import java.util.List;

public class ChunksPage extends Screen implements IStatusable {
    private final static ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/chunks.png");

    protected static final int STATUS_TIMER = 100;

    protected ITextComponent status;
    protected long statusStart;
    protected Team team;
    protected List<ChunkLoader> chunkLoaders;
    protected ChunksList list;

    public ChunksPage(Team team, List<ChunkLoader> chunkLoaders) {
        super(new TranslationTextComponent("screen.monetamoney.chunks"));
        this.team = team;
        this.chunkLoaders = chunkLoaders;
    }

    @Override
    protected void init() {
        super.init();
        this.list = new ChunksList(minecraft, chunkLoaders, 152, 72, this.getBackgroundXStart() + 17, this.getBackgroundYStart() + 26, 14);
        this.children.add(this.list);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        if(this.list != null) {
            this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        this.minecraft.textureManager.bindTexture(TEXTURE);
        RenderSystem.enableBlend();
        blit(matrixStack, this.getBackgroundXStart(), this.getBackgroundYStart(), 0, 0, 186, 136);

        /* Team Balance */
        drawString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.teambal", ClientProxy.teamBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 5, ColorHelper.PackedColor.packColor(255, 255, 255, 255));

        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderStatus(matrixStack);
    }

    @Override
    public void renderBackground(MatrixStack matrixStack, int vOffset) {
        super.renderBackground(matrixStack, vOffset);
        this.minecraft.textureManager.bindTexture(TEXTURE);
        blit(matrixStack, this.getBackgroundXStart() + 17, this.getBackgroundYStart() + 27, 0, 136, 152, 72);
    }

    protected void renderStatus(MatrixStack matrixStack) {
        if(status != null && this.minecraft.world.getGameTime() - this.statusStart < STATUS_TIMER - 2) {
            int alpha = (int) (255 - 255 * (this.minecraft.world.getGameTime() - this.statusStart) / STATUS_TIMER);
            drawCenteredString(matrixStack, this.font, this.status, width / 2, getBackgroundYStart() + 100, ColorHelper.PackedColor.packColor(alpha, 255, 255, 255));
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
    public boolean isPauseScreen() {
        return false;
    }

    class ChunksList extends ExtendedList<ChunksList.ChunkEntry> {
        protected final List<ChunkLoader> chunkLoaders;

        public ChunksList(Minecraft mcIn, List<ChunkLoader> chunkLoaders, int widthIn, int heightIn, int leftIn, int topIn, int slotHeightIn) {
            super(mcIn, widthIn, heightIn, topIn, topIn + heightIn, slotHeightIn);
            this.chunkLoaders = chunkLoaders;
            this.x0 = leftIn;
            this.x1 = leftIn + widthIn;
            for(ChunkLoader chunkLoader: chunkLoaders) {
                this.addEntry(new ChunkEntry(chunkLoader));
            }
        }

        @Override
        public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            this.renderList(matrixStack, this.getRowLeft(), this.y0 + 4 - (int)this.getScrollAmount(), mouseX, mouseY, partialTicks);
        }

        @Override
        public int getRowWidth() {
            return 152;
        }

        class ChunkEntry extends ExtendedList.AbstractListEntry<ChunkEntry> {
            public final ChunkLoader chunkLoader;

            public ChunkEntry(ChunkLoader chunkLoader) {
                this.chunkLoader = chunkLoader;
            }

            @Override
            public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                drawString(matrixStack, ChunksPage.this.font, chunkLoader.owner, left + 5, top + 2, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
                minecraft.getTextureManager().bindTexture(TEXTURE);
                if(chunkLoader.status == ChunkLoader.Status.ON) {
                    blit(matrixStack, left + 137, top + 2, 152, 157, 7, 7);
                } else if (chunkLoader.status == ChunkLoader.Status.OFF) {
                    blit(matrixStack, left + 137, top + 2, 152, 143, 7, 7);
                } else {
                    blit(matrixStack, left + 137, top + 2, 152, 150, 7, 7);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if(button == 0) {
                    IFormattableTextComponent message = new StringTextComponent("");
                    message.appendString(chunkLoader.owner + " | " + chunkLoader.dimensionKey + " | " + chunkLoader.pos.toString() + " | " + chunkLoader.status.name);
                    minecraft.player.sendStatusMessage(message.mergeStyle(TextFormatting.LIGHT_PURPLE), false);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    @Override
    public void closeScreen() {
        this.minecraft.displayGuiScreen(new MainPage());
    }

    private int getBackgroundXStart() {
        return (width - 186) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 136) / 2;
    }
}
