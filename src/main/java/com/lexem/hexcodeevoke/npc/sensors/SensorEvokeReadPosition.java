package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockMembership;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorEvokeReadPosition;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class SensorEvokeReadPosition extends SensorBase {
   protected final double minRange;
   protected final double range;
   protected final PositionProvider positionProvider = new PositionProvider();
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public SensorEvokeReadPosition(@Nonnull BuilderSensorEvokeReadPosition builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.minRange = builder.getMinRange(support);
      this.range = builder.getRange(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         this.positionProvider.clear();
         return false;
      } else {
         EvokerComponent evoker = this.getEvoker(ref, store);
         if (evoker == null) { return false; }

         Vector3d position = this.getPostion(evoker);
         if (position.equals(Vector3dUtil.MIN)) {
            this.positionProvider.clear();
            return false;
         } else {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
            if (transformComponent == null) { return false; }

            int qtdHexCreatures = evoker.getSelectedHexCreatures().length;
            double dist = transformComponent.getPosition().distanceSquared(position);
            if (
                  (qtdHexCreatures == 1 && dist < 1.0) ||
                  (qtdHexCreatures == 2 && dist < 2.0) ||
                  (qtdHexCreatures >= 3 && dist < 2.5)
            ) {
               this.positionProvider.clear();
               return false;
            } else if (!(dist > this.range * this.range) && !(dist < this.minRange * this.minRange)) {
               this.positionProvider.setTarget(position);
               return true;
            } else {
               this.positionProvider.clear();
               return false;
            }
         }
      }
   }

   private EvokerComponent getEvoker(Ref<EntityStore> ref, Store<EntityStore> store) {
      FlockMembership membership = store.getComponent(ref, FlockMembership.getComponentType());
      if (membership == null) {return null;}

      EntityGroup group = null;
      Ref<EntityStore> flockReference = membership.getFlockRef();
      if (flockReference != null && flockReference.isValid()) {
         group = store.getComponent(flockReference, EntityGroup.getComponentType());
      }
      if (group == null || group.getLeaderRef() == null) {return null;}

      PlayerRef playerRef = store.getComponent(group.getLeaderRef(), PlayerRef.getComponentType());
      if (playerRef == null) {return null;}

      Ref<EntityStore> playerEntityRef = playerRef.getReference();
      if (playerEntityRef == null) {return null;}

      return store.getComponent(playerEntityRef, EvokerComponent.getComponentType());
   }

   private Vector3d getPostion(EvokerComponent evoker) {
      if (evoker == null) {
         LOGGER.atWarning().log("No position data found");
         return new Vector3d();
      } else {
         return evoker.getTargetPosition();
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return this.positionProvider;
   }
}
