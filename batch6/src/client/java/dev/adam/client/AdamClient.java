package dev.adam.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.adam.client.config.ClientConfig;
import dev.adam.client.config.ConfigStore;
import dev.adam.client.chunkfinder.ChunkFinderScanner;
import dev.adam.client.amethyst.AmethystScanner;
import dev.adam.client.blockesp.BlockEspController;
import dev.adam.client.gui.BlockEspHud;
import dev.adam.client.gui.BaseFinderPanel;
import dev.adam.client.fullbright.FullbrightController;
import dev.adam.client.freecam.FreecamController;
import dev.adam.client.fly.FlyController;
import dev.adam.client.namehider.NameHiderController;
import dev.adam.client.hotbar.HotbarBlur;
import dev.adam.client.gui.AdamScreen;
import dev.adam.client.gui.GlassPanel;
import dev.adam.client.gui.ModuleArrayList;
import dev.adam.client.input.ModuleKeybinds;
import dev.adam.client.render.HighlightRenderer;
import dev.adam.client.autobuild.AutoBuilderController;
import dev.adam.client.gui.AutoBuilderScreen;
import dev.adam.client.gui.AutoBuilderHud;
import dev.adam.client.gui.ProgressBarHud;
import dev.adam.client.progress.ProgressTimers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class AdamClient implements ClientModInitializer {
    public static final String MOD_ID = "adam";
    private ConfigStore store;
    private ClientConfig config;
    private ChunkFinderScanner chunkScanner;
    private AmethystScanner amethystScanner;
    private dev.adam.client.light.LightDebugScanner lightDebug;
    private dev.adam.client.sound.SoundDetectorController soundDetector;
    private BlockEspController blockEsp;
    private dev.adam.client.staff.StaffDetectorController staffDetector;

    @Override
    public void onInitializeClient() {
        GlassPanel.initialize();
        ProgressBarHud.initialize();
        store = new ConfigStore(FabricLoader.getInstance().getConfigDir().resolve("adam-client.json"));
        config = store.load();
        new HotbarBlur(config);
        new NameHiderController(config);
        chunkScanner = new ChunkFinderScanner(config);
        amethystScanner = new AmethystScanner(config);
        lightDebug = new dev.adam.client.light.LightDebugScanner(config);
        soundDetector = new dev.adam.client.sound.SoundDetectorController(config);
        blockEsp = new BlockEspController(config);
        staffDetector = new dev.adam.client.staff.StaffDetectorController(config, this::save);
        ClientTickEvents.END_CLIENT_TICK.register(auto -> {
            dev.adam.client.spectrebuilder.BuilderPlacement.tick(auto);
            dev.adam.client.spectrebuilder.CreativeBuilder.INSTANCE.tick(auto);
        });
        FullbrightController fullbright = new FullbrightController(config);
        ClientTickEvents.START_CLIENT_TICK.register(fullbright::tick);
        FreecamController freecam = new FreecamController(config);
        ClientTickEvents.START_CLIENT_TICK.register(freecam::tick);
        FlyController fly = new FlyController(config);
        ClientTickEvents.START_CLIENT_TICK.register(fly::tick);
        ServerTickEvents.START_SERVER_TICK.register(fly::serverTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(fly::serverStopping);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> fly.serverDisconnected(server, handler.player));
        AutoBuilderController autoBuilder = AutoBuilderController.get();
        ClientTickEvents.START_CLIENT_TICK.register(autoBuilder::tick);
        HighlightRenderer renderer = new HighlightRenderer(config, chunkScanner, amethystScanner, lightDebug, blockEsp);
        renderer.register();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "client"));
        KeyMapping open = bind("open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT, category);
        KeyMapping camera = bind("toggle_freecam", GLFW.GLFW_KEY_F6, category);
        KeyMapping builder = bind("open_autobuilder", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping chunks = bind("toggle_chunk_finder", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping bright = bind("toggle_fullbright", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping flight = bind("toggle_fly", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping progress = bind("toggle_progress_bar", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping names = bind("toggle_name_hider", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping hotbar = bind("toggle_hotbar", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping amethyst = bind("toggle_amethyst", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping light = bind("toggle_light_debug", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping sounds = bind("toggle_sound_detector", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping blocks = bind("toggle_block_esp", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping staff = bind("toggle_staff_detector", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping acknowledgeSound = bind("acknowledge_sound", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping muteSound = bind("mute_sound", InputConstants.UNKNOWN.getValue(), category);
        KeyMapping soundHistory = bind("sound_history", InputConstants.UNKNOWN.getValue(), category);
        ModuleKeybinds.initialize(camera, builder, chunks, bright, flight, progress, names, hotbar, amethyst, light, sounds, blocks, staff);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ProgressTimers.tick(config);
            boolean inputBlocked = ModuleKeybinds.inputBlocked(client);
            while (open.consumeClick()) {
                if (!inputBlocked && client.screen == null) client.setScreen(new AdamScreen(config, this::save));
            }
            if (ModuleKeybinds.consumePress(client, 2)) {
                if (!inputBlocked && client.screen == null && client.player != null) freecam.setEnabled(!freecam.isActive());
            }
            if (ModuleKeybinds.consumePress(client, 3)) {
                if (!inputBlocked && client.screen == null && client.player != null)
                    client.setScreen(new AutoBuilderScreen(null, config, this::save));
            }
            if (ModuleKeybinds.consumePress(client, 4)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.chunkFinder.enabled = !config.chunkFinder.enabled;
                    save();
                }
            }
            if (ModuleKeybinds.consumePress(client, 5)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.fullbright.enabled = !config.fullbright.enabled;
                    save();
                }
            }
            if (ModuleKeybinds.consumePress(client, 6)) {
                if (!inputBlocked && client.screen == null && client.player != null) fly.setEnabled(!fly.isActive());
            }
            if (ModuleKeybinds.consumePress(client, 7)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.progressBar.enabled = !config.progressBar.enabled;
                    save();
                }
            }
            if (ModuleKeybinds.consumePress(client, 8)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.nameHider.enabled = !config.nameHider.enabled;
                    save();
                }
            }
            if (ModuleKeybinds.consumePress(client, 9)) {
                if (!inputBlocked && client.screen == null && client.player != null) {
                    config.hotbar.enabled = !config.hotbar.enabled;
                    save();
                }
            }
            fly.tick(client);
            chunkScanner.tick(client);
            if (ModuleKeybinds.consumePress(client, 10) && !inputBlocked && client.screen == null && client.player != null) {
                config.amethyst.enabled = !config.amethyst.enabled;
                save();
            }
            amethystScanner.tick(client);
            if (ModuleKeybinds.consumePress(client, 11) && !inputBlocked && client.screen == null && client.player != null) {
                config.lightDebug.enabled = !config.lightDebug.enabled; save();
            }
            lightDebug.tick(client);
            if (ModuleKeybinds.consumePress(client, 12) && !inputBlocked && client.screen == null && client.player != null) {
                config.soundDetector.enabled = !config.soundDetector.enabled; save();
            }
            while (acknowledgeSound.consumeClick()) if (!inputBlocked) soundDetector.acknowledge();
            while (muteSound.consumeClick()) if (!inputBlocked) soundDetector.mute(60);
            while (soundHistory.consumeClick()) if (!inputBlocked && client.screen == null && client.player != null)
                client.setScreen(new dev.adam.client.gui.SoundHistoryScreen(null, config));
            soundDetector.tick(client);
            if (ModuleKeybinds.consumePress(client, 13) && !inputBlocked && client.screen == null && client.player != null) {
                config.blockEsp.enabled = !config.blockEsp.enabled; save();
            }
            blockEsp.tick(client);
            if (ModuleKeybinds.consumePress(client, 14) && !inputBlocked && client.screen == null && client.player != null) {
                config.staffDetector.enabled = !config.staffDetector.enabled; save();
            }
            staffDetector.tick(client);
        });

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof TitleScreen) {
                Screens.getButtons(screen).add(Button.builder(Component.literal("Adam"), button ->
                        client.setScreen(new AdamScreen(config, this::save)))
                        .bounds(width - 66, height - 28, 58, 20).build());
            }
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "status"), (graphics, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (!config.showHud || client.player == null || client.screen != null || client.options.hideGui) {
                ModuleArrayList.reset();
                return;
            }
            ModuleArrayList.render(graphics, client.font, client.getWindow().getGuiScaledWidth(), config, freecam.isActive());
            AutoBuilderHud.render(graphics, client.font, client.getWindow().getGuiScaledHeight(), config);
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "progress_bar"), (graphics, delta) -> {
            Minecraft client = Minecraft.getInstance();
            if (!config.progressBar.enabled || client.player == null || client.screen != null || client.options.hideGui) return;
            ProgressBarHud.render(graphics, config, config.progressBar.completedStages);
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "base_finder_inspect"),
                (graphics, delta) -> BaseFinderPanel.render(graphics, config, chunkScanner));
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "sound_alerts"), (graphics, delta) ->
                dev.adam.client.gui.SoundAlertHud.render(graphics, config, soundDetector.alerts()));
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "block_esp"), (graphics, delta) ->
                BlockEspHud.render(graphics, config, blockEsp));
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "staff_detector"), (graphics, delta) ->
                dev.adam.client.gui.StaffDetectorHud.render(graphics, config, staffDetector));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> { staffDetector.disconnect(); blockEsp.disconnect(); soundDetector.disconnect(); chunkScanner.clear(); amethystScanner.clear(); lightDebug.clear(); freecam.disable(); fly.clientDisconnected(handler, client); autoBuilder.disconnect(); });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ProgressTimers.captureAndPause(config);
            fullbright.close();
            freecam.disable();
            fly.disable();
            autoBuilder.disconnect();
            save();
            lightDebug.close();
            blockEsp.close();
            soundDetector.disconnect();
            staffDetector.disconnect();
            renderer.close();
        });
    }

    private KeyMapping bind(String name, int key, KeyMapping.Category category) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping("key.adam." + name, InputConstants.Type.KEYSYM, key, category));
    }

    private void save() { store.save(config); }
}
