package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.VoidArenaManager;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class VoidArenaPortalCommand extends AbstractPlayerCommand {
    private final DefaultArg<Integer> minutesArg;

    public VoidArenaPortalCommand() {
        super("portal", "Open a Void Arena portal in front of you");
        this.minutesArg = this.withDefaultArg("minutes", "Minutes the portal stays open, 0 keeps it open", ArgTypes.INTEGER, 5, "5");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        if (VoidArenaManager.isArenaWorld(world)) {
            playerRef.sendMessage(Message.raw("[Evoke] Portals can't be opened inside the Void Arena"));
            return;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        float yaw = headRotation != null ? headRotation.getRotation().yaw() : transform.getRotation().yaw();
        int minutes = Math.max(0, minutesArg.get(context));
        Vector3i pos = VoidArenaManager.openPortal(world, transform.getPosition(), yaw, 3.0, VoidArenaManager.PORTAL_BLOCK, minutes * 60, store);
        if (pos == null) {
            playerRef.sendMessage(Message.raw("[Evoke] No room for a portal in front of you"));
            return;
        }

        String duration = minutes > 0 ? "for " + minutes + " minute(s)" : "until it is removed";
        playerRef.sendMessage(Message.raw("[Evoke] Void Arena portal opened at " + pos.x + " " + pos.y + " " + pos.z + " " + duration));
    }
}
