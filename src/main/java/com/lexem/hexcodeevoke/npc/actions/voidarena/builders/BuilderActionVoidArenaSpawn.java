package com.lexem.hexcodeevoke.npc.actions.voidarena.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleArrayValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ParticleSystemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionSpawn;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders.BuilderActionSpawn;
import com.lexem.hexcodeevoke.npc.actions.voidarena.ActionVoidArenaSpawn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class BuilderActionVoidArenaSpawn extends BuilderActionSpawn {
    private final BooleanHolder emerge = new BooleanHolder();
    private final NumberArrayHolder spawnOffsets = new NumberArrayHolder();
    private final AssetHolder spawnParticles = new AssetHolder();
    private final NumberArrayHolder scatterDistanceRange = new NumberArrayHolder();
    private final DoubleHolder scatterHeight = new DoubleHolder();

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Spawn a Void Arena enemy out of a summoning portal.";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return "Same fields as the Spawn action. The enemy stays still and unarmed while its Spawn animation plays. With Emerge false it skips the portal and the wait, and SpawnOffsets places each spawn on a point of the spawner's body.";
    }

    @Nonnull
    @Override
    public BuilderActionVoidArenaSpawn readConfig(@Nonnull JsonElement data) {
        super.readConfig(data);
        this.getBoolean(data, "Emerge", this.emerge, true, BuilderDescriptorState.Stable, "Whether the enemy comes out of a summoning portal and stays still and unarmed for a moment", null);
        this.getDoubleArray(data, "SpawnOffsets", this.spawnOffsets, new double[0], 0, 30, TriplesValidator.INSTANCE, BuilderDescriptorState.Stable, "Points on the spawner's body where the spawns appear", "Consecutive right, up and forward distances in blocks, relative to the spawner's position and body yaw. Each spawn takes the next point and the list wraps around. Empty keeps the positions of the Spawn action.");
        this.getAsset(data, "SpawnParticles", this.spawnParticles, null, ParticleSystemExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.NULLABLE)), BuilderDescriptorState.Stable, "Particle system played where each spawn appears", "Played at the final position of every spawn, after SpawnOffsets. Empty plays nothing.");
        this.getDoubleRange(data, "ScatterDistanceRange", this.scatterDistanceRange, new double[]{0, 0}, DoubleSequenceValidator.betweenWeaklyMonotonic(0, 30), BuilderDescriptorState.Stable, "Horizontal distance from the spawner where scattered spawns land", "With a range above 0 each spawn is thrown up and lands this far from the spawner, in a random direction; every second spawn lands on the opposite side of the one before. [0, 0] (default) does not scatter.");
        this.getDouble(data, "ScatterHeight", this.scatterHeight, 3, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Height above the spawn point at the top of a scattered throw", null);
        return this;
    }

    @Nonnull
    @Override
    public ActionSpawn build(@Nonnull BuilderSupport builderSupport) {
        return new ActionVoidArenaSpawn(this, builderSupport);
    }

    public boolean isEmerge(@Nonnull BuilderSupport support) {
        return this.emerge.get(support.getExecutionContext());
    }

    @Nonnull
    public double[] getSpawnOffsets(@Nonnull BuilderSupport support) {
        double[] value = this.spawnOffsets.get(support.getExecutionContext());
        return value != null ? value : new double[0];
    }

    @Nonnull
    public double[] getScatterDistanceRange(@Nonnull BuilderSupport support) {
        double[] value = this.scatterDistanceRange.get(support.getExecutionContext());
        return value != null && value.length == 2 ? value : new double[]{0, 0};
    }

    public double getScatterHeight(@Nonnull BuilderSupport support) {
        return this.scatterHeight.get(support.getExecutionContext());
    }

    @Nullable
    public String getSpawnParticles(@Nonnull BuilderSupport support) {
        String value = this.spawnParticles.get(support.getExecutionContext());
        return value == null || value.isEmpty() ? null : value;
    }

    private static final class TriplesValidator extends DoubleArrayValidator {
        private static final TriplesValidator INSTANCE = new TriplesValidator();

        @Override
        public boolean test(double[] array) {
            return array == null || array.length % 3 == 0;
        }

        @Override
        public String errorMessage(double[] array, String name) {
            return name + " needs right, up and forward triples, got " + (array == null ? 0 : array.length) + " values";
        }

        @Override
        public String errorMessage(double[] array) {
            return errorMessage(array, "SpawnOffsets");
        }
    }
}
