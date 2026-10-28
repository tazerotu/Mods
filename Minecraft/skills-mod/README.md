# Skills

A client-side [Fabric](https://fabricmc.net/) mod for Minecraft that adds a **Skills** window with experience bars for six categories:

- Combat
- Exploration
- Mining
- Woodcutting
- Farming
- Fishing

Each bar shows the category name and level, with the XP already earned and the XP needed to level up written above it (`340 / 500 XP`).

Skills gain xp from what you do (harder actions give more), level up on an exponential curve, and unlock **traits** along the way.

> **Status:** prototype. XP gain, level-ups and traits work, and the bars update live. Skill data is **not saved yet** (it resets when the game closes) and is read directly from the game, so it works in singleplayer / when hosting, not on a dedicated server.

## Traits

A trait does nothing until its skill reaches the unlock level, then improves every few levels up to a cap. Every number is in `Traits.java`.

| Skill | Trait | Effect |
|---|---|---|
| Combat | Brute Force | bonus attack damage |
| Combat | Iron Skin | bonus armor |
| Exploration | Fall Training | takes less fall damage |
| Exploration | Light Feet | moves faster |
| Mining | Haste | mines faster with a pickaxe |
| Mining | Sturdy Picks | chance a block costs no pickaxe durability |
| Mining | Vein Miner | hold sneak: mines the whole ore vein (ores only) |
| Mining | Prospector | chance of double ore drops |
| Woodcutting | Haste | chops faster with an axe |
| Woodcutting | Sturdy Axes | chance a block costs no axe durability |
| Woodcutting | Tree Feller | hold sneak: fells the whole tree |
| Farming | Growth Aura | passive: each second, a chance to bonemeal a random crop around you (chance and radius grow) |
| Farming | Double Harvest | chance of double drops from mature crops |
| Fishing | Treasure Hunter | better fishing loot (bonus Luck) |
| Fishing | Sea Legs | less slowdown in water |

## XP

| Skill | XP comes from |
|---|---|
| Combat | kills: mob max health x 0.5 + its attack damage (halved for passive and baby mobs) |
| Exploration | distance walked, and entering a biome for the first time (rare biomes give more) |
| Mining | ores by rarity (coal 5 ... diamond 25, emerald 30, ancient debris 60), stone by hardness |
| Woodcutting | logs (nether stems give a little more) |
| Farming | fully grown crops only |
| Fishing | each catch |

Tune the amounts in `SkillXp.java` (`XP_MULTIPLIER` scales everything).

## Level curve

`xpToNext(level) = round(BASE_XP * GROWTH ^ (level - 1))`, with `BASE_XP = 50`, `GROWTH = 1.05` and `MAX_LEVEL = 100` at the top of `LevelCurve.java`. That makes level 2 cost 50 xp, level 50 cost 546 and level 100 about 124 000 xp in total.

## Usage

Press **K** in-game to open the Skills window. Press **Esc** or click **Done** to close it. The key can be rebound under *Options → Controls → Key Binds → Skills*.

## Requirements

| Component     | Version                          |
|---------------|----------------------------------|
| Minecraft     | 26.3                             |
| Java          | 25                               |
| Fabric Loader | 0.19.5 or newer                  |
| Fabric API    | 0.163.0+26.3 or newer 26.3 build |

## Installing

1. Install Fabric Loader for Minecraft 26.3.
2. Put `skillsmod-0.1.0.jar` and Fabric API in your `.minecraft/mods` folder.
3. Launch the game.

## Building from source

You need JDK 25 and Gradle (only to generate the wrapper the first time).

```bash
# one time, if the Gradle wrapper (gradlew) is missing
gradle wrapper --gradle-version 9.6.0

# build the jar
./gradlew build          # Windows: gradlew.bat build
```

The jar is written to `build/libs/skillsmod-0.1.0.jar` (ignore the `-sources` jar).

To test without installing, run `./gradlew runClient`.

Keep the project folder outside `.minecraft/mods`. Only the finished jar belongs there.

## Project structure

```
src/main/java/com/skillsmod/
├── SkillsMod.java         Common entrypoint, hooks gameplay events
├── SkillsModClient.java   Client entrypoint, registers the K key and opens the screen
├── SkillsScreen.java      The window and the experience bars
├── SkillCategory.java     The six categories (translation key + colour)
├── SkillProgress.java     Level / xp / xp needed for one skill
├── SkillData.java         Level and xp per player, level-up logic
├── LevelCurve.java        The exponential level curve (3 constants)
├── Scaling.java           Unlock / step / cap formula shared by all traits
├── Traits.java            Every trait: numbers and effects
├── SkillXp.java           How much xp each action is worth
├── SkillEvents.java       Block breaks, kills and per-second checks
└── VeinMiner.java         Breaks connected ores / logs
src/main/resources/
├── fabric.mod.json
└── assets/skillsmod/lang/ en_us.json, fr_fr.json
```

## Roadmap

- [x] Award XP from gameplay (kills, mining, chopping, harvesting, fishing, exploring)
- [x] Level-up thresholds (exponential curve)
- [x] Traits
- [ ] Save skill data per player
- [ ] Sync skill data to clients on dedicated servers
- [ ] Level-up feedback (sound, message)
- [ ] Ignore player-placed blocks for xp

## License

MIT