package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.data.*;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SUpdateBalance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Function;

public class CUpdateBalance implements IPacket {
    public BlockPos pos;

    public CUpdateBalance() {
    }

    public CUpdateBalance(BlockPos pos) {
        this.pos = pos;
    }

    public static Function<FriendlyByteBuf, CUpdateBalance> decoder() {
        return pb -> {
            CUpdateBalance packet = new CUpdateBalance();
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
            int chunks = ServerEvents.CHUNK_MAP.containsKey(team.toString()) ? (int) ServerEvents.CHUNK_MAP.get(team.toString()).stream().filter(c -> c.status == ChunkLoader.Status.ON || c.status == ChunkLoader.Status.STUCK).count() : 0;
            long dailyChunkFee = ServerEvents.calculateChunksCost(chunks) * chunks;

            ListTag auctionList = (ListTag) Auctioned.auctionedNBT.get("auction");
            assert auctionList != null;

            int listings = (int) auctionList.stream().filter(inbt -> ((CompoundTag) inbt).getString("owner").equals(ctx.getSender().getGameProfile().getName())).count();
            long dailyListingFee = ServerEvents.calculateDailyListCost(listings) * listings;

            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SUpdateBalance(Ledger.readBalance(Ledger.jsonFile, ctx.getSender().getGameProfile().getName()), team.balance, dailyChunkFee, dailyListingFee));
        }
    }
}
