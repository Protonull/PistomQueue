package uk.protonull.pistomqueue.utilities;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public final class StringIterator implements Iterator<String> {

    private final ByteBuffer buffer;

    public StringIterator(final byte @NotNull [] buffer) {
        this.buffer = ByteBuffer.wrap(buffer);
    }

    @Override
    public boolean hasNext() {
        return this.buffer.hasRemaining();
    }

    @Override
    public @NotNull String next() {
        // TODO: This can probably be done way better
        final byte[] bytes = new byte[this.buffer.getShort()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = this.buffer.get();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * @return Returns a Stream of the remaining strings from the given buffer.
     */
    public @NotNull Stream<String> toStream() {
        final Stream.Builder<String> builder = Stream.builder();
        forEachRemaining(builder::add);
        return builder.build();
    }

}
