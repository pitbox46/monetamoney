package github.pitbox46.monetamoney.data;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Arrays;

public class ChunkLoader {
    public final ResourceLocation dimensionKey;
    public final BlockPos pos;
    public final String owner;
    public Status status;

    public ChunkLoader(ResourceLocation dimensionKey, BlockPos pos, String owner, Status status) {
        this.dimensionKey = dimensionKey;
        this.pos = pos;
        this.owner = owner;
        this.status = status;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pos", pos.toLong());
        jsonObject.addProperty("dimension", dimensionKey.toString());
        jsonObject.addProperty("owner", owner);
        jsonObject.addProperty("status", status.name);
        return jsonObject;
    }

    public static ChunkLoader fromJson(JsonObject jsonObject) {
        ResourceLocation dim = new ResourceLocation(jsonObject.getAsJsonObject().get("dimension").getAsString());
        long pos = jsonObject.getAsJsonObject().get("pos").getAsLong();
        String owner = jsonObject.getAsJsonObject().get("owner").getAsString();
        Status status = Status.getStatus(jsonObject.getAsJsonObject().get("status").getAsString());

        return new ChunkLoader(dim, BlockPos.fromLong(pos), owner, status);
    }

    public PacketBuffer writeChunk(PacketBuffer pb) {
        pb.writeResourceLocation(this.dimensionKey);
        pb.writeLong(this.pos.toLong());
        pb.writeString(this.owner);
        pb.writeString(this.status.name);
        return pb;
    }

    public static ChunkLoader readChunk(PacketBuffer pb) {
        return new ChunkLoader(pb.readResourceLocation(), BlockPos.fromLong(pb.readLong()), pb.readString(), Status.getStatus(pb.readString()));
    }

    public enum Status {
        ON("on"),
        OFF("off"),
        STUCK("stuck");

        public String name;
        Status(String name) {
            this.name = name;
        }

        public static Status getStatus(String name) {
            return Arrays.stream(Status.values()).filter(s -> s.name.equals(name)).findFirst().orElseThrow(() -> new RuntimeException("No matching status enum: " + name));
        }
    }
}
