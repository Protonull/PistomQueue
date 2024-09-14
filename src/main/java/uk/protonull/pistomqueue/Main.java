package uk.protonull.pistomqueue;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.BiomeParticle;
import org.jetbrains.annotations.NotNull;
import uk.protonull.pistomqueue.utilities.StringIterator;

public final class Main {
    private static final MinecraftServer SERVER = MinecraftServer.init();

    public static final DimensionType DIMENSION; static {
        MinecraftServer.getDimensionTypeManager().addDimension(
            DIMENSION = DimensionType
                .builder(NamespaceID.from("minecraft:queue"))
                .effects("minecraft:the_end")
                .build()
        );
    }

    public static final Biome BIOME; static {
        MinecraftServer.getBiomeManager().addBiome(
            BIOME = Biome
                .builder()
                .name(NamespaceID.from("minecraft:queue"))
                .effects(
                    BiomeEffects
                        .builder()
                        .biomeParticle(new BiomeParticle(
                            0.3f,
                            new BiomeParticle.NormalOption(NamespaceID.from("minecraft:underwater")))
                        )
                        .build()
                )
                .build()
        );
    }

    public static final InstanceContainer WORLD = MinecraftServer.getInstanceManager().createInstanceContainer(DIMENSION); static {
        WORLD.setGenerator((final GenerationUnit unit) -> {
            unit.modifier().fillBiome(BIOME);
            unit.modifier().fillHeight(0, 1, Block.BARRIER);
        });
    }

    public static final Map<UUID, Player> PLAYERS = new TreeMap<>(); static {
        //noinspection UnstableApiUsage
        WORLD.eventNode()
            .addListener(AddEntityToInstanceEvent.class, (event) -> {
                if (event.getEntity() instanceof final Player player) {
                    PLAYERS.put(player.getUuid(), player);
                }
            })
            .addListener(RemoveEntityFromInstanceEvent.class, (event) -> {
                if (event.getEntity() instanceof final Player player) {
                    PLAYERS.remove(player.getUuid());
                }
            });
    }

    public static final Sound XP_SOUND = Sound.sound(
        Key.key("entity.player.levelup"),
        Sound.Source.PLAYER,
        100.0f,
        1.0f
    );

    public static void main(
        final @NotNull String @NotNull [] args
    ) {
        MinecraftServer.setBrandName("PistomQueue");

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, (event) -> {
            event.setSpawningInstance(WORLD);
            final Player player = event.getPlayer();
            // Put the player relatively randomly
            player.setRespawnPoint(new Pos(
                ThreadLocalRandom.current().nextDouble(-16, 16),
                1,
                ThreadLocalRandom.current().nextDouble(-16, 16)
            ));
            final boolean isExempted = Config.EXEMPTED_PLAYERS.contains(player.getUsername());
            if (Config.HIDE_PLAYERS) {
                //noinspection UnstableApiUsage
                player.updateViewableRule((otherPlayer) -> !isExempted);
            }
            player.setGameMode(GameMode.ADVENTURE);
        });

        if (Config.PLAY_XP) {
            MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent.class, (event) -> {
                // https://github.com/AlexProgrammerDE/PistonQueue/blob/main/bukkit/src/main/java/net/pistonmaster/pistonqueue/bukkit/QueuePluginMessageListener.java
                if (!"piston:queue".equals(event.getIdentifier())) {
                    return;
                }
                final var in = new StringIterator(event.getMessage());
                if (!"xp".equals(in.next())) {
                    return;
                }
                in.toStream()
                    .map(UUID::fromString)
                    .map(PLAYERS::get)
                    .filter(Objects::nonNull)
                    .forEach((player) -> player.playSound(XP_SOUND, Sound.Emitter.self()));
            });
        }

        if (Config.DISABLE_CHAT) {
            MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, (event) -> {
                if (!Config.EXEMPTED_PLAYERS.contains(event.getPlayer().getUsername())) {
                    event.setCancelled(true);
                }
            });
        }

        switch (Config.PROXY.toUpperCase()) {
            case "NONE" -> {}
            case "BUNGEE" -> {
                BungeeCordProxy.enable();
                Optional.ofNullable(System.getProperty("bungeeTokens"))
                    .map((value) -> value.split(","))
                    .map(Set::of)
                    .ifPresent(BungeeCordProxy::setBungeeGuardTokens);
                MinecraftServer.LOGGER.info("Enabling Bungee proxy");
            }
            case "VELOCITY" -> {
                Optional.ofNullable(System.getProperty("velocitySecret"))
                    .ifPresentOrElse(VelocityProxy::enable, () -> {
                        MinecraftServer.LOGGER.warn("You have enabled Velocity but haven't provided a secret. Set the 'velocitySecret' property.");
                    });
                MinecraftServer.LOGGER.info("Enabling Velocity proxy");
            }
            default -> {
                MinecraftServer.LOGGER.warn("You've specified an unknown proxy [{}] which isn't supported!", Config.PROXY);
            }
        }

        SERVER.start(Config.HOST, Config.PORT);
    }
}
