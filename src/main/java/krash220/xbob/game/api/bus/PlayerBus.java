/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: LivingAttackEvent (Forge) no longer exists/fires on the client in NeoForge 26.3; attacks are now reported by PlayerAttackMixin. isInvulnerableTo(source) now needs a ServerLevel and isDamageSourceBlocked was removed, so client-side approximations (isInvulnerable / isBlocking) are used.
 */
package krash220.xbob.game.api.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PlayerBus {

    private static List<Consumer<Float>> attack = new ArrayList<>();

    public static void registerAttack(Consumer<Float> attack) {
        PlayerBus.attack.add(attack);
    }

    /** Called from PlayerAttackMixin on the logical client only. */
    public static void onClientAttack(Player attacker, Entity target, DamageSource source, float amount) {
        if (attack.isEmpty() || attacker != Minecraft.getInstance().player) {
            return;
        }

        if (target instanceof LivingEntity entity) {
            if (!entity.isInvulnerable() && !entity.isDeadOrDying()) {
                float damage = (source.getDirectEntity() == source.getEntity() ? amount : 1.0f) * (entity.isBlocking() ? 0 : 1);

                for (Consumer<Float> handler : attack) {
                    handler.accept(damage);
                }
            }
        }
    }

    public static void onBreakBlock() {
        for (Consumer<Float> handler : attack) {
            handler.accept(0.6f);
        }
    }
}
