package io.github.cottonmc.canonica;

import java.io.File;
import java.io.FileOutputStream;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import io.github.cottonmc.canonica.config.ResourceConfig;
import io.github.cottonmc.canonica.loot.ResourceEntrySerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v1.LootEntryTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;

public class Canonica implements ModInitializer {
	public static final String MODID = "canonica";

	public static final Logger logger = LogManager.getLogger();

	private static ResourceConfig config;

	@Override
	public void onInitialize() {
		if (config == null) {
			config = loadConfig();
		}
		LootEntryTypeRegistry.INSTANCE.register(new ResourceEntrySerializer());
	}

	//TODO: What do we do with stuff like nether ores or mods that register multiple things to the same tag? Have extra tags for nether/end ores that get appended in?
	public static Item getCanonicalItem(Tag<Item> tag) {
		Item ret = Items.AIR;
		int currentPref = -1;
		for (Item item : tag.values()) {
			Identifier id = Registry.ITEM.getId(item);
			String namespace = id.getNamespace();
			int index = config.namespacePreference.indexOf(namespace);
			if (index == -1) {
				config.namespacePreference.add(namespace);
				saveConfig(config);
				index = config.namespacePreference.indexOf(namespace);
			}
			if (ret == Items.AIR) {
				ret = item;
				currentPref = index;
			} else {
				if (currentPref > index) {
					ret = item;
					currentPref = index;
				}
			}
		}
		return ret;
	}

	private static ResourceConfig loadConfig() {
		try {
			Jankson jankson = Jankson.builder().build();
			File file = FabricLoader.getInstance().getConfigDirectory().toPath().resolve("canonica.json5").toFile();
			if (!file.exists()) saveConfig(new ResourceConfig());
			JsonObject json = jankson.load(file);
			ResourceConfig result =  jankson.fromJson(json, ResourceConfig.class);
			JsonElement jsonElementNew = jankson.toJson(new ResourceConfig());
			if (jsonElementNew instanceof JsonObject) {
				JsonObject jsonNew = (JsonObject) jsonElementNew;
				if (json.getDelta(jsonNew).size() > 0) {
					saveConfig(result);
				}
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Canonica could not load config! It can't apply its changes!", e);
		}
	}

	private static void saveConfig(ResourceConfig config) {
		try {
			Jankson jankson = Jankson.builder().build();
			File file = FabricLoader.getInstance().getConfigDirectory().toPath().resolve("canonica.json5").toFile();
			JsonElement json = jankson.toJson(config);
			String result = json.toJson(true, true);
			if (!file.exists()) file.createNewFile();
			FileOutputStream out = new FileOutputStream(file,false);
			out.write(result.getBytes());
			out.flush();
			out.close();
		} catch (Exception e) {
			logger.error("[Canonica] Error saving config: {}", e.getMessage());
		}
	}
}
