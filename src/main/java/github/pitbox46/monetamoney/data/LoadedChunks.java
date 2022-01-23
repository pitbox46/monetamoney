package github.pitbox46.monetamoney.data;

import com.google.gson.*;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.loading.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadedChunks {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static File jsonFile;

    public static void init(Path folder) {
        File file = new File(FileUtils.getOrCreateDirectory(folder, "monetamoney").toFile(), "loadedchunks.json");
        try {
            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                writer.write(GSON.toJson(new JsonObject()));
                writer.close();
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        jsonFile = file;
    }

    public static Map<String, List<ChunkLoader>> read(File jsonFile) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            Map<String, List<ChunkLoader>> returnMap = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                if (entry.getValue().isJsonArray()) {
                    Team team = Teams.getTeam(Teams.jsonFile, entry.getKey());
                    String teamString = team.isNull() ? "unlisted" : team.toString();
                    returnMap.putIfAbsent(teamString, new ArrayList<>());

                    for (JsonElement jsonEntry : entry.getValue().getAsJsonArray()) {
                        returnMap.get(teamString).add(ChunkLoader.fromJson(jsonEntry.getAsJsonObject()));
                    }
                }
            }
            return returnMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static void write(File jsonFile, Map<String, List<ChunkLoader>> chunks) {
        try (Writer writer = new FileWriter(jsonFile)) {
            JsonObject jsonObject = new JsonObject();

            for (Map.Entry<String, List<ChunkLoader>> entry : chunks.entrySet()) {
                JsonArray chunksArray = new JsonArray();

                for (ChunkLoader chunkLoader : entry.getValue()) {
                    chunksArray.add(chunkLoader.toJson());
                }

                jsonObject.add(entry.getKey(), chunksArray);
            }

            writer.write(GSON.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
