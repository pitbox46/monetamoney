package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.containers.vault.AccountTransactionContainer;
import github.pitbox46.monetamoney.data.Outstanding;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.items.Coin;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SGuiStatusMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Function;

public class CTeamTransactionButton implements IPacket {
    public BlockPos pos;
    public int amount;
    public Button button;

    public CTeamTransactionButton() {}

    public CTeamTransactionButton(BlockPos pos, int amount, Button button) {
        this.pos = pos;
        this.amount = amount;
        this.button = button;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.amount = buf.readInt();
        this.button = buf.readEnumValue(Button.class);
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.amount);
        buf.writeEnumValue(this.button);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(ctx.getSender() != null) {
            if (ctx.getSender().openContainer instanceof AccountTransactionContainer) {
                AccountTransactionContainer container = (AccountTransactionContainer) ctx.getSender().openContainer;
                Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getServerWorld().getDimensionKey().getLocation().toString() + this.pos.toLong());
                if(team.isNull()) {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.noteam")));
                    return;
                }
                if(team.members.contains(ctx.getSender().getGameProfile().getName())) {
                    if(this.button == CTeamTransactionButton.Button.WITHDRAW) {
                        if (this.amount > Coin.MAX_SIZE || this.amount <= 0) {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invalidsize")));
                        } else if (team.balance >= this.amount) {
                            team.balance -= this.amount;
                            ItemStack coins = Coin.createCoin(this.amount, Outstanding.newCoin(Outstanding.jsonFile, this.amount, ctx.getSender().getGameProfile().getName()));
                            if (container.handler.getStackInSlot(0).isEmpty()) {
                                container.handler.setStackInSlot(0, coins);
                            } else {
                                ctx.getSender().inventory.placeItemBackInInventory(ctx.getSender().getEntityWorld(), coins);
                            }
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.nomoney")));
                        }
                    }
                    else if (this.button == CTeamTransactionButton.Button.DEPOSIT) {
                        if (container.handler.getStackInSlot(0).getItem() instanceof Coin) {
                            ItemStack coins = container.handler.getStackInSlot(0);
                            if (!coins.getOrCreateTag().hasUniqueId("uuid") && Outstanding.redeemTeamCoin(Outstanding.jsonFile, team, coins.getOrCreateTag().getUniqueId("uuid"))) {
                                container.handler.setStackInSlot(0, ItemStack.EMPTY);
                            } else {
                                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invalidcoin")));
                            }
                        } else {
                            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.invaliditem")));
                        }
                    }
                    Teams.updateTeam(Teams.jsonFile, team);
                } else {
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SGuiStatusMessage(new TranslationTextComponent("message.monetamoney.noaccess")));
                }
            }
        }
    }

    public static Function<PacketBuffer, CTeamTransactionButton> decoder() {
        return pb -> {
            CTeamTransactionButton packet = new CTeamTransactionButton();
            packet.readPacketData(pb);
            return packet;
        };
    }

    public enum Button {
        DEPOSIT,
        WITHDRAW
    }
}
