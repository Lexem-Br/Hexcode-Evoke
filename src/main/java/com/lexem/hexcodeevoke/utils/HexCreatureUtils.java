package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.util.AimingHelper;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.events.SaveHexCreatureEvent;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import com.lexem.hexcodeevoke.pages.records.HexCreatureRecord;
import it.unimi.dsi.fastutil.Pair;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class HexCreatureUtils {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public HexCreatureUtils() {
    }

    public static boolean trySpawnHexCreature(Vector3i blockPos, Ref<EntityStore> refESPlayer, CommandBuffer<EntityStore> accessor) {
        World world = accessor.getExternalData().getWorld();
        Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);

        int blockRotationIndex = world.getBlockRotationIndex(blockPos.x, blockPos.y, blockPos.z);
        RotationTuple rotation = RotationTuple.get(blockRotationIndex);
        Rotation3f blockRotation = new Rotation3f(0.0F, (float) (rotation.yaw().getRadians() + Math.PI), 0.0F);

        BlockType blockType = world.getBlockType(blockPos);

        if (blockType == null) {
            LOGGER.atWarning().log("Evoke: invalid block");
            return false;
        }

        Map.Entry<String, String> hexItem = HexItemRegistery.getByBlockId(blockType.getId());

        if (hexItem == null) {
            LOGGER.atWarning().log("Evoke: block must be a Hex item");
            return false;
        }

        Store<EntityStore> store = refESPlayer.getStore();
        EvokerComponent evoker = store.getComponent(refESPlayer, EvokerComponent.getComponentType());
        if (evoker == null) { return false; }
        evoker.deleteUnusedHexCreatureUUID(world, evoker.getHexCreatureUUIDs());

        int roleIndex = NPCPlugin.get().getIndex(hexItem.getValue());

        accessor.run(_store -> {
            if (!evoker.canAddHexCreature()) {
                LOGGER.atWarning().log("Evoke: maximum number of Hex creatures reached");
                return;
            }
            if (roleIndex >= 0) {
                Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get().spawnEntity(_store, roleIndex, blockVector, blockRotation, null, null);
                assert npcPair != null;

                world.breakBlock(blockPos.x, blockPos.y, blockPos.z, 0);

                Ref<EntityStore> refESNPC = npcPair.first();
                SaveHexCreatureEvent.dispatch(refESPlayer, refESNPC);
            }  else {
                LOGGER.atWarning().log("Unable to spawn entity");
            }
        });

        return true;
    }

    public void despawnHexCreature(@Nonnull HexCreatureRecord hexCreature, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> accessor) {
        Ref<EntityStore> refESNPC = hexCreature.refESNPC();
        String blockId = hexCreature.blockId();

        World world = accessor.getExternalData().getWorld();

        NPCEntity npcComponent = store.getComponent(refESNPC, Objects.requireNonNull(NPCEntity.getComponentType()));
        if (npcComponent == null) return;

        HexCreatureComponent hexCreatureComponent = store.getComponent(refESNPC, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null) return;

        Ref<EntityStore> refESPlayer = store.getExternalData().getRefFromUUID(UUID.fromString(hexCreatureComponent.getEvokerUUID()));
        if (refESPlayer == null) return;

        ItemStack itemInHand = InventoryComponent.getItemInHand(accessor, refESNPC);
        if (itemInHand != null) {
            double distance = RandomExtra.randomRange(0.2, 0.4);
            Vector3d direction = this.newDirection(refESNPC, distance, accessor, store);
            ItemUtils.throwItem(refESNPC, store, itemInHand, direction, 100);
        }

        ItemStack hexDropItem = InventoryHelper.createItem(blockId);
        if (hexDropItem != null) {
            Vector3d direction = this.newDirection(refESPlayer, 1, accessor, store);
            ItemUtils.throwItem(refESPlayer, store, hexDropItem, direction, 100);
            spawnParticleEffect(refESPlayer, store, 3, accessor);
        }

        spawnParticleEffect(refESNPC, store, 0, accessor);
        deleteHexCreatureUUIDFromEvoker(refESNPC, store, world);
        npcComponent.setToDespawn();
    }

    public void deleteHexCreatureUUIDFromEvoker(@Nonnull Ref<EntityStore> npcESRef, @Nonnull Store<EntityStore> store, @Nonnull World world) {
        HexCreatureComponent hexCreature = store.getComponent(npcESRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null || hexCreature.getUUID() == null) return;

        UUID playerUUID = UUID.fromString(hexCreature.getEvokerUUID());

        Ref<EntityStore> playerRef = world.getEntityStore().getRefFromUUID(playerUUID);
        if (playerRef == null) return;

        EvokerComponent evoker = store.getComponent(playerRef, EvokerComponent.getComponentType());
        if (evoker == null) return;

        evoker.removeHexCreatureUUID(hexCreature.getUUID());
    }

    private Vector3d newDirection(@Nonnull Ref<EntityStore> ref, double distance, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull Store<EntityStore> store) {
        Vector3d dropDirection;
        double[] dropSector =  new double[]{0.0, 0.0};
        float dropSectorStart = (float) (Math.PI / 180.0) * (float)dropSector[0];
        float dropSectorEnd = (float) (Math.PI / 180.0) * (float)dropSector[1];
        double throwSpeed = 100;
        float[] pitch = new float[2];

        ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
        float eyeHeight = modelComponent != null ? modelComponent.getModel().getEyeHeight(ref, store) : 0.0F;
        float height = -eyeHeight;

        TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
        assert transformComponent != null;

        HeadRotation headRotationComponent = componentAccessor.getComponent(ref, HeadRotation.getComponentType());
        Vector3d direction;
        if (headRotationComponent != null) {
            direction = headRotationComponent.getDirection();
        } else {
            Rotation3f rotation = transformComponent.getRotation();
            direction = Vector3dUtil.setYawPitch(rotation.yaw(), rotation.pitch(), new Vector3d());
        }

        dropDirection = direction;
        dropDirection.rotateY(RandomExtra.randomRange(dropSectorStart, dropSectorEnd));

        if (!AimingHelper.computePitch(distance, height, throwSpeed, 32.0, pitch)) {
            throw new IllegalStateException(
                    String.format("Error in computing pitch with distance %s, height %s, and speed %s that was not caught in validation", distance, height, throwSpeed)
            );
        } else {
            float heading = PhysicsMath.headingFromDirection(dropDirection.x, dropDirection.z);
            PhysicsMath.vectorFromAngles(heading, pitch[0], dropDirection).normalize();
        }

        return dropDirection;
    }

    public void spawnParticleEffect(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull double distance, @Nonnull CommandBuffer<EntityStore> accessor) {
        Vector3d direction = this.newDirection(ref, distance, accessor, store);
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent != null) {
            float eyeHeight = 0.0F;
            ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
            if (modelComponent != null) {
                eyeHeight = modelComponent.getModel().getEyeHeight(ref, store);
            }

            Vector3d particlePos = new Vector3d(transformComponent.getPosition());
            particlePos.add(0.0F, eyeHeight, 0.0F).add(direction);

            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = accessor.getResource(
                    EntityModule.get().getPlayerSpatialResourceType()
            );
            List<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(particlePos, 75.0, results);
            ParticleUtil.spawnParticleEffect("Effect_Death", particlePos, results, accessor);
        }
    }
}
