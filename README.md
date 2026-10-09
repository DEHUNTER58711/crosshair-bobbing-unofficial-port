# Crosshair Bobbing – UNOFFICIAL NeoForge port for Minecraft 26.3

Makes the crosshair follow the view bobbing (plus small sneak / item-switch / bow / crossbow /
trident / eating / attack animations of the crosshair).

* **Original mod & author:** *Crosshair Bobbing* by **Krash220** –
  https://github.com/Krash220/CrosshairBobbingMod (Modrinth: https://modrinth.com/mod/xbob)
* **This port:** unofficial port to Minecraft 26.3 / NeoForge by **DEHunter58711** (2026).
  Based on upstream commit `67608da` = upstream release 1.10 (forge1.20.2 / common sources). Port version: `1.10+mc26.3-neoforge-unofficial.1`.

> ⚠️ This is an **unofficial** port, not made or endorsed by Krash220.
> **Please do not report problems with this port to the original author.**

## License

GNU General Public License v3.0 – see [`LICENSE`](LICENSE) (unchanged full text from the original
repository). All modifications by DEHunter58711 are likewise licensed under GPL-3.0; every modified
or new source file carries a notice saying so (GPL-3.0 §5a). The complete corresponding source is
this repository. (Note: upstream's `mod.properties` says `mod_license=MIT`, but the repository's
`LICENSE` file is GPL-3.0; this port follows the `LICENSE` file.)

## Details

* Client-side only (`@Mod(dist = Dist.CLIENT)`, all mixins in the `client` section). Safe to have
  on a dedicated server (it does nothing there), but servers don't need it.
* Config: `config/xbob.json`, or Mods list → Crosshair Bobbing → Config.
* Changes vs. the original and in-game differences: see [`PORTING_NOTES.md`](PORTING_NOTES.md).

## Building

Requires JDK 25. `./gradlew build` → `build/libs/xbob-<version>.jar`.
