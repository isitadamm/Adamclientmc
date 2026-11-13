package dev.adam.client.mixin;

import dev.adam.client.freecam.FreecamController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void adam$turnCamera(LocalPlayer player, double dx, double dy) {
        if (FreecamController.enabled()) FreecamController.getInstance().turn(dx, dy);
        else player.turn(dx, dy);
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void adam$preventHotbarScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (FreecamController.enabled() && Minecraft.getInstance().screen == null) ci.cancel();
    }
}
