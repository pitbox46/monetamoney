package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.blocks.VaultTile;
import github.pitbox46.monetamoney.data.*;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import github.pitbox46.monetamoney.network.server.SUpdateBalance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Function;

public class CUpdateBalance implements IPacket {
    public BlockPos pos;

    public CUpdateBalance() {}

    public CUpdateBalance(BlockPos pos) {
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
            int chunks = ServerEvents.CHUNK_MAP.containsKey(team.toString()) ? (int) ServerEvents.CHUNK_MAP.get(team.toString()).stream().filter(c -> c.status == ChunkLoader.Status.ON || c.status == ChunkLoader.Status.STUCK).count() : 0;
            long dailyChunkFee = ServerEvents.calculateChunksCost(chunks) * chunks;

            ListNBT auctionList = (ListNBT) Auctioned.auctionedNBT.get("auction");
            assert auctionList != null;

            int listings = (int) auctionList.stream().filter(inbt -> ((CompoundNBT) inbt).getString("owner").equals(ctx.getSender().getGameProfile().getName())).count();
            long dailyListingFee = ServerEvents.calculateDailyListCost(listings) * listings;

            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SUpdateBalance(Ledger.readBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName()), team.balance, dailyChunkFee, dailyListingFee));
        }
    }

    public static Function<PacketBuffer, CUpdateBalance> decoder() {
        return pb -> {
            CUpdateBalance packet = new CUpdateBalance();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
