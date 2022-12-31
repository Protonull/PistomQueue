package uk.protonull.test;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.protonull.pistomqueue.utilities.StringIterator;

public class ProtocolTest {

    @Test
    public void testStringSerialisation() {
        final var controlUUIDs = new UUID[] { UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID() };

        @SuppressWarnings("UnstableApiUsage")
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        for (final UUID controlUUID : controlUUIDs) {
            out.writeUTF(controlUUID.toString());
        }

        final UUID[] parsedUUIDs = new StringIterator(out.toByteArray())
                .toStream()
                .map(UUID::fromString)
                .toArray(UUID[]::new);

        Assertions.assertArrayEquals(
                controlUUIDs,
                parsedUUIDs
        );
    }

}
