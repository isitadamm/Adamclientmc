package dev.adam.client.mixin;

import dev.adam.client.freecam.FreecamController;
import dev.adam.client.autobuild.AutoBuilderController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void adam$clearActions(CallbackInfo ci) {
        if (FreecamController.enabled()) FreecamController.getInstance().suppressActions((Minecraft) (Object) this);
    }

    @Inject(method = {"startUseItem", "pickBlock", "continueAttack"}, at = @At("HEAD"), cancellable = true)
    private void adam$blockInteractions(CallbackInfo ci) {
        if (FreecamController.enabled() || AutoBuilderController.get().isRunning() || dev.adam.client.spectrebuilder.BuilderPlacement.placing()) ci.cancel();
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void adam$blockAttack(CallbackInfoReturnable<Boolean> cir) {
        if (FreecamController.enabled() || AutoBuilderController.get().isRunning() || dev.adam.client.spectrebuilder.BuilderPlacement.placing()) cir.setReturnValue(false);
    }
}
