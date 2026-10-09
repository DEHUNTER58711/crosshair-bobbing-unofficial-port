/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: Forge config screen API replaced by NeoForge IConfigScreenFactory registered on the ModContainer; FMLPaths moved to net.neoforged; Minecraft#setScreen -> Gui#setScreen; Screen.render/renderBackground replaced by extractRenderState (26.x GUI extraction, background drawn automatically); ARGB text colour; ${MOD_ID} placeholders replaced by xbob.
 */
package krash220.xbob.game.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Boolean> DEFAULT = new LinkedHashMap<>();
    private static final Map<String, Boolean> CONFIG = new HashMap<>();

    private static ModContainer container;

    public static void setContainer(ModContainer modContainer) {
        container = modContainer;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void load() {
        File config = new File(FMLPaths.CONFIGDIR.get().toFile(), "xbob.json");

        if (!config.exists()) {
            save();
        }

        try (FileInputStream fis = new FileInputStream(config)) {
            Map map = GSON.fromJson(new InputStreamReader(fis, StandardCharsets.UTF_8), Map.class);

            if (map != null) {
                CONFIG.putAll(map);
            }
        } catch (IOException | RuntimeException e) {}
    }

    public static void save() {
        String cfg = GSON.toJson(CONFIG);

        try (FileOutputStream fis = new FileOutputStream(new File(FMLPaths.CONFIGDIR.get().toFile(), "xbob.json"))) {
            fis.write(cfg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {}
    }

    public static void define(String key, Boolean defaultValue) {
        DEFAULT.put(key, defaultValue);
        CONFIG.put(key, defaultValue);
    }

    public static void set(String key, Boolean value) {
        CONFIG.put(key, value);
    }

    public static boolean check(String key) {
        Object val = CONFIG.get(key);

        if (val instanceof Boolean b) {
            return b.booleanValue();
        } else {
            return DEFAULT.get(key).booleanValue();
        }
    }

    public static void registerGui() {
        if (container != null) {
            container.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory) (mod, previous) -> new ConfigScreen(Minecraft.getInstance(), previous));
        }
    }

    private static class ConfigScreen extends Screen {

        private static final Component VALUE_ENABLE = Component.translatable("xbob.config.value.enable").withStyle(ChatFormatting.GREEN);
        private static final Component VALUE_DISABLE = Component.translatable("xbob.config.value.disable").withStyle(ChatFormatting.DARK_RED);

        public static Component getValueText(String key) {
            boolean bool = check(key);

            return Component.translatable("%s: %s", Component.translatable("xbob.config." + key), bool ? VALUE_ENABLE : VALUE_DISABLE);
        }

        private Minecraft mc;
        private Screen previous;

        public ConfigScreen(Minecraft mc, Screen previous) {
            super(Component.translatable("xbob.config.title"));

            this.mc = mc;
            this.previous = previous;
        }

        @Override
        public void onClose() {
            save();

            this.mc.gui.setScreen(this.previous);
        }

        @Override
        protected void init() {
            int i = this.height / 6 - 12;

            for (String key : DEFAULT.keySet()) {
                this.addRenderableWidget(Button.builder(getValueText(key), btn -> {
                    set(key, !check(key));

                    btn.setMessage(getValueText(key));
                }).pos(this.width / 2 - 150, i).size(300, 20).build());

                i += 24;
            }

            this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> this.onClose()).pos(this.width / 2 - 100, this.height - 27).size(200, 20).build());
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
            graphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);
        }
    }
}
