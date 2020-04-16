package io.github.cottonmc.canonica.mixin;

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

@Mixin(CuttingRecipe.Serializer.class)
public class MixinCuttingRecipeSerializer<T extends CuttingRecipe> {
	@Shadow @Final CuttingRecipe.Serializer.RecipeFactory<T> recipeFactory;

	@Inject(method = "read(Lnet/minecraft/util/Identifier;Lcom/google/gson/JsonObject;)Lnet/minecraft/recipe/CuttingRecipe;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JsonHelper;getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;", ordinal = 1),
			locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void injectResultParse(Identifier id, JsonObject json, CallbackInfoReturnable<T> info, String group, Ingredient ingredient) {
		if (json.get("result") instanceof JsonObject) {
			JsonObject result = json.getAsJsonObject("result");
			ItemStack stack = ShapedRecipe.getItemStack(result);
			info.setReturnValue(this.recipeFactory.create(id, group, ingredient, stack));
		}
	}
}
