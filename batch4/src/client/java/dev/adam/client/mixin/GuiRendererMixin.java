package dev.adam.client.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.adam.client.gui.GlassPanel;
import dev.adam.client.hotbar.HotbarBlur;
import dev.adam.client.namehider.ScoreboardBlur;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;

@Mixin(GuiRenderer.class)
abstract class GuiRendererMixin {
    @Shadow @Final private List<?> meshesToDraw;

    @Unique private int adam$hotbarDrawIndex = -1;
    @Unique private int adam$itemNameDrawIndex = -1;
    @Unique private int adam$scoreboardDrawIndex = -1;

    @Shadow
    private void executeDrawRange(Supplier<String> name, RenderTarget target, GpuBufferSlice fog,
                                  GpuBufferSlice transforms, GpuBuffer indices,
                                  VertexFormat.IndexType indexType, int start, int end) {
        throw new AssertionError();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void adam$prepareGlass(GpuBufferSlice fog, CallbackInfo ci) {
        adam$hotbarDrawIndex = -1;
        adam$itemNameDrawIndex = -1;
        adam$scoreboardDrawIndex = -1;
        HotbarBlur.beforeRender();
        ScoreboardBlur.beforeRender();
        GlassPanel.prepareScene();
    }

    @Inject(method = "addElementToMesh", at = @At("TAIL"))
    private void adam$recordHotbarBoundary(GuiElementRenderState state, CallbackInfo ci) {
        if (state.pipeline() == HotbarBlur.markerPipeline()) {
            // Changing to the marker pipeline flushes the preceding HUD element's last mesh.
            // The pending marker mesh becomes the next draw at this index.
            adam$hotbarDrawIndex = meshesToDraw.size();
        } else if (state.pipeline() == HotbarBlur.itemNameMarkerPipeline()) {
            adam$itemNameDrawIndex = meshesToDraw.size();
        } else if (state.pipeline() == ScoreboardBlur.markerPipeline()) {
            adam$scoreboardDrawIndex = meshesToDraw.size();
        }
    }

    @Redirect(method = "draw", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;II)V"))
    private void adam$blurAfterHotbar(GuiRenderer renderer, Supplier<String> name, RenderTarget target,
                                      GpuBufferSlice fog, GpuBufferSlice transforms, GpuBuffer indices,
                                      VertexFormat.IndexType indexType, int start, int end) {
        int cursor = start;
        int hotbar = adam$hotbarDrawIndex, itemName = adam$itemNameDrawIndex;
        if (hotbar >= cursor && hotbar < end) {
            if (cursor < hotbar) {
                executeDrawRange(name, target, fog, transforms, indices, indexType, cursor, hotbar);
            }
            // Finish the GUI pass before sampling its color attachment.
            HotbarBlur.apply(false);
            adam$hotbarDrawIndex = -1;
            cursor = hotbar;
        }
        if (itemName >= cursor && itemName < end) {
            if (cursor < itemName) executeDrawRange(name, target, fog, transforms, indices, indexType, cursor, itemName);
            HotbarBlur.apply(true);
            adam$itemNameDrawIndex = -1;
            cursor = itemName;
        }
        int scoreboard = adam$scoreboardDrawIndex;
        if (scoreboard >= cursor && scoreboard < end) {
            if (cursor < scoreboard) executeDrawRange(name, target, fog, transforms, indices, indexType, cursor, scoreboard);
            ScoreboardBlur.apply();
            adam$scoreboardDrawIndex = -1;
            cursor = scoreboard;
        }
        if (cursor < end) executeDrawRange(name, target, fog, transforms, indices, indexType, cursor, end);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void adam$closeGlass(CallbackInfo ci) {
        HotbarBlur.close();
        ScoreboardBlur.close();
        GlassPanel.close();
    }
}
