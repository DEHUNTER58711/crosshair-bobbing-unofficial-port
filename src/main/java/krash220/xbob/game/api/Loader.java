/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: Forge MCPVersion/Environment replaced by NeoForge FMLLoader equivalents.
 */
package krash220.xbob.game.api;

import net.minecraft.SharedConstants;
import net.neoforged.fml.loading.FMLEnvironment;

public class Loader {

    public static String getPlatform() {
        return "NeoForge";
    }

    public static String getVersion() {
        return SharedConstants.getCurrentVersion().name();
    }

    public static boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }
}
