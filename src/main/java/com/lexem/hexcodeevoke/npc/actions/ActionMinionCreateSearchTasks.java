package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionMinionCreateSearchTasks;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ActionMinionCreateSearchTasks extends ActionBase {
   private final double horizontalRange;
   private final double verticalRange;

   public ActionMinionCreateSearchTasks(@Nonnull BuilderActionMinionCreateSearchTasks builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.horizontalRange = builder.getHorizontalRange(support);
      this.verticalRange = builder.getVerticalRange(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(npcRef, executionSupport, sensorInfo, dt, store);

      World world = store.getExternalData().getWorld();

      HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
      if (minionComponent == null) return false;

      UUID uuid = UUID.fromString(minionComponent.getOwnerUUID());
      Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(uuid);
      if (hcRef == null) return false;

      HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
      if (hexCreatureComponent == null) return false;

      TransformComponent transformComponent = store.getComponent(hcRef, TransformComponent.getComponentType());
      if (transformComponent == null) return false;

      List<Vector3d> listChestsPosition = FinderUtils.findAllChestsInRange(
              transformComponent.getPosition(),
              (int) Math.ceil(horizontalRange),
              (int) Math.ceil(verticalRange),
              world
      );
      if (listChestsPosition.isEmpty()) return false;

      for (Vector3d chestPosition : listChestsPosition) {
         ChestTaskComponent chestTaskComponent = new ChestTaskComponent(
                 ChestTaskComponent.TaskType.Search,
                 chestPosition,
                 minionComponent.getChestTask().getItemId(),
                 minionComponent.getChestTask().getItemQuantity(),
                 false
         );

         hexCreatureComponent.addChestTask(chestTaskComponent);
      }

      return true;
   }
}