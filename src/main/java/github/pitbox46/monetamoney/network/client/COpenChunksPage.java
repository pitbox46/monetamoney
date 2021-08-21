package github.pitbox46.monetamoney.network.client;

import github.pitbox46.monetamoney.ServerEvents;
import github.pitbox46.monetamoney.blocks.Anchor;
import github.pitbox46.monetamoney.blocks.Vault;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.data.Teams;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.network.PacketHandler;
import github.pitbox46.monetamoney.network.server.SOpenChunksPage;
import github.pitbox46.monetamoney.network.server.SSyncFeesPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class COpenChunksPage implements IPacket {
    public BlockPos pos;

    public COpenChunksPage() {}

    public COpenChunksPage(BlockPos pos) {
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
        if(ctx.getSender() != null && ctx.getSender().getEntityWorld().getBlockState(this.pos).getBlock().getClass() == Vault.class) {
            Team team = Teams.getTeam(Teams.jsonFile, ctx.getSender().getServerWorld().getDimensionKey().getLocation().toString() + this.pos.toLong());
            List<ChunkLoader> chunks = ServerEvents.CHUNK_MAP.get(team.toString());
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(ctx::getSender), new SOpenChunksPage(team, chunks == null ? new ArrayList<>(0) : chunks));
        }
    }

    public static Function<PacketBuffer, COpenChunksPage> decoder() {
        return pb -> {
            COpenChunksPage packet = new COpenChunksPage();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
