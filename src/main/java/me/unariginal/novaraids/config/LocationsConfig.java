package me.unariginal.novaraids.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.unariginal.novaraids.NovaRaids;
import me.unariginal.novaraids.data.Location;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LocationsConfig {
    private final NovaRaids nr = NovaRaids.INSTANCE;

    public List<Location> locations = new ArrayList<>();

    public LocationsConfig() {
        try {
            loadLocations();
        } catch (IOException | NullPointerException | UnsupportedOperationException e) {
            NovaRaids.LOADED = false;
            NovaRaids.LOGGER.error("[NovaRaids] Failed to load locations file.", e);
        }
    }

    public void loadLocations() throws IOException, NullPointerException, UnsupportedOperationException {
        File rootFolder = FMLPaths.CONFIGDIR.get().resolve("NovaRaids").toFile();
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }

        File file = FMLPaths.CONFIGDIR.get().resolve("NovaRaids/locations.json").toFile();

        JsonObject root = new JsonObject();
        if (file.exists()) root = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();

        if (root.keySet().isEmpty()) {
            JsonObject exampleLocationObject = new JsonObject();
            exampleLocationObject.addProperty("name", "Example Location");
            root.add("example_location", exampleLocationObject);
        }

        locations.clear();
        for (String key : root.keySet()) {
            JsonObject locationObject = root.getAsJsonObject(key);
            String name = JsonHelper.getString(locationObject, "name", key);

            double x = JsonHelper.getDouble(locationObject, "x_pos", 0);
            double y = JsonHelper.getDouble(locationObject, "y_pos", 100);
            double z = JsonHelper.getDouble(locationObject, "z_pos", 0);

            Vec3d pos = new Vec3d(x, y, z);

            ServerWorld world = getWorld(locationObject);
            int borderRadius = JsonHelper.getInt(locationObject, "border_radius", 30);
            int bossPushbackRadius = JsonHelper.getInt(locationObject, "boss_pushback_radius", 5);
            float bossFacingDirection = JsonHelper.getFloat(locationObject, "boss_facing_direction", 0);
            boolean useJoinLocation = JsonHelper.getBoolean(locationObject, "use_join_location", false);
            double joinX = 0;
            double joinY = 100;
            double joinZ = 0;
            float yaw = 0;
            float pitch = 0;

            if (locationObject.has("join_location")) {
                JsonObject joinLocationObject = locationObject.get("join_location").getAsJsonObject();
                joinX = JsonHelper.getDouble(joinLocationObject, "x_pos", joinX);
                joinY = JsonHelper.getDouble(joinLocationObject, "y_pos", joinY);
                joinZ = JsonHelper.getDouble(joinLocationObject, "z_pos", joinZ);

                yaw = JsonHelper.getFloat(joinLocationObject, "yaw", yaw);
                pitch = JsonHelper.getFloat(joinLocationObject, "pitch", pitch);
            }

            Vec3d join_pos = new Vec3d(joinX, joinY, joinZ);

            String onComplete = JsonHelper.getString(locationObject, "on_complete", "");

            locations.add(new Location(key, name, pos, world, borderRadius, bossPushbackRadius, bossFacingDirection, useJoinLocation, join_pos, yaw, pitch, onComplete));
        }

        for (Location location : locations) {
            root.remove(location.id());

            JsonObject locationObject = new JsonObject();
            locationObject.addProperty("x_pos", location.pos().getX());
            locationObject.addProperty("y_pos", location.pos().getY());
            locationObject.addProperty("z_pos", location.pos().getZ());
            locationObject.addProperty("world", location.world().getRegistryKey().getValue().toString());
            locationObject.addProperty("name", location.name());
            locationObject.addProperty("border_radius", location.borderRadius());
            locationObject.addProperty("boss_pushback_radius", location.bossPushbackRadius());
            locationObject.addProperty("boss_facing_direction", location.bossFacingDirection());
            locationObject.addProperty("use_join_location", location.useSetJoinLocation());
            JsonObject joinLocationObject = new JsonObject();
            joinLocationObject.addProperty("x_pos", location.joinLocation().getX());
            joinLocationObject.addProperty("y_pos", location.joinLocation().getY());
            joinLocationObject.addProperty("z_pos", location.joinLocation().getZ());
            joinLocationObject.addProperty("yaw", location.yaw());
            joinLocationObject.addProperty("pitch", location.pitch());
            locationObject.add("join_location", joinLocationObject);
            locationObject.addProperty("on_complete", location.onComplete());

            root.add(location.id(), locationObject);
        }

        file.delete();
        file.createNewFile();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Writer writer = new FileWriter(file);
        gson.toJson(root, writer);
        writer.close();
    }

    private ServerWorld getWorld(JsonObject locationObject) {
        String worldPath = locationObject.get("world").getAsString();

        ServerWorld world = this.nr.server().getOverworld();

        boolean found = false;
        for (ServerWorld w : nr.server().getWorlds()) {
            String id = w.getRegistryKey().getValue().toString();
            String path = w.getRegistryKey().getValue().getPath();
            if (id.equals(worldPath) || path.equals(worldPath)) {
                world = w;
                found = true;
                break;
            }
        }
        if (!found) {
            nr.logError("World " + worldPath + " not found. Using overworld.");
        }

        return world;
    }

    public Location getLocation(String key) {
        for (Location loc : locations) {
            if (loc.id().equalsIgnoreCase(key)) {
                return loc;
            }
        }
        return null;
    }
}