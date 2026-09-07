package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionTargetPlayAnimation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ActionTargetPlayAnimation extends ActionBase {
   protected final BuilderActionTargetPlayAnimation.NPCAnimationSlot slot;
   protected String animationId;

   public ActionTargetPlayAnimation(@Nonnull BuilderActionTargetPlayAnimation builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.slot = builder.getSlot();
      this.animationId = builder.getAnimationId(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, executionSupport, sensorInfo, dt, store);
      if (sensorInfo == null || sensorInfo.getPositionProvider() == null) return false;

      Ref<EntityStore> npcTarget = sensorInfo.getPositionProvider().getTarget();
      if (npcTarget == null) return false;

      NPCEntity npcComponent = store.getComponent(npcTarget, Objects.requireNonNull(NPCEntity.getComponentType()));
      if (npcComponent == null) return false;

      npcComponent.playAnimation(npcTarget, this.slot.getMappedSlot(), this.animationId, store);
      return true;
   }
}
