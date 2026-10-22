package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionWithDelay;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionHCPickUpItem;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ActionHCPickUpItem extends ActionWithDelay {
   protected static final ComponentType<EntityStore, ItemComponent> ITEM_COMPONENT_TYPE = ItemComponent.getComponentType();
   protected static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
   protected final double range;
   protected final boolean hoover;
   protected final boolean skipHandSlot;
   @Nullable
   protected final List<String> hooverItems;

   public ActionHCPickUpItem(@Nonnull BuilderActionHCPickUpItem builder, @Nonnull BuilderSupport support) {
      super(builder, support);
      this.range = builder.getRange(support);
      this.hoover = builder.getHoover();
      this.skipHandSlot = builder.getSkipHandSlot();
      String[] items = builder.getItems(support);
      this.hooverItems = items != null ? List.of(items) : null;
   }

   @Override
   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.canExecute(ref, executionSupport, sensorInfo, dt, store)) {
         return false;
      } else {
         if (!this.hoover) {
            if (sensorInfo == null || !sensorInfo.hasPosition()) {
               return false;
            }

            Ref<EntityStore> targetRef = sensorInfo.getPositionProvider().getTarget();
            if (targetRef == null) {
               return false;
            }

            ItemComponent itemComponent = store.getComponent(targetRef, ITEM_COMPONENT_TYPE);
            if (itemComponent == null || !itemComponent.canPickUp()) {
               return false;
            }

            TransformComponent selfTransformComponent = store.getComponent(ref, TRANSFORM_COMPONENT_TYPE);

            assert selfTransformComponent != null;

            Vector3d selfPosition = selfTransformComponent.getPosition();
            TransformComponent targetTransformComponent = store.getComponent(targetRef, TRANSFORM_COMPONENT_TYPE);

            assert targetTransformComponent != null;

            Vector3d targetPosition = targetTransformComponent.getPosition();
            double distanceSquared = selfPosition.distanceSquared(targetPosition);
            if (distanceSquared > this.range * this.range) {
               return false;
            }
         } else if (executionSupport.getPositionCache().getDroppedItemList().isEmpty()) {
            return false;
         }

         return !this.isDelaying();
      }
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(ref, executionSupport, sensorInfo, dt, store);
      Ref<EntityStore> itemRef = null;
      if (!this.hoover) {
         if (sensorInfo != null) {
            itemRef = sensorInfo.getPositionProvider().getTarget();
         }
      } else {
         itemRef = executionSupport.getPositionCache().getClosestDroppedItemInRange(ref, 0.0, this.range, ActionHCPickUpItem::filterItem, this, store);
      }

      this.prepareDelay();
      this.startDelay(executionSupport.getEntitySupport());
      if (itemRef == null) {
         return false;
      } else {
         if (this.skipHandSlot) {
            addOutsideHand(ref, itemRef, store);
            return true;
         }

         ItemContainer hotbarContainer = Objects.requireNonNull(store.getComponent(ref, InventoryComponent.Hotbar.getComponentType())).getInventory();
         if (hotbarContainer != null && hotbarContainer.getCapacity() > 0) {
            ItemStack itemStack = hotbarContainer.getItemStack((short) 0);
            if (ItemStack.isEmpty(itemStack)) {
               ItemComponent.addToItemContainer(store, itemRef, hotbarContainer);
            } else {
               ItemContainer storageContainer = Objects.requireNonNull(store.getComponent(ref, InventoryComponent.Storage.getComponentType())).getInventory();
               if (storageContainer != null) {
                  ItemComponent.addToItemContainer(store, itemRef, storageContainer);
               }
            }
         } else {
            ItemContainer storageContainer = Objects.requireNonNull(store.getComponent(ref, InventoryComponent.Storage.getComponentType())).getInventory();
            if (storageContainer != null) {
                ItemComponent.addToItemContainer(store, itemRef, storageContainer);
            }
         }

         return true;
      }
   }

   private static void addOutsideHand(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> itemRef, @Nonnull Store<EntityStore> store) {
      if (!itemRef.isValid()) return;

      ItemComponent itemComponent = store.getComponent(itemRef, ITEM_COMPONENT_TYPE);
      if (itemComponent == null || !itemComponent.canPickUp()) return;

      ItemStack itemStack = itemComponent.getItemStack();
      if (ItemStack.isEmpty(itemStack)) return;

      InventoryComponent.Hotbar hotbar = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
      InventoryComponent.Storage storage = store.getComponent(ref, InventoryComponent.Storage.getComponentType());
      ItemContainer hotbarInventory = hotbar != null ? hotbar.getInventory() : null;
      ItemContainer storageInventory = storage != null ? storage.getInventory() : null;

      ItemStack remainder = itemStack;
      for (boolean mergeOnly : new boolean[]{true, false}) {
         remainder = addToSlots(hotbarInventory, 1, remainder, mergeOnly);
         remainder = addToSlots(storageInventory, 0, remainder, mergeOnly);
      }

      if (ItemStack.isEmpty(remainder)) {
         store.removeEntity(itemRef, RemoveReason.REMOVE);
      } else if (remainder.getQuantity() != itemStack.getQuantity()) {
         itemComponent.setItemStack(remainder);
      }
   }

   @Nullable
   private static ItemStack addToSlots(@Nullable ItemContainer inventory, int firstSlot, @Nullable ItemStack itemStack, boolean mergeOnly) {
      if (inventory == null || ItemStack.isEmpty(itemStack)) return itemStack;

      ItemStack remainder = itemStack;
      for (short slot = (short) firstSlot; slot < inventory.getCapacity() && !ItemStack.isEmpty(remainder); slot++) {
         ItemStack current = inventory.getItemStack(slot);
         boolean empty = ItemStack.isEmpty(current);
         if (mergeOnly && (empty || !current.getItemId().equals(remainder.getItemId()))) continue;
         if (!mergeOnly && !empty) continue;

         remainder = inventory.addItemStackToSlot(slot, remainder).getRemainder();
      }
      return remainder;
   }

   protected boolean filterItem(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!ref.isValid()) {
         return false;
      } else if (this.hooverItems == null) {
         return true;
      } else {
         ItemComponent itemComponent = componentAccessor.getComponent(ref, ITEM_COMPONENT_TYPE);

         assert itemComponent != null;

         return InventoryHelper.matchesItem(this.hooverItems, Objects.requireNonNull(itemComponent.getItemStack()));
      }
   }
}
