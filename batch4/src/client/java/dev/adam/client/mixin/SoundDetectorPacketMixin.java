package dev.adam.client.mixin;

import dev.adam.client.sound.SoundCapture;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** RETURN capture follows vanilla thread handoff; HEAD only marks a main-thread dispatch scope. */
@Mixin(ClientPacketListener.class)
abstract class SoundDetectorPacketMixin {
    @Inject(method = {"handleSoundEvent", "handleSoundEntityEvent", "handleBlockEvent", "handleLevelEvent", "handleBlockDestruction",
            "handleEntityEvent", "handleDamageEvent", "handleExplosion", "handleStopSoundEvent"}, at = @At("HEAD"))
    private void adam$soundEnter(CallbackInfo ci) { SoundCapture.enterPacket(); }

    @Inject(method = "handleSoundEvent", at = @At("RETURN"))
    private void adam$sound(ClientboundSoundPacket packet, CallbackInfo ci) { SoundCapture.leavePacket(); SoundCapture.sound(packet); }
    @Inject(method = "handleSoundEntityEvent", at = @At("RETURN"))
    private void adam$entitySound(ClientboundSoundEntityPacket packet, CallbackInfo ci) { SoundCapture.leavePacket(); SoundCapture.sound(packet); }
    @Inject(method = "handleBlockEvent", at = @At("RETURN"))
    private void adam$blockSound(ClientboundBlockEventPacket packet, CallbackInfo ci) { SoundCapture.leavePacket(); SoundCapture.block(packet); }
    @Inject(method = "handleLevelEvent", at = @At("RETURN"))
    private void adam$levelSound(ClientboundLevelEventPacket packet, CallbackInfo ci) { SoundCapture.leavePacket(); SoundCapture.level(packet); }
    @Inject(method = "handleBlockDestruction", at = @At("RETURN"))
    private void adam$miningSound(ClientboundBlockDestructionPacket packet, CallbackInfo ci) { SoundCapture.leavePacket(); SoundCapture.mining(packet); }
    @Inject(method = "handleEntityEvent", at = @At("RETURN"))
    private void adam$statusSound(ClientboundEntityEventPacket packet, CallbackInfo ci) { SoundCapture.leavePacket(); SoundCapture.entityEvent(packet); }
    @Inject(method = "handleDamageEvent", at = @At("RETURN"))
    private void adam$damageSound(ClientboundDamageEventPacket packet, CallbackInfo ci) { SoundCapture.leavePacket(); SoundCapture.damage(packet); }
    @Inject(method = "handleExplosion", at = @At("RETURN"))
    private void adam$explosionSound(ClientboundExplodePacket packet, CallbackInfo ci) { SoundCapture.leavePacket(); SoundCapture.explosion(packet); }
    @Inject(method = "handleStopSoundEvent", at = @At("RETURN"))
    private void adam$stopSound(ClientboundStopSoundPacket packet, CallbackInfo ci) { SoundCapture.leavePacket(); SoundCapture.stop(packet); }
    @Inject(method = "handleForgetLevelChunk", at = @At("RETURN"))
    private void adam$soundForget(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        SoundCapture.forgetChunk(packet.pos().x, packet.pos().z);
    }
    @Inject(method = "handleLevelChunkWithLight", at = @At("RETURN"))
    private void adam$soundChunk(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        SoundCapture.forgetChunk(packet.getX(), packet.getZ());
    }
}
