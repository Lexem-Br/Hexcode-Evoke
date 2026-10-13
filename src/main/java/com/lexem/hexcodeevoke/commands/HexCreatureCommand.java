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
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.utils.ErrorMessageUtils;

import javax.annotation.Nonnull;

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
        EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
        if (evoker == null) return;

        HexCreatureComponent hexCreature = evoker.getHexCreatureByUUID(npcUUIDArg.get(context));
        if (hexCreature == null) {
            ErrorMessageUtils.sendMessageError(playerRef, "errors.invalid_target", "errors.no_hex_creture_found");
            return;
        }

        playerRef.sendMessage(Message.raw(hexCreature.toString()));
        LOGGER.atInfo().log("[Evoke]: " + hexCreature);
    }
}
