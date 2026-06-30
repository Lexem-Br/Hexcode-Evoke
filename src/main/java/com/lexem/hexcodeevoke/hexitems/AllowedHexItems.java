package com.lexem.hexcodeevoke.hexitems;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;

public class AllowedHexItems {

    public static final BuilderCodec<AllowedHexItems> CODEC =
        BuilderCodec.builder(AllowedHexItems.class, AllowedHexItems::new)
            .append(
                new KeyedCodec<>(
                    "AllowedHexItems",
                    new ArrayCodec<>(
                        BuilderCodec.builder(HexItem.class, HexItem::new)
                            .append(new KeyedCodec<>("BlockId", Codec.STRING), (item, s) -> item.blockId = s, item -> item.blockId)
                            .addValidator(Validators.nonNull())
                            .add()
                            .append(new KeyedCodec<>("EntityId", Codec.STRING), (item, s) -> item.entityId = s, item -> item.entityId)
                            .addValidator(Validators.nonNull())
                            .add()
                            .build(),
                        HexItem[]::new
                    )
                ),
                (allowedHexItems, objects) -> allowedHexItems.hexItems = objects,
                allowedHexItems -> allowedHexItems.hexItems
            )
            .add()
            .build();

    public AllowedHexItems() {
    }

    public static class HexItem {
        public String blockId = "";
        public String entityId = "";

        public HexItem() {
        }

        public HexItem(String blockId, String entityId) {
            this.blockId = blockId;
            this.entityId = entityId;
        }
    }

    public HexItem[] hexItems = new HexItem[] {
            new HexItem("Hex_Mannequin_Block", "Hex_Mannequin"),
            new HexItem("Hex_Fairy_Block", "Hex_Fairy"),
            new HexItem("Hex_Fairy_Void_Block", "Hex_Fairy_Void"),
            new HexItem("Hex_Fairy_Ice_Block", "Hex_Fairy_Ice"),
            new HexItem("Alive_Homunculus_Jar", "Hex_Homunculus")
    };

    public String[] getHexblockIds() {
        if (hexItems == null || hexItems.length == 0) {
            return new String[0];
        }

        String[] hexblockIdsList = new String[hexItems.length];
        for (int i = 0; i < hexItems.length; i++) {
            HexItem item = hexItems[i];
            hexblockIdsList[i] = (item != null) ? item.blockId : null;
        }

        return hexblockIdsList;
    }

}
