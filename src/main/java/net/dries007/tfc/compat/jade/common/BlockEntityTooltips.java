/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.compat.jade.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.common.blockentities.*;
import net.dries007.tfc.common.blocks.BloomBlock;
import net.dries007.tfc.common.blocks.TFCCandleBlock;
import net.dries007.tfc.common.blocks.TFCCandleCakeBlock;
import net.dries007.tfc.common.blocks.TFCTorchBlock;
import net.dries007.tfc.common.blocks.TFCWallTorchBlock;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.dries007.tfc.common.blocks.crop.DecayingBlock;
import net.dries007.tfc.common.blocks.devices.*;
import net.dries007.tfc.common.blocks.plant.fruit.*;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.common.blocks.wood.TFCLoomBlock;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.egg.EggCapability;
import net.dries007.tfc.common.capabilities.egg.IEgg;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.recipes.BarrelRecipe;
import net.dries007.tfc.common.recipes.BloomeryRecipe;
import net.dries007.tfc.common.recipes.LoomRecipe;
import net.dries007.tfc.common.recipes.SealedBarrelRecipe;
import net.dries007.tfc.common.recipes.SoupPotRecipe;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.LampFuel;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;

/**
 * Common tooltips that can be displayed for various block entities via external sources.
 */
public final class BlockEntityTooltips
{
    public static void register(RegisterCallback<BlockEntityTooltip, Block> callback)
    {
        callback.register("barrel", BARREL, BarrelBlock.class);
        callback.register("bellows", BELLOWS, BellowsBlock.class);
        callback.register("sapling", SAPLING, TFCSaplingBlock.class);
        callback.register("blast_furnace", BLAST_FURNACE, BlastFurnaceBlock.class);
        callback.register("bloomery", BLOOMERY, BloomeryBlock.class);
        callback.register("bloom", BLOOM, BloomBlock.class);
        callback.register("charcoal_forge", CHARCOAL_FORGE, CharcoalForgeBlock.class);
        callback.register("composter", COMPOSTER, TFCComposterBlock.class);
        callback.register("crop", CROP, CropBlock.class);
        callback.register("crucible", CRUCIBLE, CrucibleBlock.class);
        callback.register("firepit", FIREPIT, FirepitBlock.class);
        callback.register("fruit_tree_sapling", FRUIT_TREE_SAPLING, FruitTreeSaplingBlock.class);
        callback.register("hoe_overlay", HOE_OVERLAY, Block.class);
        callback.register("lamp", LAMP, LampBlock.class);
        callback.register("nest_box", NEST_BOX, NestBoxBlock.class);
        callback.register("pit_kiln_internal", PIT_KILN_INTERNAL, PitKilnBlock.class);
        callback.register("pit_kiln_above", PIT_KILN_ABOVE, FireBlock.class);
        callback.register("powder_keg", POWDER_KEG, PowderkegBlock.class);
        callback.register("torch", TORCH, TFCTorchBlock.class);
        callback.register("wall_torch", TORCH, TFCWallTorchBlock.class);
        callback.register("candle", CANDLE, TFCCandleBlock.class);
        callback.register("candle_cake", CANDLE, TFCCandleCakeBlock.class);
        callback.register("jack_o_lantern", JACK_O_LANTERN, JackOLanternBlock.class);
        callback.register("mud_bricks", MUD_BRICKS, DryingBricksBlock.class);
        callback.register("decaying", DECAYING, DecayingBlock.class);
        callback.register("loom", LOOM, TFCLoomBlock.class);
        callback.register("sheet_pile", SHEET_PILE, SheetPileBlock.class);
        callback.register("ingot_pile", INGOT_PILE, IngotPileBlock.class);
    }

