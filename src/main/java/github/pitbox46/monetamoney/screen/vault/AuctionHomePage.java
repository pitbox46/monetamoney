package github.pitbox46.monetamoney.screen.vault;

import com.mojang.blaze3d.matrix.MatrixStack;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.containers.vault.AuctionHomeContainer;
import github.pitbox46.monetamoney.network.*;
import github.pitbox46.monetamoney.network.client.COpenBuyPage;
import github.pitbox46.monetamoney.network.client.CPageChange;
import github.pitbox46.monetamoney.network.client.CUpdateBalance;
import github.pitbox46.monetamoney.screen.ImageTextButton;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class AuctionHomePage extends ContainerScreen<AuctionHomeContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("monetamoney:textures/gui/auction.png");

    protected boolean editMode = false;

    public AuctionHomePage(AuctionHomeContainer screenContainer, PlayerInventory inv) {
        super(screenContainer, inv, new TranslationTextComponent("screen.monetamoney.auctionhome"));
        this.xSize = 222;
        this.ySize = 256;
    }

    @Override
    protected void init() {
        super.init();
        //Left arrow
        this.addButton(new ImageButton(this.getBackgroundXStart() + 10, this.getBackgroundYStart() + 127, 15, 15, 100, 256, 15, TEXTURE, 256, 302, button -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) (this.editMode ? 6 : 5), Math.max(container.pageNumber - 1, 0)));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }));
        //Right arrow
        this.addButton(new ImageButton(this.getBackgroundXStart() + 197, this.getBackgroundYStart() + 127, 15, 15, 115, 256, 15, TEXTURE, 256, 302, button -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) (this.editMode ? 6 : 5), Math.max(container.pageNumber + 1, 0)));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }));
        this.addButton(new ImageTextButton(this.getBackgroundXStart() + 9, this.getBackgroundYStart() + 147, 100, 23, 0, 256, 23, TEXTURE, 256, 302, button -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 7, (short) 0));
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslationTextComponent("button.monetamoney.listitem")));
        this.addButton(new ImageTextButton(this.getBackgroundXStart() + 116, this.getBackgroundYStart() + 147, 100, 23, 0, 256, 23, TEXTURE, 256, 302, button -> {
            PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 6, (short) 0));
            toggleButton(button);
            PacketHandler.CHANNEL.sendToServer(new CUpdateBalance(Vault.lastOpenedVault));
        }, new TranslationTextComponent("button.monetamoney.editlistings")));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        drawString(matrixStack, this.font, new TranslationTextComponent("info.monetamoney.personalbal", ClientProxy.personalBalance), this.getBackgroundXStart() + 5, this.getBackgroundYStart() + 5, ColorHelper.PackedColor.packColor(255, 255, 255, 255));
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        this.minecraft.textureManager.bindTexture(TEXTURE);
        this.blit(matrixStack, this.getBackgroundXStart(), this.getBackgroundYStart(), 0, 0, 222, 256, 256, 302);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if(slotIn != null && type == ClickType.PICKUP && slotIn.slotNumber > 35) {
            if(!slotIn.getStack().isEmpty()) {
                PacketHandler.CHANNEL.sendToServer(new COpenBuyPage(slotIn.getStack().write(new CompoundNBT())));
            }
        } else if(slotIn != null && slotIn.slotNumber > 35) {

        } else {
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
        }
    }

    @Override
    public List<ITextComponent> getTooltipFromItem(ItemStack itemStack) {
        List<ITextComponent> lines = itemStack.getTooltip(this.minecraft.player, this.minecraft.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
        if(itemStack.getTag() != null && itemStack.getTag().getInt("price") != 0) {
            lines.add(1, new StringTextComponent(itemStack.getTag().getInt("price") + " Coins").mergeStyle(TextFormatting.YELLOW));
            lines.add(2, new StringTextComponent("Owner: " + itemStack.getTag().getString("owner")));
        }
        return lines;
    }

    @Override
    public void closeScreen() {
        this.minecraft.player.closeScreen();
        this.minecraft.displayGuiScreen(new MainPage());
    }

    private void toggleButton(Button pressedButton) {
        if(this.editMode) {
            this.buttons.remove(pressedButton);
            this.children.remove(pressedButton);
            this.addButton(new ImageTextButton(this.getBackgroundXStart() + 116, this.getBackgroundYStart() + 147, 100, 23, 0, 256, 23, TEXTURE, 256, 302, button -> {
                PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 6, (short) 0));
                toggleButton(button);
            }, new TranslationTextComponent("button.monetamoney.editlistings")));
        } else {
            this.buttons.remove(pressedButton);
            this.children.remove(pressedButton);
            this.addButton(new ImageTextButton(this.getBackgroundXStart() + 116, this.getBackgroundYStart() + 147, 100, 23, 0, 256, 23, TEXTURE, 256, 302, button -> {
                PacketHandler.CHANNEL.sendToServer(new CPageChange((short) 5, (short) 0));
                toggleButton(button);
            }, new TranslationTextComponent("button.monetamoney.alllistings")));
        }
        this.editMode = !this.editMode;
    }

    private int getBackgroundXStart() {
        return (width - 222) / 2;
    }

    private int getBackgroundYStart() {
        return (height - 256) / 2;
    }
}
