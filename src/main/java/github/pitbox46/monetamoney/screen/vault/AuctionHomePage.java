package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.containers.vault.AuctionHomeContainer;
import github.pitbox46.monetamoney.network.ClientProxy;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.client.COpenBuyPage;
import github.pitbox46.monetamoney.network.client.CPageChange;
import github.pitbox46.monetamoney.network.client.CUpdateBalance;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AuctionHomePage extends AbstractContainerScreen<AuctionHomeContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/auction.png");

    protected boolean editMode = false;

    public AuctionHomePage(AuctionHomeContainer screenContainer, Inventory inv) {
        super(screenContainer, inv, new TranslatableComponent("screen.monetamoney.auctionhome"));
        this.imageWidth = 222;
        this.imageHeight = 256;
    }

    @Override
    protected void init() {
        super.init();
        //Left arrow
        this.addRenderableWidget(new ImageButton(this.getBackgroundXStart() + 10, this.getBackgroundYStart() + 127, 15, 15, 100, 256, 15, TEXTURE, 256, 302, button -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) (this.editMode ? 6 : 5), Math.max(menu.pageNumber - 1, 0)));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }));
        //Right arrow
        this.addRenderableWidget(new ImageButton(this.getBackgroundXStart() + 197, this.getBackgroundYStart() + 127, 15, 15, 115, 256, 15, TEXTURE, 256, 302, button -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) (this.editMode ? 6 : 5), Math.max(menu.pageNumber + 1, 0)));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }));
        this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 9, this.getBackgroundYStart() + 147, 100, 23, 0, 256, 23, TEXTURE, 256, 302, button -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 7, (short) 0));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.listitem")));
        this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 116, this.getBackgroundYStart() + 147, 100, 23, 0, 256, 23, TEXTURE, 256, 302, button -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 6, (short) 0));
            toggleButton(button);
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslatableComponent("button.monetamoney.editlistings")));
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        drawString(matrixStack, this.font, new TranslatableComponent("info.monetamoney.personalbal", ClientProxy.personalBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 5, FastColor.ARGB32.color(255, 255, 255, 255));
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
        this.renderBackground(matrixStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(matrixStack, this.getBackgroundXStart(), this.getBackgroundYStart(), 0, 0, 222, 256, 256, 302);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) {
    }

    @Override
    protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (slotIn != null && type == ClickType.PICKUP && slotIn.index > 35) {
            if (!slotIn.getItem().isEmpty()) {
                PacketHandler.CHANNEL.sendToServer(new COpenBuyPage(slotIn.getItem().save(new CompoundTag()), COpenBuyPage.Type.AUCTION));
            }
        } else if (slotIn != null && slotIn.index > 35) {

        } else {
            super.slotClicked(slotIn, slotId, mouseButton, type);
        }
    }

    @Override
    public List<Component> getTooltipFromItem(ItemStack itemStack) {
        List<Component> lines = itemStack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        if (itemStack.getTag() != null && itemStack.getTag().getInt("price") != 0) {
            lines.add(1, new TextComponent(itemStack.getTag().getInt("price") + " Coins").withStyle(ChatFormatting.YELLOW));
            lines.add(2, new TextComponent("Owner: " + itemStack.getTag().getString("owner")));
        }
        return lines;
    }

    @Override
    public void onClose() {
        this.minecraft.player.closeContainer();
        this.minecraft.setScreen(new MainPage());
    }

    private void toggleButton(Button pressedButton) {
        if (this.editMode) {
            this.removeWidget(pressedButton);
            this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 116, this.getBackgroundYStart() + 147, 100, 23, 0, 256, 23, TEXTURE, 256, 302, button -> {
                PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 6, (short) 0));
                toggleButton(button);
            }, new TranslatableComponent("button.monetamoney.editlistings")));
        } else {
            this.removeWidget(pressedButton);
            this.addRenderableWidget(new ImageTextButton(this.getBackgroundXStart() + 116, this.getBackgroundYStart() + 147, 100, 23, 0, 256, 23, TEXTURE, 256, 302, button -> {
                PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 5, (short) 0));
                toggleButton(button);
            }, new TranslatableComponent("button.monetamoney.alllistings")));
        }
        this.editMode = !this.editMode;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getBackgroundXStart() {
        return (width - 222) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 256) / 2;
    }
}
