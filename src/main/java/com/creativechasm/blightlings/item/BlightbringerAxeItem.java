package com.creativechasm.blightlings.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.AxeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class BlightbringerAxeItem extends AxeItem
{
    public BlightbringerAxeItem(IItemTier tier, float attackDamageIn, float attackSpeedIn, Properties builder) {
        super(tier, attackDamageIn, attackSpeedIn, builder);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent(getTranslationKey(stack).replace("item", "tooltip")).func_230530_a_(Style.field_240709_b_.func_240712_a_(TextFormatting.GRAY)));
        tooltip.add(new StringTextComponent(" "));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
