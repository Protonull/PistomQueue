package uk.protonull.pistomqueue;

import lombok.experimental.UtilityClass;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Config {

    public final String HOST = System.getProperty("host", "0.0.0.0");
    public final int PORT = parseInt("port", 25565);
    public final boolean HIDE_PLAYERS = parseBool("hidePlayers", false);
    public final boolean FORCE_GAMEMODE = parseBool("forceGamemode", false);
    public final GameMode FORCED_GAMEMODE = EnumUtils.getEnum(GameMode.class, System.getProperty("forcedGamemode"), GameMode.SPECTATOR);
    public final boolean DISABLE_CHAT = parseBool("disableChat", false);
    public final boolean PLAY_XP = parseBool("playXP", true);

    // ############################################################
    // Property Parsers
    // ############################################################

    public boolean parseBool(
            final @NotNull String name,
            final boolean defaultValue
    ) {
        return Boolean.parseBoolean(System.getProperty(name, Boolean.toString(defaultValue)));
    }

    public int parseInt(
            final @NotNull String name,
            final int defaultValue
    ) {
        // Yes, I do know about Integer.getInteger() but it'll silently fallback to the defaultValue if the found value
        // is not correctly formatted as a number. I'd prefer to make the error explicit to the user.
        final String property = System.getProperty(name);
        if (property == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(property);
        }
        catch (final NumberFormatException ignored) {
            MinecraftServer.LOGGER.warn("Could not parse config int [name: " + name + "] [value: " + property + "], defaulting to: " + defaultValue);
            return defaultValue;
        }
    }

}
