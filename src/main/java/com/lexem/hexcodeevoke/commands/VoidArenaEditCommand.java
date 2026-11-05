package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.instances.config.InstanceWorldConfig;
import com.hypixel.hytale.builtin.instances.removal.RemovalCondition;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.VoidArenaManager;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class VoidArenaEditCommand extends AbstractPlayerCommand {
    private static final String EDIT_WORLD = "Evoke_Void_Arena_Edit";

    public VoidArenaEditCommand() {
        super("edit", "Open an editable copy of the Void Arena, kept in universe/worlds/" + EDIT_WORLD);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        if (EDIT_WORLD.equals(world.getName())) {
            playerRef.sendMessage(Message.raw("[Evoke] You are already in the Void Arena edit copy"));
            return;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        CompletableFuture<World> editWorld = InstancesPlugin.get().spawnInstance(
                VoidArenaManager.INSTANCE_NAME,
                EDIT_WORLD,
                world,
                new Transform(transform.getTransform()),
                config -> {
                    config.setSavingPlayers(false);
                    config.setIsAllNPCFrozen(true);
                    config.setTicking(false);
                    config.setGameMode(GameMode.Creative);
                    config.setDeleteOnRemove(false);
                    config.setGameplayConfig("InstanceEdit");
                    InstanceWorldConfig.ensureAndGet(config).setRemovalConditions(RemovalCondition.EMPTY);
                }
        );
        InstancesPlugin.teleportPlayerToLoadingInstance(ref, store, editWorld, null);
        playerRef.sendMessage(Message.raw("[Evoke] Opening the Void Arena edit copy. Changes are saved in universe/worlds/" + EDIT_WORLD
                + ". Copy its chunks folder to Server/Instances/" + VoidArenaManager.INSTANCE_NAME + "/chunks to ship them"));
    }
}
