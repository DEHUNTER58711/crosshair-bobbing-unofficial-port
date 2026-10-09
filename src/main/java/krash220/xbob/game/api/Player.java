/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: UseAnim -> ItemUseAnimation (vanilla SPEAR animation was renamed TRIDENT in 1.21.x; the new 26.x SPEAR item animation is not treated as trident); getUseDuration/getChargeDuration need the user; swinging/getAttackAnim -> isSwinging/getSwingAnimation; Camera#getEntity -> entity(); getMainCamera -> mainCamera(); hand height now read from LocalPlayer#firstPersonHandsAndItems; Inventory#selected -> getSelectedSlot().
 */
package krash220.xbob.game.api;

import krash220.xbob.mixin.CameraAccessor;
import krash220.xbob.mixin.ItemInHandRendererAccessor;
import krash220.xbob.mixin.LivingEntityAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;

public class Player {

    private static Pose lastPose;
    private static float lastHandHeight;
    private static int lastSlot = -1;

    public static float spinProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player.isAutoSpinAttack()) {
            return Mth.clamp((20 - ((LivingEntityAccessor) mc.player).getAutoSpinAttackTicks() + partialTicks) / 20.0f, 0.0f, 1.0f);
        }

        return 0.0f;
    }

    public static float sneakProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();

        if (camera.entity() != null) {
            Entity entity = camera.entity();
            Pose pose = entity.getPose();

            if (pose != Pose.STANDING) {
                lastPose = pose;
            }

            if (entity.getPose() == Pose.STANDING && lastPose == Pose.CROUCHING || entity.getPose() == Pose.CROUCHING) {
                float standing = entity.getEyeHeight(Pose.STANDING);
                float crouching = entity.getEyeHeight(Pose.CROUCHING);

                float eyeHeight = ((CameraAccessor) camera).getEyeHeight();
                float eyeHeightOld = ((CameraAccessor) camera).getEyeHeightOld();

                return (Mth.lerp(partialTicks, eyeHeightOld, eyeHeight) - standing) / (crouching - standing);
            }
        }

        return 0.0f;
    }

    public static float swingProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();
        LocalPlayer player = mc.player;

        if (camera.entity() == player && player.isSwinging()) {
            return player.getSwingAnimation(partialTicks);
        }

        return 1.0f;
    }

    public static float usingItemProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();
        LocalPlayer player = mc.player;

        if (camera.entity() == player && player.isUsingItem()) {
            ItemStack item = player.getUseItem();
            float f = item.getUseDuration(player) - (player.getUseItemRemainingTicks() - partialTicks + 1.0f);

            if (item.getUseAnimation() == ItemUseAnimation.BOW) {
                return Math.min(f / 20f, 1.0f);
            } else if (item.getUseAnimation() == ItemUseAnimation.TRIDENT) {
                return Math.min(f / 10f, 1.0f);
            } else {
                return f / item.getUseDuration(player);
            }
        }

        return 1.0f;
    }

    public static float getItemShake(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();
        LocalPlayer player = mc.player;

        if (camera.entity() == player && player.isUsingItem()) {
            ItemStack item = player.getUseItem();
            float f = item.getUseDuration(player) - (player.getUseItemRemainingTicks() - partialTicks + 1.0f);
            float f0 = 0.1f;

            if (item.getUseAnimation() == ItemUseAnimation.CROSSBOW && !CrossbowItem.isCharged(item) && player.getUseItemRemainingTicks() > 0) {
                f0 = Math.min(f / CrossbowItem.getChargeDuration(item, player), 1.0f);
            } else if (item.getUseAnimation() == ItemUseAnimation.BOW) {
                f0 = f / 20.0f;
                f0 = (f0 * f0 + f0 * 2.0f) / 3.0f;
                f0 = Math.min(f0, 1.0f);
            } else if (item.getUseAnimation() == ItemUseAnimation.TRIDENT) {
                f0 = Math.min(f / 10.0f, 1.0f);
            } else {
                return 0.0f;
            }

            return Mth.sin((f - 0.1f) * 1.3f) * (f0 - 0.1f);
        }

        return 0.0f;
    }

    public static ItemType getUsingType() {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();
        LocalPlayer player = mc.player;

        if (camera.entity() == player && player.isUsingItem()) {
            ItemStack item = player.getUseItem();

            switch (item.getUseAnimation()) {
            case EAT:
            case DRINK:
                return ItemType.EATING;
            case BOW:
                return ItemType.BOW;
            case TRIDENT:
                return ItemType.SPEAR;
            default:
                break;
            }

            if (item.getItem() == Items.CROSSBOW && !CrossbowItem.isCharged(item)) {
                return ItemType.CROSSBOW;
            }
        }

        return ItemType.NONE;
    }

    public static float changeItemProgress(float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        ItemInHandRendererAccessor accessor = (ItemInHandRendererAccessor) mc.player.firstPersonHandsAndItems();
        Camera camera = mc.gameRenderer.mainCamera();
        LocalPlayer player = mc.player;

        if (camera.entity() == player && !player.isHandsBusy() && !player.isUsingItem() && !player.isSwinging()) {
            float height = (accessor.getMainHandHeight() - accessor.getPrevMainHandHeight()) * partialTicks + accessor.getPrevMainHandHeight();

            if (player.getMainHandItem().getItem() == accessor.getMainHandItem().getItem() && lastSlot == player.getInventory().getSelectedSlot()) {
                lastHandHeight = Math.max(lastHandHeight, height);

                return lastHandHeight;
            } else {
                lastSlot = player.getInventory().getSelectedSlot();
                lastHandHeight = 0.0f;

                return height;
            }
        }

        lastHandHeight = 1.0f;
        return 1.0f;
    }

    public enum ItemType {
        NONE,
        EATING,
        BOW,
        SPEAR,
        CROSSBOW
    }
}
