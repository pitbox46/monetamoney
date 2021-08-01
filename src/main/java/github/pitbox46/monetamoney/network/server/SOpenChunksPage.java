package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.screen.vault.ChunksPage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SOpenChunksPage implements IPacket {
    public Team team;
    public List<ChunkLoader> chunks;

    public SOpenChunksPage() {}

    public SOpenChunksPage(Team team, List<ChunkLoader> chunks) {
        this.team = team;
        this.chunks = chunks;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.team = Team.readTeam(buf);
        List<ChunkLoader> chunks = new ArrayList<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++) {
            chunks.add(ChunkLoader.readChunk(buf));
        }
        this.chunks = chunks;
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        this.team.writeTeam(buf);
        buf.writeInt(this.chunks.size());
        for(ChunkLoader chunkLoader: this.chunks) {
            chunkLoader.writeChunk(buf);
        }
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSOpenChunksPage(ctx, this);
    }

    public static Function<PacketBuffer,SOpenChunksPage> decoder() {
        return pb -> {
            SOpenChunksPage packet = new SOpenChunksPage();
            packet.readPacketData(pb);
            return packet;
        };
    }
}
