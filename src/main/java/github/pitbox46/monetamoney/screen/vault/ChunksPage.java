package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.screen.IStatusable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;

import java.util.List;

public class ChunksPage extends Screen implements IStatusable {
    protected static final int STATUS_TIMER = 100;
    private final static ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/chunks.png");
    protected Component status;
    protected long statusStart;
    protected Team team;
    protected List<ChunkLoader> chunkLoaders;
    protected ChunksList list;

    public ChunksPage(Team team, List<ChunkLoader> chunkLoaders) {
        super(new TranslatableComponent("screen.monetamoney.chunks"));
        this.team = team;
        this.chunkLoaders = chunkLoaders;
    }

    @Override
    protected void init() {
        super.init();
        this.list = new ChunksList(minecraft, chunkLoaders, 152, 72, this.getBackgroundXStart() + 17, this.getBackgroundYStart() + 26, 14);
        this.addRenderableWidget(this.list);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        if (this.list != null) {
            this.list.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        blit(matrixStack, this.getBackgroundXStart(), this.getBackgroundYStart(), 0, 0, 186, 136);

        /* Team Balance */
        drawString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.teambal", ClientProxy.teamBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 5, FastColor.ARGB32.color(255, 255, 255, 255));

        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderStatus(matrixStack);
    }

    @Override
    public void renderBackground(PoseStack matrixStack, int vOffset) {
        super.renderBackground(matrixStack, vOffset);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(matrixStack, this.getBackgroundXStart() + 17, this.getBackgroundYStart() + 27, 0, 136, 152, 72);
    }

    protected void renderStatus(PoseStack matrixStack) {
        if (status != null && this.minecraft.level.getGameTime() - this.statusStart < STATUS_TIMER - 2) {
            int alpha = (int) (255 - 255 * (this.minecraft.level.getGameTime() - this.statusStart) / STATUS_TIMER);
            drawCenteredString(matrixStack, this.font, this.status, width / 2, getBackgroundYStart() + 100, FastColor.ARGB32.color(alpha, 255, 255, 255));
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

    @Override
    public void onClose() {
        this.minecraft.setScreen(new MainPage());
    }

    private int getBackgroundXStart() {
        return (width - 186) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 136) / 2;
    }

    class ChunksList extends ObjectSelectionList<ChunksList.ChunkEntry> {
        protected final List<ChunkLoader> chunkLoaders;

        public ChunksList(Minecraft mcIn, List<ChunkLoader> chunkLoaders, int widthIn, int heightIn, int leftIn, int topIn, int slotHeightIn) {
            super(mcIn, widthIn, heightIn, topIn, topIn + heightIn, slotHeightIn);
            this.chunkLoaders = chunkLoaders;
            this.x0 = leftIn;
            this.x1 = leftIn + widthIn;
            for (ChunkLoader chunkLoader : chunkLoaders) {
                this.addEntry(new ChunkEntry(chunkLoader));
            }
        }

        @Override
        public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            this.renderList(matrixStack, this.getRowLeft(), this.y0 + 4 - (int) this.getScrollAmount(), mouseX, mouseY, partialTicks);
        }

        @Override
        public int getRowWidth() {
            return 152;
        }

        class ChunkEntry extends ObjectSelectionList.Entry<ChunkEntry> {
            public final ChunkLoader chunkLoader;

            public ChunkEntry(ChunkLoader chunkLoader) {
                this.chunkLoader = chunkLoader;
            }

            @Override
            public void render(PoseStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                drawString(matrixStack, ChunksPage.this.font, chunkLoader.owner, left + 5, top + 2, FastColor.ARGB32.color(255, 255, 255, 255));
                minecraft.getTextureManager().bindForSetup(TEXTURE);
                if (chunkLoader.status == ChunkLoader.Status.ON) {
                    blit(matrixStack, left + 137, top + 2, 152, 157, 7, 7);
                } else if (chunkLoader.status == ChunkLoader.Status.OFF) {
                    blit(matrixStack, left + 137, top + 2, 152, 143, 7, 7);
                } else {
                    blit(matrixStack, left + 137, top + 2, 152, 150, 7, 7);
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    MutableComponent message = new TextComponent("");
                    message.append(chunkLoader.owner + " | " + chunkLoader.dimensionKey + " | " + new ChunkPos(chunkLoader.pos) + " | " + chunkLoader.pos.toString() + " | " + chunkLoader.status.name);
                    minecraft.player.displayClientMessage(message.withStyle(ChatFormatting.LIGHT_PURPLE), false);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public Component getNarration() {
                return null;
            }
        }
    }
}
