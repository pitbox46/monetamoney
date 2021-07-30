package github.pitbox46.monetamoney.network.server;

import github.pitbox46.monetamoney.data.Team;
import github.pitbox46.monetamoney.network.IPacket;
import github.pitbox46.monetamoney.screen.vault.TeamsPage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;

public class SOpenTeamsPage implements IPacket {
    public Team team;
    public Type type;

    public SOpenTeamsPage() {}

    public SOpenTeamsPage(Team team, Type type) {
        this.team = team;
        this.type = type;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        this.team = Team.readTeam(buf);
        this.type = buf.readEnumValue(Type.class);
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        this.team.writeTeam(buf);
        buf.writeEnumValue(this.type);
    }

    @Override
    public void processPacket(NetworkEvent.Context ctx) {
        if(Minecraft.getInstance().world != null) {
            Minecraft.getInstance().displayGuiScreen(new TeamsPage(this.team, this.type));
        }
    }

    public static Function<PacketBuffer,SOpenTeamsPage> decoder() {
        return pb -> {
            SOpenTeamsPage packet = new SOpenTeamsPage();
            packet.readPacketData(pb);
            return packet;
        };
    }

    public enum Type {
        INDIFFERENT,
        INSAME,
        INNONE
    }
}
