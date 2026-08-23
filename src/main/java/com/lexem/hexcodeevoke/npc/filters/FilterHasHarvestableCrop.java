package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.HarvestingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterHasHarvestableCrop;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorHarvestableCropFinder;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FilterHasHarvestableCrop extends EntityFilterBase {
    private final double horizontalRange;
    private final double verticalRange;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public FilterHasHarvestableCrop(@Nonnull BuilderFilterHasHarvestableCrop builder, @Nonnull BuilderSupport support) {
       this.horizontalRange = builder.getHorizontalRange(support);
       this.verticalRange = builder.getVerticalRange(support);
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
       World world = store.getExternalData().getWorld();
       TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
       if (transformComponent == null) return false;

       Vector3i crop = FinderUtils.findNearestBlockBFS(
               transformComponent.getPosition(),
               (int) Math.ceil(horizontalRange),
               (int) Math.ceil(verticalRange),
               cropValidator,
               world
       );

       return crop != null;
   }

    private final FinderUtils.BlockValidator<World> cropValidator = (block, world) -> {
        BlockType blockType = world.getBlockType(block);
        if (blockType == null || blockType.getId() == null || blockType.getGathering() == null) {
            return false;
        }

        FarmingData farmingData = blockType.getFarming();
        if (farmingData != null && farmingData.getStages() != null) {
            HarvestingDropType harvestingDropType = blockType.getGathering().getHarvest();
            return harvestingDropType != null;
        }

        return false;
    };

   @Override
   public int cost() {
      return 100;
   }
}
