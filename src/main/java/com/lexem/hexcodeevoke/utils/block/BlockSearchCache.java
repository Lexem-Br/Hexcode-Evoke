package com.lexem.hexcodeevoke.utils.block;

import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

public final class BlockSearchCache {
    private static final long RETRY_NANOS = TimeUnit.MILLISECONDS.toNanos(500);

    private final Vector3i found = new Vector3i();
    private boolean hasFound;
    private boolean waitingRetry;
    private long retryAt;

    @Nullable
    public <T> Vector3i find(
            @Nonnull Vector3d startPos,
            int horizontalRange,
            int verticalRange,
            @Nonnull FinderUtils.BlockValidator<T> validator,
            @Nonnull T context
    ) {
        if (hasFound && FinderUtils.isWithinRange(startPos, found, horizontalRange, verticalRange) && validator.isValid(found, context)) {
            return new Vector3i(found);
        }

        long now = System.nanoTime();
        if (!hasFound && waitingRetry && now - retryAt < 0) return null;

        Vector3i block = FinderUtils.findNearestBlock(startPos, horizontalRange, verticalRange, validator, context);
        hasFound = block != null;
        waitingRetry = !hasFound;
        if (hasFound) {
            found.set(block);
        } else {
            retryAt = now + RETRY_NANOS;
        }
        return block;
    }
}
