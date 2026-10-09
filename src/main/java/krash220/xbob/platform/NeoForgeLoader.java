/*
 * Crosshair Bobbing - original work by Krash220 (https://github.com/Krash220/CrosshairBobbingMod),
 * licensed under the GNU General Public License v3.0 (see LICENSE).
 *
 * MODIFIED by DEHunter58711 in 2026 for the UNOFFICIAL NeoForge / Minecraft 26.3 port
 * (GPL-3.0 section 5a). This file is distributed under GPL-3.0 as well.
 * Changes: replaces the Forge ForgeLoader: NeoForge 26.3 entrypoint restricted to Dist.CLIENT (never constructed on a dedicated server); MainMod is created on FMLClientSetupEvent like before, but directly instead of via reflection. Optional -Dxbob.selfcheck=true forces mixin targets to load (dev/test aid, not in original).
 */
package krash220.xbob.platform;

import krash220.xbob.MainMod;
import krash220.xbob.game.api.Config;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(value = NeoForgeLoader.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeLoader {

    public static final String MOD_ID = "xbob";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public NeoForgeLoader(IEventBus modBus, ModContainer container) {
        Config.setContainer(container);
        modBus.addListener(this::setup);

        if (Boolean.getBoolean("xbob.selfcheck")) {
            selfCheck();
        }
    }

    private void setup(final FMLClientSetupEvent event) {
        event.enqueueWork(MainMod::new);
    }

    private static void selfCheck() {
        String[] targets = {
                "net.minecraft.client.renderer.GameRenderer",
                "net.minecraft.client.Camera",
                "net.minecraft.client.player.FirstPersonHandsAndItems",
                "net.minecraft.world.entity.LivingEntity",
                "net.minecraft.world.entity.player.Player",
                "net.minecraft.client.multiplayer.MultiPlayerGameMode",
        };
        ClassLoader loader = NeoForgeLoader.class.getClassLoader();
        for (String target : targets) {
            try {
                Class.forName(target, false, loader);
                LOGGER.info("[xbob selfcheck] mixin target {} loaded and transformed OK", target);
            } catch (Throwable t) {
                LOGGER.error("[xbob selfcheck] FAILED to load/transform {}", target, t);
                throw new IllegalStateException("xbob mixin self-check failed for " + target, t);
            }
        }
        // Accessor/invoker interfaces must actually be implemented by the transformed classes.
        check("net.minecraft.client.renderer.GameRenderer", "krash220.xbob.mixin.GameRendererAccessor", loader);
        check("net.minecraft.client.Camera", "krash220.xbob.mixin.CameraAccessor", loader);
        check("net.minecraft.client.player.FirstPersonHandsAndItems", "krash220.xbob.mixin.ItemInHandRendererAccessor", loader);
        check("net.minecraft.world.entity.LivingEntity", "krash220.xbob.mixin.LivingEntityAccessor", loader);
        LOGGER.info("[xbob selfcheck] all mixin targets OK");
    }

    private static void check(String target, String iface, ClassLoader loader) {
        try {
            Class<?> t = Class.forName(target, false, loader);
            Class<?> i = Class.forName(iface, false, loader);
            if (!i.isAssignableFrom(t)) {
                throw new IllegalStateException(target + " does not implement " + iface);
            }
            LOGGER.info("[xbob selfcheck] {} implements {}", target, iface);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
