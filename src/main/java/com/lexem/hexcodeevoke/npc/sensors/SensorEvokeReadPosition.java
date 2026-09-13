package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorEvokeReadPosition;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.UUID;

public class SensorEvokeReadPosition extends SensorBase {
   protected final double minRange;
   protected final double range;
   protected final PositionProvider positionProvider = new PositionProvider();
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   private static final double BASE_DISTANCE = 2.0;
   private static final double DISTANCE_INCREMENT = 0.5;

   public SensorEvokeReadPosition(@Nonnull BuilderSensorEvokeReadPosition builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.minRange = builder.getMinRange(support);
      this.range = builder.getRange(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, executionSupport, dt, store)) {
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

            if (isWithinAllowedDistance(qtdHexCreatures, dist)) {
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


   private boolean isWithinAllowedDistance(int qtdHexCreatures, double dist) {
      if (qtdHexCreatures == 1) {
         return dist < 0.1;
      }

      if (qtdHexCreatures >= 2) {
         double maxDistance = BASE_DISTANCE + (qtdHexCreatures - 2) * DISTANCE_INCREMENT;
         return dist < maxDistance;
      }

      return false;
   }

   private EvokerComponent getEvoker(Ref<EntityStore> ref, Store<EntityStore> store) {
      HexCreatureComponent hexCreatureComponent = store.getComponent(ref, HexCreatureComponent.getComponentType());
      if (hexCreatureComponent == null) {return null;}

      UUID playerUUID = UUID.fromString(hexCreatureComponent.getEvokerUUID());
      Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(playerUUID);
      if (playerRef == null) {return null;}

      return store.getComponent(playerRef, EvokerComponent.getComponentType());
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