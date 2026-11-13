package dev.adam.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.adam.client.hotbar.HotbarBlur;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Changes renderer inputs only; the inventory and item-use packets retain the real items. */
@Mixin(ItemInHandRenderer.class)
abstract class HeldItemPrivacyMixin {
    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack adam$emptyHand(ItemStack original) {
        return HotbarBlur.hideHeldItems() ? ItemStack.EMPTY : original;
    }

    @ModifyExpressionValue(method = "renderHandsWithItems", at = @At(value = "FIELD", target =
            "Lnet/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection;renderMainHand:Z"))
    private boolean adam$showFist(boolean original) {
        return HotbarBlur.hideHeldItems() || original;
    }

    @ModifyExpressionValue(method = "renderHandsWithItems", at = @At(value = "FIELD", target =
            "Lnet/minecraft/client/renderer/ItemInHandRenderer$HandRenderSelection;renderOffHand:Z"))
    private boolean adam$hideOffhand(boolean original) {
        return !HotbarBlur.hideHeldItems() && original;
    }

    @ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/player/AbstractClientPlayer;isScoping()Z"))
    private boolean adam$keepEmptyArmVisible(boolean original) {
        return !HotbarBlur.hideHeldItems() && original;
    }
}
