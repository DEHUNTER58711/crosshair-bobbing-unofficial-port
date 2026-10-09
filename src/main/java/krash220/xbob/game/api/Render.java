/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: rewritten against the 26.3 extract/render-state pipeline: projection matrix, hurt/view bob inputs and nausea/portal spin are read from GameRenderer.gameRenderState() (extracted for the current frame before the HUD); hideGui -> Hud#isHidden, F3 debug crosshair -> DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR; getFrameTime -> DeltaTracker; Camera Overhaul compat removed (no 26.3 build / API); getReachDistance uses Player#blockInteractionRange.
 */
package krash220.xbob.game.api;

import com.mojang.blaze3d.vertex.PoseStack;

import krash220.xbob.game.api.math.MatrixStack;
import krash220.xbob.mixin.GameRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Render {

    public static int getScaledWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public static int getScaledHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    public static int getBlitOffset() {
        return 0;
    }

    public static boolean isDebugCrosshair() {
        Minecraft mc = Minecraft.getInstance();

        return mc.gui.hud.isHidden() || mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR);
    }

    private static GameRenderState state() {
        return Minecraft.getInstance().gameRenderer.gameRenderState();
    }

    public static void bobView(MatrixStack mat, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        CameraRenderState camera = state().levelRenderState.cameraRenderState;
        PoseStack pose = new PoseStack();

        ((GameRendererAccessor) mc.gameRenderer).xbob$bobHurt(camera, pose);
        if (mc.options.bobView().get().booleanValue()) {
            ((GameRendererAccessor) mc.gameRenderer).xbob$bobView(camera, pose);
        }

        mat.mul(pose.last().pose());
    }

    public static void modCamera(MatrixStack mat) {
        // Camera Overhaul integration of the original is not available for Minecraft 26.3 (see PORTING_NOTES.md).
    }

    public static void updateCameraMatrix(MatrixStack mat, float partialTicks) {
        mat.mul(state().levelRenderState.cameraRenderState.projectionMatrix);
    }

    @SuppressWarnings("resource")
    public static float getReachDistance() {
        return (float) Minecraft.getInstance().player.blockInteractionRange();
    }

    public static void distortion(MatrixStack mat, float partialTicks) {
        GameRenderState s = state();
        PlayerRenderState player = s.levelRenderState.playerRenderState;

        float g = s.optionsRenderState.screenEffectScale;
        float f = Math.max(player.portalEffectIntensity, player.nauseaEffectIntensity) * g * g;
        if (f > 0.0F) {
           float f1 = 5.0F / (f * f + 5.0F) - f * 0.04F;
           f1 = f1 * f1;
           float angle = player.spinningEffectAngle;
           mat.rotate(angle, 0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
           mat.scale(1.0F / f1, 1.0F, 1.0F);
           mat.rotate(-angle, 0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F);
        }
    }

    public static float getCenterDepth() {
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.getCameraEntity();
        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        if (entity != null && mc.level != null) {
            Vec3 vec3 = entity.getEyePosition(partialTicks);
            Vec3 vec31 = entity.getViewVector(partialTicks);
            Vec3 vec32 = vec3.add(vec31.x * 1000F, vec31.y * 1000F, vec31.z * 1000F);
            HitResult result = mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));

            if (result.getType() != HitResult.Type.MISS) {
                Vec3 begin = entity.getEyePosition(partialTicks);
                Vec3 end = result.getLocation();

                return (float) end.distanceTo(begin);
            }
        }

        return 1000F;
    }
}
