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
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
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
   protected boolean wasSteering = false;
   protected final PositionProvider positionProvider = new PositionProvider();
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public SensorEvokeReadPosition(@Nonnull BuilderSensorEvokeReadPosition builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.minRange = builder.getMinRange(support);
      this.range = builder.getRange(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store) || wasSteering) {
         this.positionProvider.clear();
         return false;
      } else {
         Vector3d position = getPostion(ref, store);

         if (position.equals(Vector3dUtil.MIN)) {
            this.positionProvider.clear();
            return false;
         } else {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
            assert transformComponent != null;

            double dist = transformComponent.getPosition().distanceSquared(position);
            if (dist < 2.0) {
               return false;
            } else if (!(dist > this.range * this.range) && !(dist < this.minRange * this.minRange)) {
               this.positionProvider.setTarget(position);
               return true;
            }  else {
               this.positionProvider.clear();
               return false;
            }
         }
      }
   }

   private Vector3d getPostion(Ref<EntityStore> ref, Store<EntityStore> store) {
      FlockMembership membership = store.getComponent(ref, FlockMembership.getComponentType());
      assert membership != null;

      EntityGroup group = null;
      Ref<EntityStore> flockReference = membership.getFlockRef();
      if (flockReference != null && flockReference.isValid()) {
         group = store.getComponent(flockReference, EntityGroup.getComponentType());
      }
      assert (group != null ? group.getLeaderRef() : null) != null;

      PlayerRef playerRef = store.getComponent(group.getLeaderRef(), PlayerRef.getComponentType());
      assert playerRef != null;

      Ref<EntityStore> playerEntityRef = playerRef.getReference();
      assert playerEntityRef != null;

      EvokerComponent evoker = store.getComponent(playerEntityRef, EvokerComponent.getComponentType());

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
