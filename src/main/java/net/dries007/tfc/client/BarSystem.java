/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client;

import java.util.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.egg.EggCapability;
import net.dries007.tfc.common.capabilities.egg.IEgg;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;


/**
 * Should only be used in cases where anything could have this bar.
 * If you control the items, use the methods on the Item class
 */
public final class BarSystem
{
    private static final BiMap<ResourceLocation, Bar> REGISTRY = HashBiMap.create();

    public static void registerDefaultBars()
    {
        register(Helpers.identifier("heat"), new Bar() {
            @Override
            public int getBarColor(ItemStack stack)
            {
                return stack.getCapability(HeatCapability.CAPABILITY).map(cap -> {
                    if (cap.getTemperature() > 0)
                    {
                        final Heat heat = Heat.getHeat(cap.getTemperature());
                        if (heat != null)
                        {
                            return Objects.requireNonNull(heat.getColor().getColor());
                        }
                    }
                    return 0;
                }).orElse(0);
            }

            @Override
            public boolean isBarVisible(ItemStack stack)
            {
                return TFCConfig.CLIENT.displayItemHeatBars.get() && stack.getCapability(HeatCapability.CAPABILITY).map(cap -> cap.getTemperature() > 0).orElse(false);
            }

            @Override
            public int getBarWidth(ItemStack stack)
            {
                return stack.getCapability(HeatCapability.CAPABILITY).map(cap -> {
                    final HeatingRecipe def = HeatingRecipe.getRecipe(stack);
                    if (def != null)
                    {
                        return Mth.clamp(Math.round(13f * cap.getTemperature() / def.getTemperature()), 1, 13);
                    }
                    return Mth.clamp(Math.round(13f * cap.getTemperature() / Heat.maxVisibleTemperature()), 1, 13);
                }).orElse(0);
            }

            @Override
            public ItemStack createDefaultItem(ItemStack stack)
            {
                stack.getCapability(HeatCapability.CAPABILITY).ifPresent(cap -> cap.setTemperature(0));
                return stack;
            }
        });

        register(Helpers.identifier("egg"), new Bar() {
            @Override
            public int getBarColor(ItemStack stack)
            {
                return Objects.requireNonNull(ChatFormatting.GOLD.getColor());
            }

            @Override
            public boolean isBarVisible(ItemStack stack)
            {
                return stack.getCapability(EggCapability.CAPABILITY).map(cap -> cap.getHatchDay() != 0).orElse(false);
            }

            @Override
            public int getBarWidth(ItemStack stack)
            {
                final int maxDays = 8;
                return stack.getCapability(EggCapability.CAPABILITY).map(cap -> {
                    final int incubationDays = maxDays - Mth.clamp((int) (cap.getHatchDay() - Calendars.CLIENT.getTotalDays()), 0, maxDays);
                    return Math.round(13f * incubationDays / maxDays);
                }).orElse(0);
            }

            @Override
            public ItemStack createDefaultItem(ItemStack stack)
            {
                stack.getCapability(EggCapability.CAPABILITY).ifPresent(IEgg::removeFertilization);
                return stack;
            }
        });
    }

    public static synchronized Bar register(ResourceLocation id, Bar bar)
    {
        if (REGISTRY.containsKey(id))
        {
            throw new IllegalArgumentException("Duplicate key: " + id);
        }
        REGISTRY.put(id, bar);
        return bar;
    }

    @Nullable
    public static Bar getCustomBar(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return null;
        }
        for (Bar bar : REGISTRY.values())
        {
            if (bar.isBarVisible(stack) && (bar.overridesOtherBars() || !bar.createDefaultItem(stack.copy()).isBarVisible()))
            {
                return bar;
            }
        }
        return null;
    }

    public interface Bar
    {
        /**
         * @return an {@code int} color. This is compatible with {@linkplain net.minecraft.ChatFormatting}
         */
        int getBarColor(ItemStack stack);

        /**
         * @return {@code true if the bar should render}
         */
        boolean isBarVisible(ItemStack stack);

        /**
         * @return {@code int} in range [0, 13]
         */
        int getBarWidth(ItemStack stack);

        /**
         * On a copy of the item stack, reverses changes that would have caused this bar to show up.
         * Example: for the 'heat' bar, set the item's heat to zero.
         */
        ItemStack createDefaultItem(ItemStack stack);

        /**
         * @return {@code true} if the bar should ignore bars added by vanilla/other mods
         */
        default boolean overridesOtherBars()
        {
            return false;
        }
    }
}
