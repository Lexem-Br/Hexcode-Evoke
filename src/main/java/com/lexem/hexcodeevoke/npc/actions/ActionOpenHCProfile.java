package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionOpenHCProfile;
import com.lexem.hexcodeevoke.pages.HCProfilePage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ActionOpenHCProfile extends ActionBase {
   private String pageName = "HCProfilePage";
   private String cardName = "HCProfileSlotEntry";


   public ActionOpenHCProfile(@Nonnull BuilderActionOpenHCProfile builder, @Nonnull BuilderSupport support) {
      super(builder);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, role, sensorInfo, dt, store);

      HexCreatureComponent hexCreatureComponent = store.getComponent(ref, HexCreatureComponent.getComponentType());
      if (hexCreatureComponent == null) {return false;}

      Ref<EntityStore> refESPlayer = store.getExternalData().getRefFromUUID(UUID.fromString(hexCreatureComponent.getEvokerUUID()));
      if (refESPlayer == null) return false;

      PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
      if (playerRef == null) { return false; }

      Player player = store.getComponent(refESPlayer, Player.getComponentType());
      if (player == null) { return false; }

      HCProfilePage hcProfilePage = new HCProfilePage(playerRef, pageName, cardName);
      player.getPageManager().openCustomPage(refESPlayer, store, hcProfilePage);

      return true;
   }
}
