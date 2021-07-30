package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.blocks.VaultTile;
import github.pitbox46.monetamoney.data.Ledger;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SOpenBalancePage;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Function;

public class COpenBalancePage implements IPacket {
    public BlockPos pos;

    public COpenBalancePage() {}

    public COpenBalancePage(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(ctx.getSender() != null && ctx.getSender().getEntityWorld().getTileEntity(this.pos) instanceof VaultTile) {
            Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getServerWorld().getDimensionKey().getLocation().toString() + this.pos.toLong());
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenBalancePage(Ledger.readBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName()), team.balance));
        }
    }

    public static Function<PacketBuffer, COpenBalancePage> decoder() {
        return pb -> {
            COpenBalancePage packet = new COpenBalancePage();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
