package com.lexem.hexcodeevoke.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

public class ChestDataComponent {
   protected String chestDataId;
   public final Vector3d chestPosition = new Vector3d();
   private String[] listItemsId = new String[0];

   private static ComponentType<EntityStore, HexCreatureComponent> TYPE;

   public static void setComponentType(ComponentType<EntityStore, HexCreatureComponent> type) {
      TYPE = type;
   }

   public static ComponentType<EntityStore, HexCreatureComponent> getComponentType() {
      return TYPE;
   }

   public static final Codec<ChestDataComponent> CODEC = BuilderCodec.builder(ChestDataComponent.class, ChestDataComponent::new)
      .append(
              new KeyedCodec<>("Id", Codec.STRING),
              (itemStack, id) -> itemStack.chestDataId = id,
              itemStack -> itemStack.chestDataId)
      .addValidator(Validators.nonNull())
      .addValidator(Item.VALIDATOR_CACHE.getValidator().late())
      .add()
      .append(
              new KeyedCodec<>("ChestPosition", Vector3dUtil.CODEC),
              (component, v) -> component.chestPosition.set(v),
              component -> component.chestPosition)
      .add()
      .append(
             new KeyedCodec<>("ListItemsId",  Codec.STRING_ARRAY),
             (component, value) -> component.listItemsId = value,
             component -> component.listItemsId
      ).add()
      .build();

   public ChestDataComponent() {
   }

}
