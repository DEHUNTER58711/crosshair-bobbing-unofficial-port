/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: 26.3 GameRenderer: bobHurt/bobView now take (CameraRenderState, PoseStack); tick counter and getFov are gone (FOV/projection come from the extracted CameraRenderState, the nausea angle from PlayerRenderState).
 */
package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {

    @Invoker("bobHurt")
    void xbob$bobHurt(CameraRenderState cameraState, PoseStack poseStack);

    @Invoker("bobView")
    void xbob$bobView(CameraRenderState cameraState, PoseStack poseStack);
}
