package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.MonetaMoney;
import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Function;

public class SOpenTeamsPage implements IPacket {
    public Team team;
    public Type type;

    public SOpenTeamsPage() {
    }

    public SOpenTeamsPage(Team team, Type type) {
        this.team = team;
        this.type = type;
    }

    public static Function<FriendlyByteBuf, SOpenTeamsPage> decoder() {
        return pb -> {
            SOpenTeamsPage packet = new SOpenTeamsPage();
            packet.readPacketData(pb);
            return packet;
        };
    }

    @Override
    public void readPacketData(FriendlyByteBuf buf) {
        this.team = Team.readTeam(buf);
        this.type = buf.readEnum(Type.class);
    }

    @Override
    public void writePacketData(FriendlyByteBuf buf) {
        this.team.writeTeam(buf);
        buf.writeEnum(this.type);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        MonetaMoney.PROXY.handleSOpenTeamsPage(ctx, this);
    }

    public enum Type {
        INDIFFERENT,
        INSAME,
        INNONE
    }
}
