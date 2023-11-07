/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.recipes.ingredients;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import net.dries007.tfc.util.Helpers;

public record FluidIngredient(List<IngredientType.Entry<Fluid>> entries) implements IngredientType<Fluid>
{
    private static final Factory<Fluid, FluidIngredient> FACTORY = new Factory<>("fluid", ForgeRegistries.FLUIDS, FluidTag::new, FluidIngredient::new);

    public static FluidIngredient fromJson(JsonElement json)
    {
        return IngredientType.fromJson(json, FACTORY);
    }

    public static FluidIngredient fromNetwork(FriendlyByteBuf buffer)
    {
        return IngredientType.fromNetwork(buffer, FACTORY);
    }

    public Collection<Fluid> fluids()
    {
        return all().toList();
    }

    public JsonElement toJson()
    {
        return IngredientType.toJson(this, FACTORY);
    }

    public void toNetwork(FriendlyByteBuf buffer)
    {
        IngredientType.toNetwork(buffer, this, FACTORY);
    }

    private record FluidTag(TagKey<Fluid> tag) implements TagEntry<Fluid>
    {
        @Override
        public Stream<Fluid> stream()
        {
            return Helpers.streamAllTagValues(tag, ForgeRegistries.FLUIDS);
        }

        @Override
        public boolean test(Fluid fluid)
        {
            return Helpers.isFluid(fluid, tag);
        }
    }
}
