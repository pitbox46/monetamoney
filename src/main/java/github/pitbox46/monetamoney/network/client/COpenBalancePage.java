package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.data.Ledger;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SOpenBalancePage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.function.Function;

public class COpenBalancePage implements IPacket {
    public BlockPos pos;

    public COpenBalancePage() {
    }

    public COpenBalancePage(BlockPos pos) {
        this.pos = pos;
    }

    public static Function<FriendlyByteBuf, COpenBalancePage> decoder() {
        return pb -> {
            COpenBalancePage packet = new COpenBalancePage();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if (ctx.getSender() != null && ctx.getSender().getCommandSenderWorld().getBlockState(this.pos).getBlock().getClass() == Vault.class) {
            Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getLevel().dimension().location().toString() + this.pos.asLong());
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenBalancePage(Ledger.readBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName()), team.balance));
        }
    }
}
