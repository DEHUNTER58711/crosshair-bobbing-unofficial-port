/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: Forge event bus replaced: RenderGuiOverlayEvent/VanillaGuiOverlay -> NeoForge RenderGuiLayerEvent/VanillaGuiLayers.CROSSHAIR; GuiGraphics -> GuiGraphicsExtractor with a 2D Matrix3x2fStack pose; partial tick from DeltaTracker.
 */
package krash220.xbob.game.api.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import krash220.xbob.game.api.math.MatrixStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

public class GuiBus {

    private static boolean registered = false;

    private static List<BiConsumer<MatrixStack, Float>> pre = new ArrayList<>();
    private static List<BiConsumer<MatrixStack, Float>> post = new ArrayList<>();

    public static void registerRenderCrosshair(BiConsumer<MatrixStack, Float> pre, BiConsumer<MatrixStack, Float> post) {
        if (!registered) {
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, RenderGuiLayerEvent.Pre.class, GuiBus::pre);
            NeoForge.EVENT_BUS.addListener(RenderGuiLayerEvent.Post.class, GuiBus::post);
            registered = true;
        }

        GuiBus.pre.add(pre);
        GuiBus.post.add(post);
    }

    public static void pre(RenderGuiLayerEvent.Pre event) {
        if (VanillaGuiLayers.CROSSHAIR.equals(event.getName()) && !event.isCanceled()) {
            MatrixStack mat = new MatrixStack(event.getGuiGraphics().pose());
            float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);

            for (BiConsumer<MatrixStack, Float> handler : pre) {
                handler.accept(mat, partialTicks);
            }
        }
    }

    public static void post(RenderGuiLayerEvent.Post event) {
        if (VanillaGuiLayers.CROSSHAIR.equals(event.getName())) {
            MatrixStack mat = new MatrixStack(event.getGuiGraphics().pose());
            float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);

            for (BiConsumer<MatrixStack, Float> handler : post) {
                handler.accept(mat, partialTicks);
            }
        }
    }
}
