package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChestViewpointUtils {
    private static final double FREE_SEARCH_STEP = 0.25;
    private static final double MIN_FREE_HEIGHT = 0.25;

    private ChestViewpointUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void findViewpoint(
            @Nonnull Vector3d chest,
            double heightOffset,
            @Nonnull World world,
            @Nullable MotionController motionController,
            @Nonnull Store<EntityStore> store,
            @Nonnull Vector3d result
    ) {
        double x = Math.floor(chest.x);
        double y = Math.floor(chest.y);
        double z = Math.floor(chest.z);
        result.set(x + 0.5, y + 1.0 + heightOffset, z + 0.5);
        if (motionController == null) return;

        for (double height = heightOffset; height >= MIN_FREE_HEIGHT; height -= FREE_SEARCH_STEP) {
            result.set(x + 0.5, y + 1.0 + height, z + 0.5);
            if (motionController.isValidPosition(result, store)) return;
        }

        int[] front = getFrontDirection(world, (int) x, (int) y, (int) z);
        int[][] sides = {
                front,
                {-front[1], front[0]},
                {front[1], -front[0]},
                {-front[0], -front[1]}
        };
        for (int[] side : sides) {
            result.set(x + 0.5 + side[0], y, z + 0.5 + side[1]);
            if (motionController.isValidPosition(result, store)) return;
        }

        result.set(x + 0.5 + front[0], y, z + 0.5 + front[1]);
    }

    @Nonnull
    private static int[] getFrontDirection(@Nonnull World world, int x, int y, int z) {
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
        BlockSection blockSection = section != null ? section.getStore().getComponent(section, BlockSection.getComponentType()) : null;
        if (blockSection == null) return new int[] {0, 1};

        RotationTuple rotation = RotationTuple.get(blockSection.getRotationIndex(x, y, z));
        float facingYaw = (float) (rotation.yaw().getRadians() + Math.PI);
        int frontX = (int) Math.round(PhysicsMath.headingX(facingYaw));
        int frontZ = (int) Math.round(PhysicsMath.headingZ(facingYaw));
        return frontX == 0 && frontZ == 0 ? new int[] {0, 1} : new int[] {frontX, frontZ};
    }
}
