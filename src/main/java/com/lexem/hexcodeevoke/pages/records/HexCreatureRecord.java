package com.lexem.hexcodeevoke.pages.records;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public record HexCreatureRecord(int index, String name, String blockId, Ref<EntityStore> refESNPC) {
}
