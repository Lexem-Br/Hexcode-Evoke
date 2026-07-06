package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;

import javax.annotation.Nonnull;

public class TargetPositionCommand extends AbstractPlayerCommand {

    public TargetPositionCommand() {
        super("targetPosition", "Show the target position");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        var rpg = store.getComponent(ref, EvokerComponent.getComponentType());
        if (rpg == null) {
            playerRef.sendMessage(Message.raw("No RPG data found"));
            return;
        }

        var targetPosition = rpg.getTargetPosition();

        playerRef.sendMessage(Message.raw("Target position: %s".formatted(targetPosition)));
    }
}
