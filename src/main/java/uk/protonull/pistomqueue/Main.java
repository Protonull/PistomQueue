package uk.protonull.pistomqueue;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import java.util.Collections;
import java.util.Map;
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
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import net.minestom.server.world.biome.BiomeEffects;
import net.minestom.server.world.biome.BiomeParticle;
import org.jetbrains.annotations.NotNull;

public final class Main {
    private static final MinecraftServer SERVER = MinecraftServer.init();
    private static final Map<UUID, Player> PLAYERS = Collections.synchronizedMap(new TreeMap<>());

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

        final DynamicRegistry.Key<DimensionType> dimension = MinecraftServer.getDimensionTypeRegistry().register(
            NamespaceID.from("minecraft:queue"),
            DimensionType.builder()
                .effects("minecraft:the_end")
                .build()
        );

        final DynamicRegistry.Key<Biome> biome = MinecraftServer.getBiomeRegistry().register(
            NamespaceID.from("minecraft:queue"),
            Biome.builder()
                .effects(
                    BiomeEffects
                        .builder()
                        .biomeParticle(new BiomeParticle(
                            0.3f,
                            Particle.fromNamespaceId(NamespaceID.from("minecraft:underwater"))
                        ))
                        .build()
                )
                .build()
        );

        final InstanceContainer world = MinecraftServer.getInstanceManager().createInstanceContainer(dimension);
        world.setGenerator((final GenerationUnit unit) -> {
            unit.modifier().fillBiome(biome);
            unit.modifier().fillHeight(0, 1, Block.BARRIER);
        });

        //noinspection UnstableApiUsage
        world.eventNode()
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

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, (event) -> {
            final Player player = event.getPlayer();
            MinecraftServer.LOGGER.info("[{}] has connected!", player.getUsername());

            event.setSpawningInstance(world);
            // Put the player relatively randomly
            player.setRespawnPoint(new Pos(
                ThreadLocalRandom.current().nextDouble(-16, 16),
                1,
                ThreadLocalRandom.current().nextDouble(-16, 16)
            ));
            final boolean isExempted = Config.EXEMPTED_PLAYERS.contains(player.getUsername());
            if (Config.HIDE_PLAYERS) {
                player.updateViewableRule((otherPlayer) -> !isExempted);
            }
            player.setGameMode(GameMode.ADVENTURE);
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, (event) -> {
            MinecraftServer.LOGGER.info("[{}] has disconnected!", event.getPlayer().getUsername());
        });

        if (Config.PLAY_XP) {
            MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent.class, (event) -> {
                // https://github.com/AlexProgrammerDE/PistonQueue/blob/main/bukkit/src/main/java/net/pistonmaster/pistonqueue/bukkit/QueuePluginMessageListener.java
                if (!"piston:queue".equals(event.getIdentifier())) {
                    return;
                }
                final ByteArrayDataInput in = ByteStreams.newDataInput(event.getMessage());
                if (!"xpV2".equals(in.readUTF())) {
                    return;
                }
                final int amountOfPlayers = in.readInt();
                for (int i = 0; i < amountOfPlayers; i++) {
                    final UUID playerUUID; {
                        final String raw = in.readUTF();
                        try {
                            playerUUID = UUID.fromString(raw);
                        }
                        catch (final IllegalArgumentException ignored) {
                            MinecraftServer.LOGGER.warn("[xpV2] Could not parse [{}] into a UUID!", raw);
                            continue;
                        }
                    }
                    final Player player = PLAYERS.get(playerUUID);
                    if (player == null) {
                        MinecraftServer.LOGGER.warn("[xpV2] Received XP chime for non-existent player [{}]", playerUUID);
                        continue;
                    }
                    player.playSound(XP_SOUND, Sound.Emitter.self());
                }
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
