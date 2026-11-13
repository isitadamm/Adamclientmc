package dev.adam.client.mixin;

import dev.adam.client.staff.StaffDetectorController;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** RETURN runs after vanilla applies the packet on its client-thread dispatch. */
@Mixin(ClientPacketListener.class)
abstract class StaffDetectorPacketMixin {
    @Inject(method = {"handlePlayerInfoUpdate", "handlePlayerInfoRemove"}, at = @At("RETURN"))
    private void adam$staffRoster(CallbackInfo ci) {
        StaffDetectorController.rosterApplied((ClientPacketListener) (Object) this);
    }
    @Inject(method = "handleCommandSuggestions", at = @At("RETURN"))
    private void adam$staffSuggestions(ClientboundCommandSuggestionsPacket packet, CallbackInfo ci) {
        StaffDetectorController.suggestionsApplied((ClientPacketListener) (Object) this, packet);
    }
}
