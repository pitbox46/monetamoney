package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SOpenAnchorPage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.ArrayList;
import java.util.function.Function;

public class CAnchorButton implements IPacket {
    public BlockPos pos;
    public boolean enable;

    public CAnchorButton() {
    }

    public CAnchorButton(BlockPos pos, boolean enable) {
        this.pos = pos;
        this.enable = enable;
    }

    public static Function<FriendlyByteBuf, CAnchorButton> decoder() {
        return pb -> {
            CAnchorButton packet = new CAnchorButton();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.enable = buf.readBoolean();
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.enable);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        Team team = Teams.getPlayersTeam(Teams.jsonFile, ctx.getSender().getGameProfile().getName());
        if (ctx.getSender() != null && !team.isNull()) {
            if (this.enable) {
                ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                boolean flag = false;
                for (ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(team.toString())) {
                    if (ctx.getSender().getCommandSenderWorld().dimension().location().equals(chunkLoader.dimensionKey) && chunkLoader.pos.equals(this.pos)) {
                        chunkLoader.status = ChunkLoader.Status.ON;
                        flag = true;
                        ServerEvents.loadNewChunk(ctx.getSender().getLevel(), team, chunkLoader);
                        break;
                    }
                }
                if (!flag) {
                    ChunkLoader chunkLoader = new ChunkLoader(ctx.getSender().getLevel().dimension().location(), this.pos, ctx.getSender().getGameProfile().getName(), ChunkLoader.Status.ON);
                    ServerEvents.loadNewChunk(ctx.getSender().getLevel(), team, chunkLoader);
                    ServerEvents.CHUNK_MAP.get(team.toString()).add(chunkLoader);
                }
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenAnchorPage(true));
            } else {
                ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                boolean flag = false;
                for (ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(team.toString())) {
                    if (ctx.getSender().getCommandSenderWorld().dimension().location().equals(chunkLoader.dimensionKey) && chunkLoader.pos.equals(this.pos)) {
                        chunkLoader.status = ChunkLoader.Status.OFF;
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    ServerEvents.CHUNK_MAP.get(team.toString()).add(new ChunkLoader(ctx.getSender().getLevel().dimension().location(), this.pos, ctx.getSender().getGameProfile().getName(), ChunkLoader.Status.OFF));
                }
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenAnchorPage(false));
            }
        }
    }
}
