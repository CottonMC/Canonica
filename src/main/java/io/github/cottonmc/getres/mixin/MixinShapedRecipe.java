package io.github.cottonmc.getres.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import io.github.cottonmc.getres.GetRes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.datafixer.NbtOps;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.JsonHelper;

@Mixin(ShapedRecipe.class)
public class MixinShapedRecipe {
	//TODO: nbt crafting support?
	@Inject(method = "getItemStack", at = @At("HEAD"), cancellable = true)
	private static void loadResource(JsonObject json, CallbackInfoReturnable<ItemStack> info) {
		if (json.has("resource")) {
			String resourceName = JsonHelper.getString(json, "resource");
			Item resource = GetRes.getExistingRes(resourceName);
			if (resource == Items.AIR) {
				throw new JsonSyntaxException("Unknown resource" + resourceName);
			}
			int count = JsonHelper.getInt(json, "count", 1);
			ItemStack stack = new ItemStack(resource, count);
			if (json.has("data")) {
				JsonObject data = JsonHelper.getObject(json, "data");
				Tag tag = Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, data);
				if (tag instanceof CompoundTag) {
					stack.setTag((CompoundTag)tag);
				}
			}
			info.setReturnValue(stack);
		}
	}
}
