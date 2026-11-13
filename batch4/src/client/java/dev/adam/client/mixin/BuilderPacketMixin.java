package dev.adam.client.mixin;

import dev.adam.client.autobuild.AutoBuilderController;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only packets received from the server can acknowledge builder interactions. */
@Mixin(ClientPacketListener.class)
abstract class BuilderPacketMixin {
    @Inject(method = "handleBlockUpdate", at = @At("RETURN"))
    private void adam$block(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
        AutoBuilderController.get().serverBlockUpdate(packet.getPos(), packet.getBlockState());
    }
    @Inject(method = "handleChunkBlocksUpdate", at = @At("RETURN"))
    private void adam$section(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) {
        packet.runUpdates(AutoBuilderController.get()::serverBlockUpdate);
    }
    @Inject(method = "handleBlockChangedAck", at = @At("RETURN"))
    private void adam$ack(ClientboundBlockChangedAckPacket packet, CallbackInfo ci) {
        AutoBuilderController.get().serverAcknowledged(packet.sequence());
    }
    @Inject(method = "handleMovePlayer", at = @At("RETURN"))
    private void adam$correction(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        AutoBuilderController.get().positionCorrected();
    }
}
