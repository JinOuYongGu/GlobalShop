package me.jinou.globalshop.utils;

import me.jinou.globalshop.GlobalShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

/**
 * @author 69142
 */
public class MsgUtil {
    private static FileConfiguration message;

    public static void updateMsg() {
        File msgFile = new File(GlobalShop.get().getDataFolder(), "message.yml");
        if (!msgFile.exists()) {
            GlobalShop.get().saveResource("message.yml", false);
        }
        message = YamlConfiguration.loadConfiguration(msgFile);
    }

    /**
     * @param msgKey    key value in message.yml, for example "reload"
     * @param usePrefix if true will return string contains prefix
     * @return message string in message.yml
     */
    public static String get(String msgKey, boolean usePrefix) {
        String prefix = "";
        if (usePrefix) {
            if (message.contains("msg.prefix")) {
                prefix = message.getString("msg.prefix");
            }
        }

        if (message.contains("msg." + msgKey)) {
            return prefix + message.getString("msg." + msgKey);
        } else {
            return prefix + "§cNo message, please check key §e" + msgKey + "§c in message.yml";
        }
    }

    /**
     * @param msgKey key value in message.yml, for example "reload"
     * @return formatted message string that in message.yml with prefix
     */
    public static String get(String msgKey) {
        return get(msgKey, true);
    }

    public static List<String> getList(String msgKey) {
        msgKey = "msg." + msgKey;
        return message.getStringList(msgKey);
    }
}
