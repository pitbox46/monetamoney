package github.pitbox46.monetamoney.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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

    public static Team fromJson(JsonObject jsonObject) {
        JsonArray array = jsonObject.getAsJsonArray("members");
        List<String> members = new ArrayList<>();
        array.forEach(m -> members.add(m.getAsString()));
        return new Team(BlockPos.of(jsonObject.getAsJsonPrimitive("pos").getAsLong()), new ResourceLocation(jsonObject.getAsJsonPrimitive("dimension").getAsString()), jsonObject.getAsJsonPrimitive("leader").getAsString(), members, jsonObject.getAsJsonPrimitive("balance").getAsLong(), jsonObject.getAsJsonPrimitive("locked").getAsBoolean());
    }

    public static Team readTeam(FriendlyByteBuf pb) {
        BlockPos pos = pb.readBlockPos();
        ResourceLocation dim = pb.readResourceLocation();
        String leader = pb.readUtf();
        List<String> members = new ArrayList<>();
        for (int i = pb.readInt(); i > 0; i--) {
            members.add(pb.readUtf());
        }
        return new Team(pos, dim, leader, members, pb.readLong(), pb.readBoolean());
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pos", pos.asLong());
        jsonObject.addProperty("dimension", dim.toString());
        jsonObject.addProperty("leader", leader);
        JsonArray memberList = new JsonArray();
        members.forEach(memberList::add);
        jsonObject.add("members", memberList);
        jsonObject.addProperty("balance", balance);
        jsonObject.addProperty("locked", locked);
        return jsonObject;
    }

    public FriendlyByteBuf writeTeam(FriendlyByteBuf pb) {
        pb.writeBlockPos(this.pos);
        pb.writeResourceLocation(this.dim);
        pb.writeUtf(this.leader);
        pb.writeInt(this.members.size());
        for (String member : this.members) {
            pb.writeUtf(member);
        }
        pb.writeLong(this.balance);
        pb.writeBoolean(this.locked);
        return pb;
    }

    public boolean isNull() {
        return this.equals(Team.NULL_TEAM);
    }

    @Override
    public String toString() {
        return dim.toString() + pos.asLong();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == Team.class) {
            Team team = (Team) obj;
            if (team.pos.equals(this.pos) && team.leader.equals(this.leader) && team.members.equals(this.members) && team.balance == this.balance) {
                return true;
            }
        }
        return super.equals(obj);
    }
}
