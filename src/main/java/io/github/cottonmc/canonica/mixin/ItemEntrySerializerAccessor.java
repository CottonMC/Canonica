package io.github.cottonmc.canonica.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunction;

@Mixin(ItemEntry.Serializer.class)
public interface ItemEntrySerializerAccessor {
	@Invoker
	ItemEntry invokeFromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions);
}
