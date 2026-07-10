package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class HexcreaturesListCommand extends AbstractPlayerCommand {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexcreaturesListCommand() {
        super("hexCreaturesList", "Show the list of hex creatures");
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
        if (evoker == null) {
            playerRef.sendMessage(Message.raw("No data found"));
            return;
        }

        String[] uuids = evoker.getHexCreatureUUIDs();

        playerRef.sendMessage(Message.raw("Hex creatures list: " + Arrays.toString(uuids)));
        LOGGER.atInfo().log("Hex creatures list: " + Arrays.toString(uuids));
    }
}
