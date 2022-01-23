package github.pitbox46.monetamoney.data;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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

    public static ChunkLoader fromJson(JsonObject jsonObject) {
        ResourceLocation dim = new ResourceLocation(jsonObject.getAsJsonObject().get("dimension").getAsString());
        long pos = jsonObject.getAsJsonObject().get("pos").getAsLong();
        String owner = jsonObject.getAsJsonObject().get("owner").getAsString();
        Status status = Status.getStatus(jsonObject.getAsJsonObject().get("status").getAsString());

        return new ChunkLoader(dim, BlockPos.of(pos), owner, status);
    }

    public static ChunkLoader readChunk(FriendlyByteBuf pb) {
        return new ChunkLoader(pb.readResourceLocation(), BlockPos.of(pb.readLong()), pb.readUtf(), Status.getStatus(pb.readUtf()));
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pos", pos.asLong());
        jsonObject.addProperty("dimension", dimensionKey.toString());
        jsonObject.addProperty("owner", owner);
        jsonObject.addProperty("status", status.name);
        return jsonObject;
    }

    public FriendlyByteBuf writeChunk(FriendlyByteBuf pb) {
        pb.writeResourceLocation(this.dimensionKey);
        pb.writeLong(this.pos.asLong());
        pb.writeUtf(this.owner);
        pb.writeUtf(this.status.name);
        return pb;
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
