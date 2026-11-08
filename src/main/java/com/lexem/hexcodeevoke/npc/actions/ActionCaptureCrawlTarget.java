package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionCaptureCrawlTarget;
import com.lexem.hexcodeevoke.utils.SurfaceCrawlState;
import com.lexem.hexcodeevoke.utils.SurfaceNavigator;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ActionCaptureCrawlTarget extends ActionBase {
    protected final BuilderActionCaptureCrawlTarget.Target target;

    public ActionCaptureCrawlTarget(@Nonnull BuilderActionCaptureCrawlTarget builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.target = builder.getTarget();
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null) return false;

        SurfaceCrawlState crawl = hexCreature.getCrawlState();
        Ref<EntityStore> evokerRef = store.getExternalData().getRefFromUUID(UUID.fromString(hexCreature.getEvokerUUID()));
        if (evokerRef == null || !evokerRef.isValid()) {
            crawl.setGoal(null);
            return false;
        }

        crawl.setGoal(this.target == BuilderActionCaptureCrawlTarget.Target.Evoker
                ? this.evokerPosition(crawl, evokerRef, store)
                : this.wandTarget(evokerRef, store));
        return true;
    }

    @Nullable
    private Vector3d wandTarget(@Nonnull Ref<EntityStore> evokerRef, @Nonnull Store<EntityStore> store) {
        EvokerComponent evoker = store.getComponent(evokerRef, EvokerComponent.getComponentType());
        return evoker != null && !evoker.getTargetPosition().equals(Vector3dUtil.MIN) ? evoker.getTargetPosition() : null;
    }

    @Nullable
    private Vector3d evokerPosition(@Nonnull SurfaceCrawlState crawl, @Nonnull Ref<EntityStore> evokerRef, @Nonnull Store<EntityStore> store) {
        SurfaceNavigator.Node node = crawl.getCurrentNode();
        if (!crawl.isActive() || node == null || node.face() == SurfaceNavigator.UP) return null;

        TransformComponent evokerTransform = store.getComponent(evokerRef, TransformComponent.getComponentType());
        return evokerTransform != null ? evokerTransform.getPosition() : null;
    }
}
