package com.lexem.hexcodeevoke.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class HCProfilePageEvent {
    public static final BuilderCodec<HCProfilePageEvent> CODEC = BuilderCodec.builder(
                    HCProfilePageEvent.class,
                    HCProfilePageEvent::new)
            .addField(new KeyedCodec<>("Close", Codec.STRING),
                    (entry, s) -> entry.close = s, entry -> entry.close)
            .addField(new KeyedCodec<>("Take", Codec.STRING),
                    (entry, s) -> entry.take = s, entry -> entry.take)
            .addField(new KeyedCodec<>("InventorySelect", Codec.STRING),
                    (entry, s) -> entry.inventorySelect = s, entry -> entry.inventorySelect)
            .addField(new KeyedCodec<>("ClearPrimary", Codec.STRING),
                    (entry, s) -> entry.clearPrimary = s, entry -> entry.clearPrimary)
            .addField(new KeyedCodec<>("ClearSecondary", Codec.STRING),
                    (entry, s) -> entry.clearSecondary = s, entry -> entry.clearSecondary)
            .addField(new KeyedCodec<>("NameColor", Codec.STRING),
                    (entry, s) -> entry.nameColor = s, entry -> entry.nameColor)
            .addField(new KeyedCodec<>("GlowColor", Codec.STRING),
                    (entry, s) -> entry.glowColor = s, entry -> entry.glowColor)
            .addField(new KeyedCodec<>("NameInput", Codec.STRING),
                    (entry, s) -> entry.nameInputTrigger = s, entry -> entry.nameInputTrigger)
            .addField(new KeyedCodec<>("@NameInput", Codec.STRING),
                    (entry, s) -> entry.nameInput = s, entry -> entry.nameInput)
            .build();

    public String close;
    public String take;
    public String inventorySelect;
    public String clearPrimary;
    public String clearSecondary;
    public String nameColor;
    public String glowColor;
    public String nameInputTrigger;
    public String nameInput;

    public HCProfilePageEvent() {
    }
}
