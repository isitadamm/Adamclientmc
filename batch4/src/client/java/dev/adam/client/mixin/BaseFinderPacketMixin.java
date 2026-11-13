package dev.adam.client.mixin;

import dev.adam.client.chunkfinder.ChunkFinderScanner;
import dev.adam.client.amethyst.AmethystScanner;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Observe received chunks, light and block changes after vanilla has applied them. */
@Mixin(ClientPacketListener.class)
abstract class BaseFinderPacketMixin {
    @Inject(method = "handleLevelChunkWithLight", at = @At("RETURN"))
    private void adam$chunk(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        ChunkFinderScanner.packetApplied(packet);
        AmethystScanner.packetApplied(packet);
        ChunkFinderScanner.lightQueued(Minecraft.getInstance().level, packet.getX(), packet.getZ());
    }
    @Inject(method = "handleLightUpdatePacket", at = @At("RETURN"))
    private void adam$light(ClientboundLightUpdatePacket packet, CallbackInfo ci) {
        ChunkFinderScanner.lightQueued(Minecraft.getInstance().level, packet.getX(), packet.getZ());
    }
    @Inject(method = "handleBlockUpdate", at = @At("RETURN"))
    private void adam$block(ClientboundBlockUpdatePacket packet, CallbackInfo ci) { ChunkFinderScanner.packetApplied(packet); AmethystScanner.packetApplied(packet); }
    @Inject(method = "handleChunkBlocksUpdate", at = @At("RETURN"))
    private void adam$section(ClientboundSectionBlocksUpdatePacket packet, CallbackInfo ci) { ChunkFinderScanner.packetApplied(packet); AmethystScanner.packetApplied(packet); }
    @Inject(method = "handleForgetLevelChunk", at = @At("RETURN"))
    private void adam$forget(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) { ChunkFinderScanner.packetApplied(packet); AmethystScanner.packetApplied(packet); }
}
