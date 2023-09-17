package com.focamacho.ringsofascension.item;

import com.focamacho.ringsofascension.client.GlintRenderTypes;
import com.focamacho.ringsofascension.init.ModItems;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class ItemRingBase extends Item {

    protected String tooltip;
    public final boolean isEnabled;
    public final GlintRenderTypes glintType;

    public ItemRingBase(Properties properties, String tooltip, boolean enabled, GlintRenderTypes glintType) {
        super(properties.stacksTo(1));
        this.tooltip = tooltip;
        this.isEnabled = enabled;
        this.glintType = glintType;
        ModItems.allRings.add(this);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    public void tickCurio(String identifier, int index, LivingEntity livingEntity){}

    public Multimap<Attribute, AttributeModifier> curioModifiers(ItemStack stack, String identifier){
        return HashMultimap.create();
    }

    public void onEquippedCurio(String identifier, LivingEntity livingEntity){}

    public void onUnequippedCurio(String identifier, LivingEntity livingEntity){}

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<ICurio> lazyCurio = LazyOptional.of(()-> new ICurio() {

                @Override
                public ItemStack getStack() {
                    return stack;
                }

                @Override
                public void curioTick(SlotContext slotContext) {
                    tickCurio(slotContext.identifier(), slotContext.index(), slotContext.entity());
                }

                @NotNull
                @Override
                public SoundInfo getEquipSound(SlotContext slotContext) {
                    return new SoundInfo(SoundEvents.ARMOR_EQUIP_GOLD, 1.0f, 1.0f);
                }

                @Override
                public void onEquip(SlotContext slotContext, ItemStack prevStack) {
                    onEquippedCurio(slotContext.identifier(), slotContext.entity());
                }

                @NotNull
                @Override
                public DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit) {
                    return DropRule.DEFAULT;
                }

                @Override
                public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                    onUnequippedCurio(slotContext.identifier(), slotContext.entity());
                }

                @Override
                public boolean canEquipFromUse(SlotContext slotContext) {
                    return true;
                }

                @Override
                public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
                    return curioModifiers(stack, slotContext.identifier());
                }

                @Override
                public List<Component> getSlotsTooltip(List<Component> tt) {
                    List<Component> tooltips = ICurio.super.getAttributesTooltip(tt);
                    return replaceTooltips(tooltips);
                }

                @Override
                public List<Component> getAttributesTooltip(List<Component> tt) {
                    List<Component> tooltips = ICurio.super.getAttributesTooltip(tt);
                    return replaceTooltips(tooltips);
                }

            });

            @Override
            public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction side) {
                return CuriosCapability.ITEM.orEmpty(capability, lazyCurio);
            }
        };
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if(this.tooltip == null) return;

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal(ChatFormatting.GRAY + Component.translatable("tooltip.ringsofascension.worn").getString()));
        tooltip.add(Component.literal(ChatFormatting.BLUE + Component.translatable(this.tooltip).getString()));
    }

    private static List<Component> replaceTooltips(List<Component> tooltips) {
        tooltips.replaceAll(tooltip ->
                changeColors(tooltip, TextColor.fromLegacyFormat(ChatFormatting.GOLD), TextColor.fromLegacyFormat(ChatFormatting.GRAY))
        );
        return tooltips;
    }

    private static Component changeColors(Component component, TextColor from, TextColor to) {
        MutableComponent mutable = component.copy();

        if(Objects.equals(mutable.getStyle().getColor(), from))
            mutable.setStyle(mutable.getStyle().withColor(to));

        mutable.getSiblings().replaceAll(component1 -> changeColors(component1, from, to));

        return mutable;
    }

}
