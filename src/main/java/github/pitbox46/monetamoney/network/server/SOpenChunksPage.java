package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.data.ChunkLoader;
import github.pitbox46.monetamoney.data.Team;

import java.util.List;

public class SOpenChunksPage {
    public final Team team;
    public final List<ChunkLoader> chunks;

    public SOpenChunksPage(Team team, List<ChunkLoader> chunks) {
        this.team = team;
        this.chunks = chunks;
    }
}
