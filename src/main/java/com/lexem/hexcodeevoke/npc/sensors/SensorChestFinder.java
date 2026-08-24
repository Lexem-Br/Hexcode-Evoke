package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorChestFinder;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import com.lexem.hexcodeevoke.utils.InventoryUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class SensorChestFinder extends SensorBase {
    private final double horizontalRange;
    private final double verticalRange;
    private boolean checkCanStore;
    protected boolean wasSteering = false;
    private Ref<EntityStore> npcRef;
    private Store<EntityStore> store;
    private final PositionProvider positionProvider = new PositionProvider();
    public SensorChestFinder(@Nonnull BuilderSensorChestFinder builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.horizontalRange = builder.getHorizontalRange(support);
        this.verticalRange = builder.getVerticalRange(support);
        this.checkCanStore = builder.getCheckCanStore(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(npcRef, executionSupport, dt, store) || wasSteering) {
            this.positionProvider.clear();
            return false;
        }

        this.npcRef = npcRef;
        this.store = store;

        World world = store.getExternalData().getWorld();
        TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (transformComponent == null) {
            this.positionProvider.clear();
            return false;
        }

        Vector3i chest = FinderUtils.findNearestBlockBFS(
                transformComponent.getPosition(),
                (int) Math.ceil(horizontalRange),
                (int) Math.ceil(verticalRange),
                chestValidator,
                world
        );

        if (chest != null) {
            this.positionProvider.setTarget(new Vector3d(chest.x, chest.y, chest.z));
        } else {
            this.positionProvider.clear();
        }

        return chest != null;
    }

    private final FinderUtils.BlockValidator<World> chestValidator = (block, world) -> {
        long chunkIndex = ChunkUtil.indexChunkFromBlock(block.x, block.z);
        Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);
        if (chunkRef == null) return false;

        Store<ChunkStore> chunkComponentStore = world.getChunkStore().getStore();
        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(block.x, block.y, block.z);
        if (sectionRef == null) return false;

        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(chunkComponentStore, sectionRef, block.x, block.y, block.z);
        if (blockRef == null) return false;

        ItemContainerBlock itemContainerBlock = chunkComponentStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        if (itemContainerBlock == null) {
            return false;
        } else if (!checkCanStore) {
            return true;
        }

        SimpleItemContainer chestContainer = itemContainerBlock.getItemContainer();
        return InventoryUtils.canAddAnyItemToContainerNPC(chestContainer, npcRef, store);
    };

    public InfoProvider getSensorInfo() { return this.positionProvider; }
}
