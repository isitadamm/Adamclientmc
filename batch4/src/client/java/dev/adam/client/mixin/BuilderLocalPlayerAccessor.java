package dev.adam.client.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read vanilla's last transmitted state; the builder never sends an extra movement packet. */
@Mixin(LocalPlayer.class)
public interface BuilderLocalPlayerAccessor {
    @Accessor("yRotLast") float adam$lastSentYaw();
    @Accessor("xRotLast") float adam$lastSentPitch();
    @Accessor("lastSentInput") Input adam$lastSentInput();
}
