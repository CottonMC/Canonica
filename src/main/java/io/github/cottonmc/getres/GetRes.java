package io.github.cottonmc.getres;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.loot.v1.LootEntryTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import io.github.cottonmc.cotton.datapack.tags.TagEntryManager;
import io.github.cottonmc.cotton.datapack.tags.TagType;
import io.github.cottonmc.getres.config.CanonNamespaces;
import io.github.cottonmc.getres.loot.ResourceEntrySerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GetRes implements ModInitializer {
	public static final String MODID = "getres";

	public static final Logger logger = LogManager.getLogger();

	//TODO: random resource
	public static final ItemGroup GETRES_GROUP = FabricItemGroupBuilder.build(new Identifier(MODID, "resources"), () -> new ItemStack(Items.DIAMOND));

	private static CanonNamespaces canonNames;

	private static final Map<String, Item> canonItems = new HashMap<>();

	@Override
	public void onInitialize() {
		if (canonNames == null) {
			canonNames = loadCanon();
		}
		LootEntryTypeRegistry.INSTANCE.register(new ResourceEntrySerializer());
	}

	public static Item getResource(String resourceName, String defaultId) {
		if (canonNames == null) {
			canonNames = loadCanon();
		}
		Item ret = canonItems.computeIfAbsent(resourceName, name -> registerItem(new Identifier(name, resourceName)));
		canonNames.itemNamespaces.put(resourceName, defaultId);
		saveCanon(canonNames);
		return ret;
	}

	public static Item getResource(String resourceName, Block defaultBlock) {
		if (canonNames == null) {
			canonNames = loadCanon();
		}
		Item ret = canonItems.computeIfAbsent(resourceName, name -> registerBlockItem(defaultBlock));
		canonNames.blockItemNamespaces.put(resourceName, Registry.BLOCK.getId(defaultBlock).toString());
		saveCanon(canonNames);
		return ret;
	}

	public static Item getExistingRes(String resourceName) {
		return canonItems.getOrDefault(resourceName, Items.AIR);
	}

	private static Item registerItem(Identifier name) {
		Item ret = Registry.register(Registry.ITEM, name, new Item(new Item.Settings().group(GETRES_GROUP)));
		Identifier tagId = new Identifier("c", name.getPath() + "s"); //TODO: better way to pluralize?
		TagEntryManager.registerToTag(TagType.ITEM, tagId, name.toString());
		return ret;
	}

	private static Item registerBlockItem(Block block) {
		Identifier name = Registry.BLOCK.getId(block);
		Item ret = Registry.register(Registry.ITEM, name, new BlockItem(block, new Item.Settings().group(GETRES_GROUP)));
		Identifier tagId = new Identifier("c", name.getPath() + "s"); //TODO: better way to pluralize?
		TagEntryManager.registerToTag(TagType.ITEM, tagId, name.toString());
		return ret;
	}

	private static CanonNamespaces loadCanon() {
		try {
			Jankson jankson = Jankson.builder().build();
			File file = FabricLoader.getInstance().getConfigDirectory().toPath().resolve("getres.json5").toFile();
			if (!file.exists()) saveCanon(new CanonNamespaces());
			JsonObject json = jankson.load(file);
			CanonNamespaces result =  jankson.fromJson(json, CanonNamespaces.class);
			JsonElement jsonElementNew = jankson.toJson(new CanonNamespaces());
			if(jsonElementNew instanceof JsonObject){
				JsonObject jsonNew = (JsonObject) jsonElementNew;
				if(json.getDelta(jsonNew).size()>= 0){
					saveCanon(result);
				}
			}
			for (String name : result.itemNamespaces.keySet()) {
				Item item = registerItem(new Identifier(result.itemNamespaces.get(name), name));
				canonItems.put(name, item);
			}
			//TODO: is this a load order risk?
			for (String name : result.blockItemNamespaces.keySet()) {
				Block block = Registry.BLOCK.get(new Identifier(result.blockItemNamespaces.get(name)));
				if (block == Blocks.AIR) throw new RuntimeException("Tell B0undary to fix the block item load order issue!");
				Item item = registerBlockItem(block);
				canonItems.put(name, item);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("GetRes could not resource canon! It can't help other mods register their resources!", e);
		}
	}

	private static void saveCanon(CanonNamespaces canon) {
		try {
			Jankson jankson = Jankson.builder().build();
			File file = FabricLoader.getInstance().getConfigDirectory().toPath().resolve("getres.json5").toFile();
			JsonElement json = jankson.toJson(canon);
			String result = json.toJson(true, true);
			if (!file.exists()) file.createNewFile();
			FileOutputStream out = new FileOutputStream(file,false);
			out.write(result.getBytes());
			out.flush();
			out.close();
		} catch (Exception e) {
			logger.error("[GetRes] Error saving resource canon: {}", e.getMessage());
		}
	}
}
