# Porting notes – Crosshair Bobbing → Minecraft 26.3 / NeoForge (unofficial)

* Original: Crosshair Bobbing by **Krash220**, GPL-3.0, https://github.com/Krash220/CrosshairBobbingMod,
  upstream commit `67608da` (2025-02-05) = upstream release **1.10** (Modrinth version 1.10, published 2025-02-05 07:39 UTC, about 3 h after this merge; changelog "Compat with TACZ 1.1.4 (Thanks to SNWCreations)" matches the merged PR #15. Upstream has no git tags/GitHub releases and its `mod.properties` was not bumped and still says 1.9). Ported from `game/forge1.20.2` + `src/main` (MainMod) + `src/modbase` (ForgeLoader, mods.toml, lang).
* Porter: **DEHunter58711**, 2026. Unofficial – do not report port issues to the original author.
* Toolchain: Minecraft 26.3, NeoForge `26.3.0.51-beta`, ModDevGradle `2.0.147` (official MDK-26.3 layout), Gradle 9.2.1, Temurin JDK 25.0.4.1+1, Java target 25, official Mojang names.
* Mod id `xbob`, version `1.10+mc26.3-neoforge-unofficial.1`, license `GPL-3.0`, Minecraft range `[26.3,26.4)`.
* Metadata per CurseForge feedback from the Bulk Villager Trading port: no `issueTrackerURL`, no `displayURL`, no `displayTest`; original credited in `authors` and `credits`.
* Mod icon: `src/main/resources/xbob_icon.png` (128x128 RGBA PNG), referenced via `logoFile="xbob_icon.png"`. Made for this unofficial port by the team; it is not artwork from the original mod / Krash220.

## Build system
1. The original custom multi-version `buildSrc` (ffm1 plugin: `${MOD_ID}` source templating, package renaming, mixin merging for 16 Forge/Fabric targets) was replaced by a single standard NeoForge MDK project. All `${MOD_*}` placeholders in sources/lang files were substituted with literal values.
2. `src/modbase/.../ForgeLoader.java` + `META-INF/mods.toml` → `platform/NeoForgeLoader.java` + `META-INF/neoforge.mods.toml` (template). Fabric loader/files and `pack.mcmeta` dropped (not needed for NeoForge). Optional `tacz` dependency entry dropped (TaCZ integration only existed in other targets and has no 26.3 build).

## Code / API changes (with reasons)
3. **Entrypoint**: `@Mod("xbob")` with `FMLJavaModLoadingContext` + reflection → `@Mod(value="xbob", dist=Dist.CLIENT)` with constructor injection `(IEventBus, ModContainer)`. Never constructed on a dedicated server. `MainMod` is still created in `FMLClientSetupEvent` (now via `enqueueWork`, on the main thread).
4. **Crosshair hook**: Forge `RenderGuiOverlayEvent.Pre/Post` + `VanillaGuiOverlay.CROSSHAIR` → NeoForge `RenderGuiLayerEvent.Pre/Post` + `VanillaGuiLayers.CROSSHAIR` (same priorities: Pre at LOWEST, skip if cancelled). Partial tick from the event's `DeltaTracker`.
5. **HUD matrix**: `GuiGraphics.pose()` (3D `PoseStack`) → `GuiGraphicsExtractor.pose()` = 2D JOML `Matrix3x2fStack` (since 1.21.6). `MatrixStack` wrapper now has two backends: JOML `Matrix4fStack` for the camera maths and `Matrix3x2fStack` for the HUD (translate/rotate-about-Z/scale only – which is all the HUD transform uses).
6. **Camera/projection**: `GameRenderer#getProjectionMatrix(getFov(...))` and `getFov` no longer exist. The projection matrix is taken from the extracted `CameraRenderState.projectionMatrix` (`GameRenderer#gameRenderState()`); the HUD is extracted after the camera in the same frame, so it is current.
7. **View/hurt bobbing**: `GameRenderer#bobHurt/bobView(PoseStack, float)` → `bobHurt/bobView(CameraRenderState, PoseStack)` (invokers retargeted; result multiplied into the camera matrix).
8. **Nausea/portal distortion**: `LocalPlayer.(o)spinningEffectIntensity` and `GameRenderer.tick` are gone; now uses `PlayerRenderState.portalEffectIntensity / nauseaEffectIntensity / spinningEffectAngle` exactly like vanilla 26.3 `GameRenderer#renderLevel`.
9. **Debug-crosshair check**: `options.hideGui` → `gui.hud.isHidden()`; "F3 open & not reduced debug" → `debugEntries.isCurrentlyEnabled(DebugScreenEntries.THREE_DIMENSIONAL_CROSSHAIR)` (26.x only replaces the crosshair when that debug entry is on).
10. **Hand height (item switch animation)**: `ItemInHandRenderer.mainHandHeight/oMainHandHeight/mainHandItem` moved to `net.minecraft.client.player.FirstPersonHandsAndItems` (`LocalPlayer#firstPersonHandsAndItems()`); accessor retargeted.
11. **Player API**: `UseAnim` → `ItemUseAnimation` (`SPEAR` → `TRIDENT`), `getUseDuration()` → `getUseDuration(LivingEntity)`, `CrossbowItem.getChargeDuration(stack)` → `(stack, entity)`, `swinging`/`getAttackAnim` → `isSwinging()`/`getSwingAnimation()`, `Camera#getEntity` → `entity()`, `getMainCamera` → `mainCamera()`, `Inventory.selected` → `getSelectedSlot()`.
12. **Attack detection**: Forge `LivingAttackEvent` (fired client-side from `hurt`) does not exist in NeoForge 26.3, and client `hurt` was split into `hurtServer/hurtClient`. New `PlayerAttackMixin` (MixinExtras `@WrapOperation` on `Entity#hurtOrSimulate` inside `Player#attack`, client only, local player only) reads the final attack damage. `isDamageSourceBlocked` was removed → `LivingEntity#isBlocking()`; `isInvulnerableTo(source)` now needs a `ServerLevel` → `isInvulnerable()`.
13. **Block break**: `MultiPlayerGameMode#destroyBlock(BlockPos)` mixin unchanged.
14. **Config**: `ConfigScreenHandler.ConfigScreenFactory` → `IConfigScreenFactory` on the `ModContainer`; `Screen#render/renderBackground` → `extractRenderState` (background automatic); `Minecraft#setScreen` → `Gui#setScreen`; text colour now ARGB. Config file and keys unchanged (`config/xbob.json`); robust against a missing/garbled file.
15. **Loader info**: `MCPVersion`/`Environment` → `SharedConstants.getCurrentVersion().name()` / `FMLEnvironment.getDist()`.
16. **Camera Overhaul** compatibility (`mirsario.cameraoverhaul` callback) removed – no 26.3 version/API to compile against; `Render#modCamera` is a no-op.
17. Dev aid (not in original): `-Dxbob.selfcheck=true` (set in the `runClient` config) force-loads all mixin targets and checks the accessor interfaces were applied.

## In-game differences vs. the original
* **F3**: original froze the crosshair (no bobbing) while the F3 screen was open; now bobbing is only disabled when the crosshair itself is replaced (F3 "3D crosshair" debug entry) or the HUD is hidden (F1).
* **Hit animation**: damage now comes from 26.x combat (includes cooldown scaling, crit multiplier and enchantment bonus). Weak/early hits therefore produce a smaller animation than full-strength ones; a "crit" spin happens whenever damage > 1 (same rule as original).
* **Blocked hits**: shake animation is shown whenever the target is blocking with anything (direction of the shield is not checked); targets that are only immune via damage-type/creative rules may still show a hit animation.
* **New 26.x spear** (`ItemUseAnimation.SPEAR`) has no special crosshair animation; only the trident uses the trident animation.
* **Nausea/portal warp** follows vanilla 26.3's combined spin angle (portal effect now also warps the crosshair; speed matches the screen).
* **Hurt tilt** follows vanilla 26.3 including the *Damage Tilt* accessibility slider, death tilt and NeoForge `no_flinch` damage types.
* **Camera Overhaul** integration missing (see 16).
* **Config screen title**: upstream typo "Corsshair Bobbing Configuration" (en_us.json) intentionally corrected to "Crosshair Bobbing Configuration". The zh_cn/zh_hk/zh_tw translations did not contain it.
* Otherwise identical maths, animations and config options.

## Repository hygiene
* Raw QA logs (`qa/logs/`), backup jars (`qa/jars/`), `run/`, `build/`, the upstream clone (`orig/`) and decompiled sources (`.mcsrc/`) are git-ignored and never committed.
* `qa/run-server-test.sh` uses repository-relative paths (overridable via `SRV`/`OUT`/`UNI`).
