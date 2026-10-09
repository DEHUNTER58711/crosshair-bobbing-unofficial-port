/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: ItemInHandRenderer no longer holds the hand-height state in 26.3; it moved to FirstPersonHandsAndItems (LocalPlayer#firstPersonHandsAndItems). Accessor retargeted.
 */
package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.item.ItemStack;

@Mixin(FirstPersonHandsAndItems.class)
public interface ItemInHandRendererAccessor {

    @Accessor("mainHandHeight")
    float getMainHandHeight();

    @Accessor("oMainHandHeight")
    float getPrevMainHandHeight();

    @Accessor("mainHandItem")
    ItemStack getMainHandItem();
}
