package com.lexem.hexcodeevoke.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ParticleTestCommand extends AbstractPlayerCommand {
    private static final String LIST = "list";

    private final RequiredArg<String> idArg;
    private final DefaultArg<Double> distanceArg;
    private final DefaultArg<Double> heightArg;
    private final DefaultArg<Float> scaleArg;

    public ParticleTestCommand() {
        super("particle", "Spawn a particle system in front of you, or list the Evoke ones");
        this.idArg = this.withRequiredArg("id", "Particle system id, or list", ArgTypes.STRING);
        this.distanceArg = this.withDefaultArg("distance", "Blocks in front of you", ArgTypes.DOUBLE, 3.0, "3");
        this.heightArg = this.withDefaultArg("height", "Blocks above your feet", ArgTypes.DOUBLE, 1.0, "1");
        this.scaleArg = this.withDefaultArg("scale", "Particle scale", ArgTypes.FLOAT, 1.0F, "1");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        String id = idArg.get(context);
        if (LIST.equalsIgnoreCase(id)) {
            List<String> ids = ParticleSystem.getAssetMap().getAssetMap().keySet().stream()
                    .filter(key -> key.startsWith("Evoke_"))
                    .sorted()
                    .toList();
            playerRef.sendMessage(Message.raw("[Evoke] Particle systems: " + String.join(", ", ids)));
            return;
        }

        if (!ParticleSystem.getAssetMap().getAssetMap().containsKey(id)) {
            playerRef.sendMessage(Message.raw("[Evoke] Unknown particle system " + id + ". Use 'list' to see the Evoke ones"));
            return;
        }

        Vector3d position = inFrontOf(store, ref, distanceArg.get(context), heightArg.get(context));
        if (position == null) return;

        ParticleUtil.spawnParticleEffect(id, position, 0.0F, 0.0F, 0.0F, scaleArg.get(context), 1.0F, store);
        playerRef.sendMessage(Message.raw("[Evoke] Spawned " + id));
    }

    @Nullable
    static Vector3d inFrontOf(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, double distance, double height) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return null;

        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        float yaw = headRotation != null ? headRotation.getRotation().yaw() : transform.getRotation().yaw();
        return new Vector3d(transform.getPosition())
                .add(-Math.sin(yaw) * distance, height, -Math.cos(yaw) * distance);
    }
}
