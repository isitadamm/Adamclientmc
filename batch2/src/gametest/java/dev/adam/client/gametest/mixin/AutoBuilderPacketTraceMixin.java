package dev.adam.client.gametest.mixin;

import dev.adam.client.gametest.AutoBuilderPacketTrace;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Test-only interception: each regular movement and interaction is recorded once. */
@Mixin(ClientCommonPacketListenerImpl.class)
abstract class AutoBuilderPacketTraceMixin {
    @Inject(method="send",at=@At("HEAD"))
    private void adam$trace(Packet<?> packet, CallbackInfo ci) {
        // Teleport confirmation and its immediate PosRot use Connection.send directly in vanilla.
        // Excluding those legitimate correction responses keeps this guard about normal tick movement.
        AutoBuilderPacketTrace.sent((ClientCommonPacketListenerImpl)(Object)this,packet);
    }

    @Inject(method="onDisconnect",at=@At("HEAD"))
    private void adam$resetTrace(DisconnectionDetails details, CallbackInfo ci) {
        AutoBuilderPacketTrace.disconnected((ClientCommonPacketListenerImpl)(Object)this);
    }
}
