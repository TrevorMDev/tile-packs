package com.tilepacks;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/** Validation for user-supplied Ground Markers tile imports. */
public final class TilePackImportValidator {
    private TilePackImportValidator() {
    }

    public static boolean isValid(String tilesJson, Gson gson) {
        if (tilesJson == null || tilesJson.isEmpty() || gson == null) {
            return false;
        }

        try {
            JsonElement root = gson.fromJson(tilesJson, JsonElement.class);
            if (root == null || !root.isJsonArray() || root.getAsJsonArray().size() == 0) {
                return false;
            }

            for (JsonElement element : root.getAsJsonArray()) {
                if (element == null || !element.isJsonObject()) {
                    return false;
                }
                JsonObject point = element.getAsJsonObject();
                Integer regionId = readInteger(point, "regionId");
                Integer regionX = readInteger(point, "regionX");
                Integer regionY = readInteger(point, "regionY");
                Integer plane = readInteger(point, "z");
                if (regionId == null || regionId < 0 || regionId > 0xFFFF
                        || regionX == null || regionX < 0 || regionX > 63
                        || regionY == null || regionY < 0 || regionY > 63
                        || plane == null || plane < 0 || plane > 3) {
                    return false;
                }
            }
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static Integer readInteger(JsonObject object, String key) {
        if (!object.has(key)) {
            return null;
        }
        JsonElement element = object.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isNumber()) {
            return null;
        }
        try {
            return Integer.valueOf(primitive.getAsString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
