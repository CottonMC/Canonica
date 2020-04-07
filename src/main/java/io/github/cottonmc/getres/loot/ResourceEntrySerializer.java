package io.github.cottonmc.getres.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.cottonmc.getres.GetRes;
import io.github.cottonmc.getres.mixin.ItemEntrySerializerAccessor;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class ResourceEntrySerializer extends LeafEntry.Serializer<ItemEntry> {
	private static final ItemEntry.Serializer serializer = new ItemEntry.Serializer();

	public ResourceEntrySerializer() {
		super(new Identifier(GetRes.MODID, "resource"), ItemEntry.class);
	}

	@Override
	protected ItemEntry fromJson(JsonObject entryJson, JsonDeserializationContext context, int weight, int quality, LootCondition[] conditions, LootFunction[] functions) {
		String resource = JsonHelper.getString(entryJson, "name");
		Item item = GetRes.getExistingRes(resource);
		if (item == Items.AIR) {
			throw new JsonSyntaxException("Unknown resource " + resource);
		}
		entryJson.addProperty("name", Registry.ITEM.getId(item).toString());
		return ((ItemEntrySerializerAccessor) serializer).invokeFromJson(entryJson, context, weight, quality, conditions, functions);
	}
}
