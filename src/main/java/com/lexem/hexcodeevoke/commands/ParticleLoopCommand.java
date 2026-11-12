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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.ParticleLoops;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class ParticleLoopCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> idArg;
    private final DefaultArg<Double> intervalArg;
    private final DefaultArg<Double> distanceArg;
    private final DefaultArg<Double> heightArg;
    private final DefaultArg<Float> scaleArg;

    public ParticleLoopCommand() {
        super("particleloop", "Spawn a particle system in front of you again and again until /evoker particlestop");
        this.idArg = this.withRequiredArg("id", "Particle system id", ArgTypes.STRING);
        this.intervalArg = this.withDefaultArg("interval", "Seconds between spawns", ArgTypes.DOUBLE, 1.0, "1");
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
        if (!ParticleSystem.getAssetMap().getAssetMap().containsKey(id)) {
            playerRef.sendMessage(Message.raw("[Evoke] Unknown particle system " + id + ". Use /evoker particle list to see the Evoke ones"));
            return;
        }

        double interval = intervalArg.get(context);
        if (interval <= 0.0) {
            playerRef.sendMessage(Message.raw("[Evoke] Interval must be greater than 0"));
            return;
        }

        Vector3d position = ParticleTestCommand.inFrontOf(store, ref, distanceArg.get(context), heightArg.get(context));
        if (position == null) return;

        int running = ParticleLoops.start(playerRef.getUuid(), world, id, position, scaleArg.get(context), interval);
        playerRef.sendMessage(Message.raw("[Evoke] Spawning " + id + " every " + interval + "s (" + running + " running). Use /evoker particlestop to stop"));
    }
}
