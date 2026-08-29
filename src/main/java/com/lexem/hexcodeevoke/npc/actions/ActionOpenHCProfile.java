package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionOpenHCProfile;
import com.lexem.hexcodeevoke.pages.HCProfilePage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionOpenHCProfile extends ActionBase {
    public ActionOpenHCProfile(@Nonnull BuilderActionOpenHCProfile builder) {
      super(builder);
   }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        Ref<EntityStore> refESPlayer = executionSupport.getStateSupport().getInteractionIterationTarget();
        if (refESPlayer == null || !refESPlayer.isValid()) return false;

        PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
        if (playerRef == null) return false;

        Player player = store.getComponent(refESPlayer, Player.getComponentType());
        if (player == null) return false;

        String cardName = "HCProfileSlotEntry";
        String pageName = "HCProfilePage";
        HCProfilePage hcProfilePage = new HCProfilePage(playerRef, npcRef, pageName, cardName);
        player.getPageManager().openCustomPage(refESPlayer, store, hcProfilePage);

        return true;
    }
}
