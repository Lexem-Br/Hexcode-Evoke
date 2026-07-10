package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;

import javax.annotation.Nonnull;
import java.util.UUID;

public class HexCreatureCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> npcUUIDArg;

    public HexCreatureCommand() {
        super("hexCreature", "Show hex creature info");
        this.npcUUIDArg = this.withRequiredArg("npcUUID", "UUID of the NPC", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        UUID npcUUID = UUID.fromString(npcUUIDArg.get(context));

        Ref<EntityStore> npcRef = world.getEntityStore().getRefFromUUID(npcUUID);
        if (npcRef == null) {
            playerRef.sendMessage(Message.raw("Hex creature not found"));
            return;
        }

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null) {
            playerRef.sendMessage(Message.raw("Hex creature not found"));
            return;
        }

        playerRef.sendMessage(Message.raw(hexCreature.toString()));
    }
}
