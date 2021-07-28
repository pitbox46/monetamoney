package github.pitbox46.monetamoney.data;

import com.google.gson.*;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.fml.loading.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Teams {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static File jsonFile;

    public static void init(Path folder) {
        File file = new File(FileUtils.getOrCreateDirectory(folder, "monetamoney").toFile(), "teams.json");
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

    public static boolean newTeam(File jsonFile, Team team) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            jsonObject.add(team.toString(), team.toJson());

            overwrite(jsonFile, jsonObject);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean removeTeam(File jsonFile, String team) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            jsonObject.remove(team);

            overwrite(jsonFile, jsonObject);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Team getTeam(File jsonFile, String key) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            JsonObject teamJson = jsonObject.getAsJsonObject(key);

            if(teamJson != null) {
                return Team.fromJson(teamJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Team.NULL_TEAM;
    }

    public static void updateTeam(File jsonFile, Team newData) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            jsonObject.remove(newData.toString());
            jsonObject.add(newData.toString(), newData.toJson());

            overwrite(jsonFile, jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void overwrite(File jsonFile, JsonObject json) throws IOException {
        FileWriter writer = new FileWriter(jsonFile);
        writer.write(GSON.toJson(json));
        writer.close();
    }

    public static Team getPlayersTeam(File jsonFile, String player) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            for(Map.Entry<String, JsonElement> entry: jsonObject.entrySet()) {
                if(Team.fromJson(entry.getValue().getAsJsonObject()).members.contains(player)) {
                    return Team.fromJson(entry.getValue().getAsJsonObject());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Team.NULL_TEAM;
    }
}
