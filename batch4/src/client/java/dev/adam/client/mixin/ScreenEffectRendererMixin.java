package dev.adam.client.mixin;

import dev.adam.client.freecam.FreecamController;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
abstract class ScreenEffectRendererMixin {
    @Inject(method = "renderScreenEffect", at = @At("HEAD"), cancellable = true)
    private void adam$hideBodyOverlays(CallbackInfo ci) {
        if (FreecamController.enabled()) ci.cancel();
    }
}