    public static final BlockEntityTooltip INGOT_PILE = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof IngotPileBlockEntity pile)
        {
            pile.fillTooltip(tooltip);
        }
    };

    public static final BlockEntityTooltip SHEET_PILE = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof SheetPileBlockEntity pile)
        {
            pile.fillTooltip(tooltip);
        }
    };

    public static final BlockEntityTooltip BARREL = (level, state, pos, entity, tooltip) -> {
        if (state.getBlock() instanceof BarrelBlock && entity instanceof BarrelBlockEntity barrel)
        {
            if (state.getValue(BarrelBlock.SEALED))
            {
                BarrelRecipe recipe = barrel.getRecipe();
                if (recipe != null)
                {
                    tooltip.accept(recipe.getTranslationComponent());
                    // this is the translation key used in the barrel class, if that changes we should change it in barrel screen too.
                    tooltip.accept(Helpers.translatable("tfc.jade.sealed_date", ICalendar.getTimeAndDate(Calendars.get(level).ticksToCalendarTicks(barrel.getSealedTick()), Calendars.get(level).getCalendarDaysInMonth())));
                    if (recipe instanceof SealedBarrelRecipe sealedRecipe)
                    {
                        timeLeft(level, tooltip, sealedRecipe.getDuration() - (Calendars.get(level).getTicks() - barrel.getRecipeTick()));
                    }
                }
            }
        }
    };

    public static final BlockEntityTooltip BELLOWS = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof BellowsBlockEntity bellows)
        {
            int pushTicks = bellows.getTicksSincePushed();
            if (pushTicks < 20 && pushTicks > 0)
            {
                if (pushTicks > 10)
                {
                    pushTicks = 20 - pushTicks;
                }

                tooltip.accept(Helpers.translatable("tfc.jade.bellows_" + pushTicks));
            }
        }
    };

    public static final BlockEntityTooltip BLAST_FURNACE = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof BlastFurnaceBlockEntity furnace)
        {
            furnace.getCapability(HeatCapability.BLOCK_CAPABILITY).ifPresent(cap -> heat(tooltip, cap.getTemperature()));

            tooltip.accept(Helpers.translatable("tfc.jade.input_stacks", furnace.getInputCount()));
            tooltip.accept(Helpers.translatable("tfc.jade.catalyst_stacks", furnace.getCatalystCount()));
            tooltip.accept(Helpers.translatable("tfc.jade.fuel_stacks", furnace.getFuelCount()));
        }
    };

    public static final BlockEntityTooltip BLOOMERY = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof BloomeryBlockEntity bloomery && state.getBlock() instanceof BloomeryBlock)
        {
            tooltip.accept(Helpers.translatable("tfc.jade.input_stacks", bloomery.getInputStacks().size()));
            tooltip.accept(Helpers.translatable("tfc.jade.catalyst_stacks", bloomery.getCatalystStacks().size()));
            if (state.getValue(BloomeryBlock.LIT))
            {
                final long ticksLeft = bloomery.getRemainingTicks();
                if (ticksLeft > 0)
                {
                    final BloomeryRecipe recipe = bloomery.getCachedRecipe();
                    if (recipe != null)
                    {
                        timeLeft(level, tooltip, ticksLeft);
                        tooltip.accept(Helpers.translatable("tfc.jade.creating", recipe.getResultItem(level.registryAccess()).getHoverName()));
                    }
                }
            }
        }
    };

    public static final BlockEntityTooltip BLOOM = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof BloomBlockEntity bloom)
        {
            final ItemStack item = bloom.getItem();

            ItemStack displayItem = item.copy();
            displayItem.setCount(bloom.getCount());
            itemWithCount(tooltip, displayItem);

            final List<Component> text = new ArrayList<>();
            item.getCapability(HeatCapability.CAPABILITY).ifPresent(cap -> cap.addTooltipInfo(item, text));
            text.forEach(tooltip);
        }
    };

    public static final BlockEntityTooltip CHARCOAL_FORGE = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof CharcoalForgeBlockEntity forge)
        {
            heat(tooltip, forge.getTemperature());
        }
    };

    public static final BlockEntityTooltip COMPOSTER = (level, state, pos, entity, tooltip) -> {
        if (state.getBlock() instanceof TFCComposterBlock block && entity instanceof ComposterBlockEntity composter)
        {
            if (composter.isRotten())
            {
                tooltip.accept(Helpers.translatable("tfc.composter.rotten"));
            }
            else
            {
                if (!composter.isReady() && state.getValue(TFCComposterBlock.STAGE) == 8)
                {
                    timeLeft(level, tooltip, composter.getReadyTicks() - composter.getTicksSinceUpdate());
                }
            }
        }
    };

    public static final BlockEntityTooltip CROP = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof CropBlockEntity crop && state.getBlock() instanceof CropBlock block)
        {
            tooltip.accept(Helpers.translatable("tfc.jade.yield", String.format("%.0f", crop.getYield() * 100)));
        }
    };

    public static final BlockEntityTooltip CRUCIBLE = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof CrucibleBlockEntity crucible)
        {
            crucible.getCapability(HeatCapability.BLOCK_CAPABILITY).ifPresent(cap -> heat(tooltip, cap.getTemperature()));
        }
    };

    public static final BlockEntityTooltip FIREPIT = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof AbstractFirepitBlockEntity<?> firepit)
        {
            heat(tooltip, firepit.getTemperature());

            if (state.hasProperty(FirepitBlock.SMOKE_LEVEL))
            {
                tooltip.accept(Helpers.translatable("tfc.jade.smoke_level", state.getValue(FirepitBlock.SMOKE_LEVEL)));
            }
            if (firepit instanceof PotBlockEntity pot)
            {
                if (pot.shouldRenderAsBoiling())
                {
                    tooltip.accept(Helpers.translatable("tfc.tooltip.pot_boiling"));
                }
                else if (pot.getOutput() != null && !pot.getOutput().isEmpty())
                {
                    tooltip.accept(Helpers.translatable("tfc.tooltip.pot_finished"));

                    if (pot.getOutput() instanceof SoupPotRecipe.SoupOutput soup)
                    {
                        final ItemStack stack = soup.stack();
                        itemWithCount(tooltip, stack);

                        final List<Component> text = new ArrayList<>();
                        stack.getCapability(FoodCapability.CAPABILITY).ifPresent(cap -> cap.addTooltipInfo(stack, text));
                        text.forEach(tooltip);
                    }
                }
            }
        }
    };

    public static final BlockEntityTooltip FRUIT_TREE_SAPLING = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof TickCounterBlockEntity counter && state.getBlock() instanceof FruitTreeSaplingBlock sapling)
        {
            timeLeft(level, tooltip, (long) (sapling.getTreeGrowthDays() * ICalendar.TICKS_IN_DAY * TFCConfig.SERVER.globalFruitSaplingGrowthModifier.get()) - counter.getTicksSinceUpdate(), Helpers.translatable("tfc.jade.ready_to_grow"));
        }
    };

    public static final BlockEntityTooltip HOE_OVERLAY = (level, state, pos, entity, tooltip) -> {
        if (state.getBlock() instanceof HoeOverlayBlock overlay)
        {
            if (TFCConfig.CLIENT.showHoeOverlaysInInfoMods.get() && entity != null)
            {
                final List<Component> text = new ArrayList<>();
                final BlockPos pos1 = entity.getBlockPos();
                overlay.addHoeOverlayInfo(level, pos1, level.getBlockState(pos1), text, false);
                text.forEach(tooltip);
            }
        }
    };

    public static final BlockEntityTooltip LAMP = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof LampBlockEntity lamp && state.getBlock() instanceof LampBlock)
        {
            final LampFuel fuel = lamp.getFuel();
            if (fuel != null)
            {
                if (fuel.getBurnRate() != -1)
                {
                    tooltip.accept(Helpers.translatable("tfc.jade.burn_rate", fuel.getBurnRate()));
                }
                if (state.getValue(LampBlock.LIT))
                {
                    if (fuel.getBurnRate() == -1)
                    {
                        tooltip.accept(Helpers.translatable("tfc.jade.burn_forever"));
                    }
                    else
                    {
                        lamp.getCapability(Capabilities.FLUID).ifPresent(cap -> {
                            final int fluid = cap.getFluidInTank(0).getAmount();
                            if (fluid > 0)
                            {
                                // ticks / mB * mB = ticks
                                timeLeft(level, tooltip, (long) fluid * fuel.getBurnRate());
                            }
                        });
                    }

                }

            }
        }
    };

    public static final BlockEntityTooltip NEST_BOX = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof NestBoxBlockEntity nest)
        {
            final List<Component> text = new ArrayList<>();
            nest.getCapability(Capabilities.ITEM).ifPresent(inventory -> {
                for (int i = 0; i < inventory.getSlots(); i++)
                {
                    ItemStack stack = inventory.getStackInSlot(i);
                    stack.getCapability(EggCapability.CAPABILITY).filter(IEgg::isFertilized).ifPresent(egg -> {
                        text.add(stack.getHoverName());
                        egg.addTooltipInfo(text);
                    });
                }
            });
            text.forEach(tooltip);
        }
    };

    public static final BlockEntityTooltip PIT_KILN_INTERNAL = (level, state, pos, entity, tooltip) -> pitKiln(level, pos, tooltip);
    public static final BlockEntityTooltip PIT_KILN_ABOVE = (level, state, pos, entity, tooltip) -> pitKiln(level, pos.below(), tooltip);

    public static final BlockEntityTooltip POWDER_KEG = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof PowderkegBlockEntity keg)
        {
            tooltip.accept(Helpers.translatable("tfc.jade.explosion_strength", PowderkegBlockEntity.getStrength(keg)));
        }
    };

    public static final BlockEntityTooltip SAPLING = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof TickCounterBlockEntity counter && state.getBlock() instanceof TFCSaplingBlock sapling)
        {
            timeLeft(level, tooltip, (long) (sapling.getDaysToGrow() * ICalendar.TICKS_IN_DAY * TFCConfig.SERVER.globalSaplingGrowthModifier.get()) - counter.getTicksSinceUpdate(), Helpers.translatable("tfc.jade.ready_to_grow"));
        }
    };

    public static final BlockEntityTooltip TORCH = tickCounter(TFCConfig.SERVER.torchTicks);

    public static final BlockEntityTooltip CANDLE = tickCounter(TFCConfig.SERVER.candleTicks);

    public static final BlockEntityTooltip JACK_O_LANTERN = tickCounter(TFCConfig.SERVER.jackOLanternTicks);

    public static final BlockEntityTooltip MUD_BRICKS = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof TickCounterBlockEntity counter && state.getBlock() instanceof DryingBricksBlock)
        {
            if (state.getValue(DryingBricksBlock.DRIED))
            {
                tooltip.accept(Helpers.translatable("tfc.jade.dried_mud_bricks"));
            }
            else
            {
                if (level.isRainingAt(entity.getBlockPos().above()))
                {
                    tooltip.accept(Helpers.translatable("tfc.jade.raining_mud_bricks").withStyle(ChatFormatting.BLUE));
                }
                else
                {
                    timeLeft(level, tooltip, TFCConfig.SERVER.mudBricksTicks.get() - counter.getTicksSinceUpdate(), Helpers.translatable("tfc.jade.mud_bricks_nearly_done"));
                }
            }
        }
    };

    public static final BlockEntityTooltip DECAYING = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof DecayingBlockEntity decaying)
        {
            final ItemStack stack = decaying.getStack();
            tooltip.accept(stack.getHoverName());
            final List<Component> text = new ArrayList<>();
            stack.getCapability(FoodCapability.CAPABILITY).ifPresent(cap -> cap.addTooltipInfo(stack, text));
            text.forEach(tooltip);
        }
    };

    public static final BlockEntityTooltip LOOM = (level, state, pos, entity, tooltip) -> {
        if (entity instanceof LoomBlockEntity loom)
        {
            final LoomRecipe recipe = loom.getRecipe();
            if (recipe != null)
            {
                tooltip.accept(Helpers.translatable("tfc.jade.loom_progress", loom.getProgress(), recipe.getStepCount(), recipe.getResultItem(level.registryAccess()).getDisplayName()));
            }
        }
    };

    private static void pitKiln(Level level, BlockPos pos, Consumer<Component> tooltip)
    {
        final BlockState state = level.getBlockState(pos);
        if (level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln && state.getBlock() instanceof PitKilnBlock)
        {
            if (state.getValue(PitKilnBlock.STAGE) == PitKilnBlock.LIT)
            {
                timeLeft(level, tooltip, kiln.getTicksLeft());
            }
            else
            {
                tooltip.accept(Helpers.translatable("tfc.jade.straws", kiln.getStraws().stream().filter(s -> !s.isEmpty()).toList().size()));
                tooltip.accept(Helpers.translatable("tfc.jade.logs", kiln.getLogs().stream().filter(s -> !s.isEmpty()).toList().size()));
            }
        }
    }

    public static void itemWithCount(Consumer<Component> tooltip, ItemStack stack)
    {
        tooltip.accept(Helpers.literal(String.valueOf(stack.getCount())).append("x ").append(stack.getHoverName()));
    }

    public static void heat(Consumer<Component> tooltip, float temperature)
    {
        final MutableComponent heat = TFCConfig.CLIENT.heatTooltipStyle.get().formatColored(temperature);
        if (heat != null)
        {
            tooltip.accept(heat);
        }
    }

    public static void timeLeft(Level level, Consumer<Component> tooltip, long ticks)
    {
        timeLeft(level, tooltip, ticks, null);
    }

    public static void timeLeft(Level level, Consumer<Component> tooltip, long ticks, @Nullable Component ifNegative)
    {
        if (ticks > 0)
        {
            tooltip.accept(Helpers.translatable("tfc.jade.time_left", Calendars.get(level).getTimeDelta(ticks)));
        }
        else if (ifNegative != null)
        {
            tooltip.accept(ifNegative);
        }
    }

    public static BlockEntityTooltip tickCounter(Supplier<Integer> totalTicks)
    {
        return (level, state, pos, entity, tooltip) -> {
            if (entity instanceof TickCounterBlockEntity counter)
            {
                timeLeft(level, tooltip, totalTicks.get() - counter.getTicksSinceUpdate());
            }
        };
    }

}
