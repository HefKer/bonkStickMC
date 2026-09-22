# Bonk Stick

A Fabric mod for Minecraft 1.21.1 that adds one item: a stale baguette that shoves mobs
around without hurting them.

![Bonking a mob with the Bonk Stick](assets/bonk.gif)

## What it does

Swing the Bonk Stick at a mob and it gets pushed away. That's a Bonk. It isn't an attack:
the target takes no damage, doesn't flash red or make a hurt sound, and doesn't turn on
you. Every Bonk that lands plays a bonk sound for everyone nearby, at a slightly
different pitch each time. Misses are silent.

- **How far it pushes.** A fully charged Bonk lands between Knockback I and Knockback II.
  Sprinting adds more on top, the same way a sprint hit does. The push scales with your
  attack cooldown bar, which refills at sword speed, so spam-clicking only gives a tap.
- **What you can Bonk.** Every mob, every player, and armor stands (they get pushed, not
  broken). Creative-mode players and invulnerable entities can't be Bonked. Swinging at
  anything else, like a boat or a minecart, works like punching it with an empty hand.
- **Shields.** A raised shield facing you blocks the Bonk completely. The shield takes no
  durability damage.
- **The item itself.** Unbreakable, can't be enchanted, doesn't stack, and you can't eat it.

By default players can be Bonked even on servers with PvP off, since a Bonk does no
damage. Server owners can change that (see [Configuration](#configuration)).

## Getting one

There's no crafting recipe. In survival you find it in chests:

- dungeon chests (the spawner rooms)
- stronghold corridor chests

Each of those has a 20% chance of holding one. A chest never has more than one, and the
rest of its loot is the same as vanilla. Server owners can change which chests count and
how likely it is.

In creative it's in the Combat tab, right after the netherite sword. It can also be
given with `/give @s bonkstick:bonk_stick`.

## Installing

Needs Minecraft 1.21.1, Fabric Loader 0.19.3 or newer, and Fabric API. Put the jar in
the `mods` folder on the server and on every client that joins it, since the mod adds a
new item and sound that the client has to know about. For single-player you only need
it in your own game.

## Configuration

The config lives in `config/bonkstick.json`. The mod writes it with the defaults the
first time it starts:

```json
{
  "bonk": {
    "bonkStrength": 1.0,
    "respectKnockbackResistance": true,
    "respectPvpSetting": false
  },
  "loot": {
    "minecraft:chests/simple_dungeon": 0.2,
    "minecraft:chests/stronghold_corridor": 0.2
  }
}
```

It's a server-side setting: the server reads it once at startup, so restart the server
to apply a change. In single-player your own game is the server. Editing the file on a
client that joins someone else's server does nothing.

### `bonk`

| Key | Default | What it does |
|---|---|---|
| `bonkStrength` | `1.0` | Multiplies how far a Bonk pushes. `2.0` pushes twice as far, `0` turns the push off. Allowed range is 0 to 5; anything outside is clamped. |
| `respectKnockbackResistance` | `true` | Whether knockback resistance (iron golems, netherite armor, and so on) reduces a Bonk, as it does vanilla knockback. Set it to `false` to push everything the full amount. |
| `respectPvpSetting` | `false` | Whether players stop being Bonkable when PvP is off (`pvp=false` in `server.properties`). With the default `false`, players can always be Bonked. |

### `loot`

Maps a loot-table id to the chance, from 0 to 1, that a chest using that table holds a
Bonk Stick. Any loot table works, vanilla or from another mod.

- The section is all or nothing. If you write a `loot` section, it replaces the defaults,
  so list the dungeon and stronghold tables too if you want to keep them.
- Leave `loot` out entirely to keep the defaults.
- `"loot": {}` means no chest ever has a Bonk Stick.
- A chance of `0` turns that table off. Values outside 0 to 1 are clamped.
- Ids that don't exist (a typo, or a mod that isn't installed) are ignored, with a note in
  the debug log.

For example, to also put one in half of all desert temple chests:

```json
"loot": {
  "minecraft:chests/simple_dungeon": 0.2,
  "minecraft:chests/stronghold_corridor": 0.2,
  "minecraft:chests/desert_pyramid": 0.5
}
```

Chests roll their loot the first time they're opened, so unopened chests in an existing
world pick up the new chances too.

### When the file is wrong

The file has to be plain JSON: no comments, no trailing commas. If it doesn't parse, the
mod logs a warning and uses all the defaults. A single bad value (wrong type, out of
range) falls back to its own default, and unknown keys are ignored, each with a warning
in the log.

The mod never overwrites a file that already exists, so your broken file stays put until
you fix it. Delete it to get a fresh default one on the next start.

## Known limitations

- There's no recipe on purpose. If your world is short on dungeons and strongholds, use
  `/give` or add more chests in the config.
- A creative-mode anvil can still put enchanted books on it. Vanilla lets creative
  players skip the anvil's compatibility check, so the "can't be enchanted" rule only
  holds in survival.

## Development

Built from the upstream
[fabric-example-mod](https://github.com/FabricMC/fabric-example-mod), plus a flake
devShell that makes `runClient` actually work on NixOS without an FHS wrapper.

The domain terms used in the code (Bonk, Bonkable, Charge, Loot Chest…) are defined in
`CONTEXT.md`; design decisions are in `docs/adr/`.

### Versions

| | |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.116.17+1.21.1 |
| Loom | 1.17.21 |
| Gradle | 9.5.1 (via wrapper) |
| Java | 21 |
| JUnit | 5.11.4 (tests only) |

Mappings are Mojang official (`loom.officialMojangMappings()`), not Yarn. Change the
`mappings` line in `build.gradle` if you want Yarn instead.

Bump any of these in `gradle.properties`. Java 21 is what Mojang's version manifest
declares for 1.21.1 (`javaVersion.majorVersion`); it's a floor, not an exact pin, but
it needs to come from the devShell — Gradle's toolchain auto-provisioning downloads
prebuilt JDK tarballs that don't run on NixOS.

### Build and run

```sh
direnv allow     # or: nix develop
./gradlew build
./gradlew runClient
./gradlew test    # unit tests (config parsing, Bonk Strength, Loot Chests)
```

The built jar lands in `build/libs/`. The top-level `assets/` folder (the README gif and
the original sound download) is not part of the mod and never goes into the jar; the
mod's own resources live in `src/main/resources/`.

### Why the devShell is shaped like this

Minecraft on NixOS fails in a specific way. LWJGL ships its native `.so` files inside
its jars, extracts them to a temp directory at runtime, and `dlopen`s their
dependencies by bare soname. Those extracted binaries have no RUNPATH, and a nixpkgs
JDK has no FHS fallback, so nothing resolves. The devShell answers that with
`LD_LIBRARY_PATH`:

- **`addDriverRunpath.driverLink` first.** This is `/run/opengl-driver/lib`, where the
  vendor GL/Vulkan ICD lives. `libGL` alone only gets you the dispatch layer. Omitting
  this is the difference between hardware rendering and a failed GL context.
- **`alsa-lib`, `libpulseaudio`, `pipewire`, `libjack2`.** LWJGL's *bundled*
  `libopenal.so` dlopens these backends itself. Adding `openal` alone gets you silence.
- **`libdecor`, `wayland`, `libxkbcommon`** for GLFW's Wayland backend, and the X11 set
  for the XWayland fallback.
- **`ncurses`, `zlib`** are not for Minecraft. Gradle extracts its own JNI natives
  (`libnative-platform-curses.so`) which need `libncursesw`/`libtinfo`.

The list mirrors the one in nixpkgs' `prismlauncher` wrapper, which is the best
maintained reference for this.

### Gotchas

**Don't mix nixpkgs `gradle` with `./gradlew`.** Gradle caches its extracted natives
under `$GRADLE_USER_HOME` keyed on version alone, ignoring file contents. nixpkgs'
`gradle` is autoPatchelf'd and the wrapper's is not, so alternating between them
poisons the cache and produces confusing load failures. Use the wrapper.

**Wayland works.** LWJGL 3.3.3 bundles GLFW 3.4 built with both the Wayland and X11
backends, and GLFW 3.4 prefers Wayland when `XDG_SESSION_TYPE=wayland`. If you hit the
known Wayland cursor-grab or fractional-scaling bugs, force the patched GLFW that's
already in the shell — but do it through Loom's DSL, not an environment variable:

```groovy
loom {
    runConfigs.all {
        property "org.lwjgl.glfw.libname", "/nix/store/.../lib/libglfw.so"
    }
}
```

`JAVA_TOOL_OPTIONS` looks like it should work here and mostly doesn't: the forked
client inherits the *Gradle daemon's* environment, and the daemon is long-lived, so a
daemon started outside the devShell ignores it. It also makes every JVM print `Picked
up JAVA_TOOL_OPTIONS` to stderr.

**Editors.** `jdtls` is in the shell. Zed has no built-in Java support — install the
Java extension, which prefers a `jdtls` on `$PATH` over downloading its own.

## Credits and licenses

**Mod code:** MIT, see [`LICENSE`](LICENSE).

**Bonk Sound:** "Bonk hitsound (real)" by Demomium, from
<https://gamebanana.com/sounds/47482>, licensed under
[CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/).

The sound is not covered by the MIT license. It ships in the mod as
`src/main/resources/assets/bonkstick/sounds/bonk.ogg`, and the original download is kept
in `assets/hitsound_19ad3.mp3`. CC BY-NC-ND means you can share it with credit, but not
commercially and not in modified form. If you fork the mod and need different terms,
swap in another sound.
