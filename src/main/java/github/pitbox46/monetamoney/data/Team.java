package github.pitbox46.monetamoney.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.pitbox46.monetamoney.network.SOpenTeamsPage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Team {
    public static final Team NULL_TEAM = new Team(BlockPos.ZERO, new ResourceLocation("void:empty"), "null name", new ArrayList<>(0), 0, true);

    public BlockPos pos;
    public ResourceLocation dim;
    public String leader;
    public List<String> members;
    public long balance;
    public boolean locked;

    public Team(BlockPos pos, ResourceLocation dim, String leader, List<String> members, long balance, boolean locked) {
        this.pos = pos;
        this.dim = dim;
        this.leader = leader;
        this.members = members;
        this.balance = balance;
        this.locked = locked;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pos", pos.toLong());
        jsonObject.addProperty("dimension", dim.toString());
        jsonObject.addProperty("leader", leader);
        JsonArray memberList = new JsonArray();
        members.forEach(memberList::add);
        jsonObject.add("members", memberList);
        jsonObject.addProperty("balance", balance);
        jsonObject.addProperty("locked", locked);
        return jsonObject;
    }

    public static Team fromJson(JsonObject jsonObject) {
        JsonArray array = jsonObject.getAsJsonArray("members");
        List<String> members = new ArrayList<>();
        array.forEach(m -> members.add(m.getAsString()));
        return new Team(BlockPos.fromLong(jsonObject.getAsJsonPrimitive("pos").getAsLong()), new ResourceLocation(jsonObject.getAsJsonPrimitive("dimension").getAsString()), jsonObject.getAsJsonPrimitive("leader").getAsString(), members, jsonObject.getAsJsonPrimitive("balance").getAsLong(), jsonObject.getAsJsonPrimitive("locked").getAsBoolean());
    }

    public PacketBuffer writeTeam(PacketBuffer pb) {
        pb.writeBlockPos(this.pos);
        pb.writeResourceLocation(this.dim);
        pb.writeString(this.leader);
        pb.writeInt(this.members.size());
        for (String member : this.members) {
            pb.writeString(member);
        }
        pb.writeLong(this.balance);
        pb.writeBoolean(this.locked);
        return pb;
    }

    public static Team readTeam(PacketBuffer pb) {
        BlockPos pos = pb.readBlockPos();
        ResourceLocation dim = pb.readResourceLocation();
        String leader = pb.readString();
        List<String> members = new ArrayList<>();
        for(int i = pb.readInt(); i > 0; i--) {
            members.add(pb.readString());
        }
        return new Team(pos, dim, leader, members, pb.readLong(), pb.readBoolean());
    }

    public boolean isNull() {
        return this.equals(Team.NULL_TEAM);
    }

    @Override
    public String toString() {
        return dim.toString() + pos.toLong();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == Team.class) {
            Team team = (Team) obj;
            if(team.pos.equals(this.pos) && team.leader.equals(this.leader) && team.members.equals(this.members) && team.balance == this.balance) {
                return true;
            }
        }
        return super.equals(obj);
    }
}
