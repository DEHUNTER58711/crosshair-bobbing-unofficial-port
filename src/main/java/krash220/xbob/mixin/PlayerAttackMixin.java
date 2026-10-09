/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: NEW file. Replaces the Forge LivingAttackEvent listener: NeoForge 26.3 has no client-side attack event carrying the damage amount, so the local player's Player#attack is wrapped at its hurtOrSimulate call to read the final damage.
 */
package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import krash220.xbob.game.api.bus.PlayerBus;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {

    @WrapOperation(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean xbob$onAttack(Entity target, DamageSource source, float damage, Operation<Boolean> original) {
        Player self = (Player) (Object) this;

        if (self.level().isClientSide()) {
            PlayerBus.onClientAttack(self, target, source, damage);
        }

        return original.call(target, source, damage);
    }
}
