package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SOpenAnchorPage;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.function.Function;

public class CAnchorButton implements IPacket {
    public BlockPos pos;
    public boolean enable;

    public CAnchorButton() {}

    public CAnchorButton(BlockPos pos, boolean enable) {
        this.pos = pos;
        this.enable = enable;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.enable = buf.readBoolean();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeBlockPos(this.pos);
        buf.writeBoolean(this.enable);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        Team team = Teams.getPlayersTeam(Teams.jsonFile, ctx.getSender().getGameProfile().getName());
        if(ctx.getSender() != null && !team.isNull()) {
            if(this.enable) {
                ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                boolean flag = false;
                for(ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(team.toString())) {
                    if(ctx.getSender().getEntityWorld().getDimensionKey().getLocation().equals(chunkLoader.dimensionKey) && chunkLoader.pos.equals(this.pos)) {
                        chunkLoader.status = ChunkLoader.Status.ON;
                        flag = true;
                        ServerEvents.loadNewChunk(ctx.getSender().getServerWorld(), team, chunkLoader);
                        break;
                    }
                }
                if(!flag) {
                    ChunkLoader chunkLoader = new ChunkLoader(ctx.getSender().getServerWorld().getDimensionKey().getLocation(), this.pos, ctx.getSender().getGameProfile().getName(), ChunkLoader.Status.ON);
                    ServerEvents.loadNewChunk(ctx.getSender().getServerWorld(), team, chunkLoader);
                    ServerEvents.CHUNK_MAP.get(team.toString()).add(chunkLoader);
                }
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenAnchorPage(true));
            } else {
                ServerEvents.CHUNK_MAP.putIfAbsent(team.toString(), new ArrayList<>());
                boolean flag = false;
                for(ChunkLoader chunkLoader : ServerEvents.CHUNK_MAP.get(team.toString())) {
                    if(ctx.getSender().getEntityWorld().getDimensionKey().getLocation().equals(chunkLoader.dimensionKey) && chunkLoader.pos.equals(this.pos)) {
                        chunkLoader.status = ChunkLoader.Status.OFF;
                        flag = true;
                        break;
                    }
                }
                if(!flag) {
                    ServerEvents.CHUNK_MAP.get(team.toString()).add(new ChunkLoader(ctx.getSender().getServerWorld().getDimensionKey().getLocation(), this.pos, ctx.getSender().getGameProfile().getName(), ChunkLoader.Status.OFF));
                }
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenAnchorPage(false));
            }
        }
    }

    public static Function<PacketBuffer, CAnchorButton> decoder() {
        return pb -> {
            CAnchorButton packet = new CAnchorButton();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
