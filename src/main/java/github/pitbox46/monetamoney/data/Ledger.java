package github.pitbox46.monetamoney.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.loading.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;

public class Ledger {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static File jsonFile;

    public static void init(Path folder) {
        File file = new File(FileUtils.getOrCreateDirectory(folder, "monetamoney").toFile(), "ledger.json");
        try {
            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file);
                JsonObject jsonObject = new JsonObject();
                JsonObject miscData = new JsonObject();
                miscData.addProperty("previous_pay", 0);
                jsonObject.add("%MISC_DATA%", miscData);
                writer.write(GSON.toJson(jsonObject));
                writer.close();
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        jsonFile = file;
    }

    /**
     * @param jsonFile
     * @param name
     * @param balance
     * @return Whether or not a new player was added to ledger
     */
    public static boolean newPlayer(File jsonFile, String name, long balance) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            if (jsonObject.has(name)) {
                return false;
            } else {
                JsonObject playerEntry = new JsonObject();
                playerEntry.addProperty("balance", balance);
                playerEntry.addProperty("last_reward", 0);

                jsonObject.add(name, playerEntry);
            }
            FileWriter writer = new FileWriter(jsonFile);
            writer.write(GSON.toJson(jsonObject));
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static long readBalance(File jsonFile, String name) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.parse(reader);

            return jsonObject.get(name).getAsJsonObject().get("balance").getAsLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long readLastReward(File jsonFile, String name) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.parse(reader);

            return jsonObject.get(name).getAsJsonObject().get("last_reward").getAsLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean updateLastReward(File jsonFile, String name) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            if (jsonObject.has(name)) {
                JsonObject playerEntry = jsonObject.get(name).getAsJsonObject();
                playerEntry.remove("last_reward");
                playerEntry.addProperty("last_reward", System.currentTimeMillis());
            } else {
                JsonObject playerEntry = new JsonObject();
                playerEntry.addProperty("balance", 0);
                playerEntry.addProperty("last_reward", System.currentTimeMillis());
                jsonObject.add(name, playerEntry);
            }
            FileWriter writer = new FileWriter(jsonFile);
            writer.write(GSON.toJson(jsonObject));
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param jsonFile Ledger file
     * @param name     Player username
     * @param balance  Amount to set balance too
     * @return Whether or not the function succeeded
     */
    public static boolean setBalance(File jsonFile, String name, long balance) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            if (jsonObject.has(name)) {
                JsonObject playerEntry = jsonObject.get(name).getAsJsonObject();
                playerEntry.remove("balance");
                playerEntry.addProperty("balance", balance);
            } else {
                JsonObject playerEntry = new JsonObject();
                playerEntry.addProperty("balance", balance);
                playerEntry.addProperty("last_reward", 0);
                jsonObject.add(name, playerEntry);
            }
            FileWriter writer = new FileWriter(jsonFile);
            writer.write(GSON.toJson(jsonObject));
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param jsonFile Ledger file
     * @param name     Player username
     * @param change   Amount to change balance by (can be negative)
     * @return Whether or not the function succeeded
     */
    public static boolean addBalance(File jsonFile, String name, long change) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            if (jsonObject.has(name)) {
                JsonObject playerEntry = jsonObject.get(name).getAsJsonObject();
                playerEntry.addProperty("balance", playerEntry.remove("balance").getAsLong() + change);
            } else {
                JsonObject playerEntry = new JsonObject();
                playerEntry.addProperty("balance", change);
                playerEntry.addProperty("last_reward", 0);
                jsonObject.add(name, playerEntry);
            }
            reader.close();
            FileWriter writer = new FileWriter(jsonFile);
            writer.write(GSON.toJson(jsonObject));
            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static long getLastRewardTime(File jsonFile, String key) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            if (jsonObject.has("%MISC_DATA%")) {
                JsonObject data = jsonObject.get("%MISC_DATA%").getAsJsonObject();
                if (data.has(key)) return data.getAsJsonPrimitive(key).getAsLong();
                else {
                    data.addProperty(key, 0);
                }
            } else {
                JsonObject data = new JsonObject();
                data.addProperty(key, 0);
//                jsonObject.add("%MISC_DATA%", data);
            }
            reader.close();
            FileWriter writer = new FileWriter(jsonFile);
            writer.write(GSON.toJson(jsonObject));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void updateLastTime(File jsonFile, String key) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            if (jsonObject.has("%MISC_DATA%")) {
                JsonObject data = jsonObject.get("%MISC_DATA%").getAsJsonObject();
                data.remove(key);
                data.addProperty(key, System.currentTimeMillis());
            } else {
                JsonObject data = new JsonObject();
                data.addProperty(key, System.currentTimeMillis());
                jsonObject.add("%MISC_DATA%", data);
            }
            reader.close();
            FileWriter writer = new FileWriter(jsonFile);
            writer.write(GSON.toJson(jsonObject));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
