package com.lexem.hexcodeevoke.npc.actions.voidarena;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.lexem.hexcodeevoke.npc.actions.voidarena.builders.BuilderActionInterruptAttack;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cancels the attack the NPC is running and clears its attack pause, so the next Attack action starts right away.
 * The vanilla Attack action waits while another attack chain or its pause is still running.
 */
public class ActionInterruptAttack extends ActionBase {

    public ActionInterruptAttack(@Nonnull BuilderActionInterruptAttack builder) {
        super(builder);
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, executionSupport, sensorInfo, dt, store);

        InteractionManager interactionManager = store.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
        if (interactionManager != null) {
            List<InteractionChain> running = new ArrayList<>();
            for (InteractionChain chain : interactionManager.getChains().values()) {
                if (chain.getServerState() == InteractionState.NotFinished) running.add(chain);
            }
            for (InteractionChain chain : running) {
                interactionManager.cancelChains(chain);
            }
        }
        executionSupport.getCombatSupport().setExecutingAttack(null, false, 0.0);
        return true;
    }
}
