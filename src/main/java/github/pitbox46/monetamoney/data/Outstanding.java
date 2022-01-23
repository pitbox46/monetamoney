package github.pitbox46.monetamoney.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.loading.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Outstanding {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static File jsonFile;

    public static void init(Path folder) {
        File file = new File(FileUtils.getOrCreateDirectory(folder, "monetamoney").toFile(), "outstanding.json");
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

    public static UUID newCoin(File jsonFile, long amount, String creator) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            UUID uuid = new UUID(System.nanoTime(), Double.doubleToLongBits(Math.random()));

            JsonObject coinEntry = new JsonObject();
            coinEntry.addProperty("amount", amount);
            coinEntry.addProperty("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            coinEntry.addProperty("creator", creator);

            jsonObject.add(uuid.toString(), coinEntry);
            FileWriter writer = new FileWriter(jsonFile);
            writer.write(GSON.toJson(jsonObject));
            writer.close();
            return uuid;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean redeemCoin(File jsonFile, String name, UUID uuid) {
        if (uuid == null)
            return false;
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            JsonElement coinElement = jsonObject.remove(uuid.toString());
            if (coinElement != null && coinElement.isJsonObject()) {
                JsonObject coinObject = coinElement.getAsJsonObject();
                long amount = coinObject.getAsJsonPrimitive("amount").getAsLong();
                Ledger.addBalance(Ledger.jsonFile, name, amount);

                FileWriter writer = new FileWriter(jsonFile);
                writer.write(GSON.toJson(jsonObject));
                writer.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean redeemTeamCoin(File jsonFile, Team team, UUID uuid) {
        if (uuid == null)
            return false;
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            JsonElement coinElement = jsonObject.remove(uuid.toString());
            if (coinElement != null && coinElement.isJsonObject()) {
                JsonObject coinObject = coinElement.getAsJsonObject();
                long amount = coinObject.getAsJsonPrimitive("amount").getAsLong();
                team.balance += amount;

                FileWriter writer = new FileWriter(jsonFile);
                writer.write(GSON.toJson(jsonObject));
                writer.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isValidCoin(File jsonFile, UUID uuid) {
        if (uuid == null)
            return false;
        try (Reader reader = new FileReader(jsonFile)) {
            JsonObject jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
            assert jsonObject != null;

            JsonElement coinElement = jsonObject.get(uuid.toString());
            return coinElement != null && coinElement.isJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
