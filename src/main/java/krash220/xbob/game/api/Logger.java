/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: log category placeholder replaced by xbob.
 */
package krash220.xbob.game.api;

import org.apache.logging.log4j.LogManager;

public class Logger {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("xbob");

    public static void info(String log, Object... args) {
        LOGGER.info(log, args);
    }
}
