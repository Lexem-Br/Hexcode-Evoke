package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.flock.FlockMembershipSystems;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionSpawnMinion;
import it.unimi.dsi.fastutil.Pair;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionSpawnMinion extends ActionBase {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final String entityId;
   private ExecutionSupport executionSupport;
   private Store<EntityStore> store;

   private static final double MAX_SPAWN_DISTANCE = 0.5;

   public ActionSpawnMinion(@Nonnull BuilderActionSpawnMinion builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.entityId = builder.getEntityId(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(npcRef, executionSupport, sensorInfo, dt, store);
      this.executionSupport = executionSupport;
      this.store = store;

      HexCreatureComponent hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
      if (hexCreatureComponent == null) return false;

      World world = store.getExternalData().getWorld();
      hexCreatureComponent.deleteUnusedMinionUUID(world);

      int roleIndex = NPCPlugin.get().getIndex(entityId);
      if (roleIndex >= 0) {
         TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());
         if (transformComponent == null) return false;

         Vector3d npcPosition = transformComponent.getPosition();

         double angle = RandomExtra.randomRange(0.0, 2.0 * Math.PI);
         double distance = RandomExtra.randomRange(0.0, MAX_SPAWN_DISTANCE);
         double offsetX = distance * Math.cos(angle);
         double offsetZ = distance * Math.sin(angle);
         Vector3d spawnPosition = new Vector3d(
                 npcPosition.x + offsetX,
                 npcPosition.y + 0.5,
                 npcPosition.z + offsetZ
         );

         Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get().spawnEntity(store, roleIndex, spawnPosition, Rotation3f.IDENTITY, null, null);
         if (npcPair == null) return false;

         Ref<EntityStore> minionRef = npcPair.first();
         UUIDComponent minionUUID = store.getComponent(minionRef, UUIDComponent.getComponentType());
         if (minionUUID == null) return false;

         hexCreatureComponent.addMinionUUID(minionUUID.getUuid().toString());
         addMinionToNPCFlock(minionRef, npcRef);
      } else {
         LOGGER.atWarning().log("Unable to spawn entity");
      }

      return true;
   }

   private void addMinionToNPCFlock(Ref<EntityStore> minionRef, Ref<EntityStore> npcRef) {
      FlockMembership targetMembership = npcRef.getStore().getComponent(npcRef, FlockMembership.getComponentType());
      Ref<EntityStore> targetFlockReference = targetMembership != null ? targetMembership.getFlockRef() : null;
      Ref<EntityStore> flockReference = FlockPlugin.getFlockReference(minionRef, store);
      if (flockReference != null) {
         FlockMembershipSystems.join(npcRef, flockReference, store);
      } else if (targetFlockReference != null) {
         FlockMembershipSystems.join(minionRef, targetFlockReference, store);
      } else {
         if (executionSupport.getRole() != null) {
            flockReference = FlockPlugin.createFlock(store, executionSupport.getRole());
            if (executionSupport.getRole().isCanLeadFlock()) {
               FlockMembershipSystems.join(minionRef, flockReference, store);
               FlockMembershipSystems.join(npcRef, flockReference, store);
            } else {
               FlockMembershipSystems.join(npcRef, flockReference, store);
               FlockMembershipSystems.join(minionRef, flockReference, store);
            }
         }
      }
   }
}