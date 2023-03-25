package vin35.autoattack.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.GameVersion;
import net.minecraft.MinecraftVersion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class UpdateUtil {
    private static String getJsonString(String sURL) {
        try {
            URL obj = new URL(sURL);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonArray getJsonArray(String sURL) {
        String jsonStr = getJsonString(sURL);
        assert jsonStr != null;
        return JsonParser.parseString(jsonStr).getAsJsonArray();
    }

    public static JsonObject getJsonObject(String sURL) {
        String jsonStr = getJsonString(sURL);
        assert jsonStr != null;
        return JsonParser.parseString(jsonStr).getAsJsonObject();
    }

    public static String getMinecraftVersion() {
        GameVersion minecraftVersion = MinecraftVersion.create();
        String versionStr = minecraftVersion.getId();
        return versionStr;
    }

    public static int compare(String v1, String v2) {
        String s1 = normalisedVersion(v1);
        String s2 = normalisedVersion(v2);
        int cmp = s1.compareTo(s2);
        return cmp;
    }

    public static String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    public static String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }
}
