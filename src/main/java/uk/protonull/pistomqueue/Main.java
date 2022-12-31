package uk.protonull.pistomqueue;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.biomes.BiomeEffects;
import net.minestom.server.world.biomes.BiomeParticle;
import uk.protonull.pistomqueue.utilities.StringIterator;

@UtilityClass
public class Main {

    private final MinecraftServer SERVER = MinecraftServer.init();

    public final DimensionType DIMENSION;
    static {
        MinecraftServer.getDimensionTypeManager().addDimension(DIMENSION = DimensionType
                .builder(NamespaceID.from("minecraft:queue"))
                .effects("minecraft:the_end")
                .build()
        );
    }

    public final Biome BIOME;
    static {
        MinecraftServer.getBiomeManager().addBiome(BIOME = Biome
                .builder()
                .name(NamespaceID.from("minecraft:queue"))
                .effects(BiomeEffects
                        .builder()
                        .biomeParticle(new BiomeParticle(
                                0.3f,
                                new BiomeParticle.NormalOption(NamespaceID.from("minecraft:underwater")))
                        )
                        .build())
                .build()
        );
    }

    public final InstanceContainer WORLD = MinecraftServer.getInstanceManager().createInstanceContainer(DIMENSION);
    static {
        WORLD.setGenerator((final GenerationUnit unit) -> {
            unit.modifier().fillBiome(BIOME);
            unit.modifier().fillHeight(0, 1, Block.BARRIER);
        });
    }

    public final Map<UUID, Player> PLAYERS = new TreeMap<>();
    static {
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

    public final Sound XP_SOUND = Sound.sound(
            Key.key("entity.player.levelup"),
            Sound.Source.PLAYER,
            100.0f,
            1.0f
    );

    public void main(final String[] args) {
        MinecraftServer.setBrandName("PistomQueue");

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, (event) -> {
            event.setSpawningInstance(WORLD);
            final Player player = event.getPlayer();
            // Put the player relatively randomly
            player.setRespawnPoint(new Pos(
                    ThreadLocalRandom.current().nextDouble(-16, 16),
                    1,
                    ThreadLocalRandom.current().nextDouble(-16, 16)
            ));
            player.setGameMode(Config.FORCE_GAMEMODE ? Config.FORCED_GAMEMODE : GameMode.ADVENTURE);
            player.setAutoViewable(!Config.HIDE_PLAYERS);
        });

        if (Config.PLAY_XP) {
            MinecraftServer.getGlobalEventHandler().addListener(PlayerPluginMessageEvent.class, (event) -> {
                https://github.com/AlexProgrammerDE/PistonQueue/blob/main/bukkit/src/main/java/net/pistonmaster/pistonqueue/bukkit/QueuePluginMessageListener.java
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
            // Override chat packet handler... perhaps an unnecessary micro-optimisation over just cancelling the chat event
            MinecraftServer.getPacketListenerManager().setListener(ClientChatMessagePacket.class, (packet, player) -> {});
        }

        SERVER.start(Config.HOST, Config.PORT);
    }

}
