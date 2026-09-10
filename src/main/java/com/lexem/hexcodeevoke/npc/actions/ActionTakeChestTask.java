package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionTakeChestTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class ActionTakeChestTask extends ActionBase {
    protected ChestTaskComponent.TaskType taskType;
    private static final Random RANDOM = new Random();

    public ActionTakeChestTask(@Nonnull BuilderActionTakeChestTask builder) {
      super(builder);
   }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (minionComponent == null) return false;

        UUID uuid = UUID.fromString(minionComponent.getOwnerUUID());
        Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(uuid);
        if (hcRef == null) return false;

        HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null) return false;

        ChestTaskComponent[] allTasks = hexCreatureComponent.getListChestTask();
        if (allTasks == null || allTasks.length == 0) return false;

        List<ChestTaskComponent> validTasks = new ArrayList<>();
        for (ChestTaskComponent task : allTasks) {
            if (task != null && !task.isFinished()) {
                if (this.taskType == null || task.getTaskType() == this.taskType) {
                    validTasks.add(task);
                }
            }
        }

        if (validTasks.isEmpty()) return false;

        ChestTaskComponent selectedTask = validTasks.get(RANDOM.nextInt(validTasks.size()));

        minionComponent.setChestTask(selectedTask);
        hexCreatureComponent.removeChestTask(selectedTask);

        return true;
    }
}