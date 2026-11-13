package dev.adam.client.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientLevel.class)
public interface BuilderClientLevelAccessor {
    @Invoker("getBlockStatePredictionHandler")
    BlockStatePredictionHandler adam$predictionHandler();
}
