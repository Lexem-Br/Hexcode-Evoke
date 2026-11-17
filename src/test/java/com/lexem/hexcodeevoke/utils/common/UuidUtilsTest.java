package com.lexem.hexcodeevoke.utils.common;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UuidUtilsTest {
    @Test
    void parsesValidUuids() {
        UUID uuid = UUID.randomUUID();

        assertEquals(uuid, UuidUtils.parse(uuid.toString()));
    }

    @Test
    void returnsNullForInvalidInput() {
        assertNull(UuidUtils.parse(null));
        assertNull(UuidUtils.parse(""));
        assertNull(UuidUtils.parse("not-a-uuid"));
    }
}
