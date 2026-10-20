# Skills

A client-side [Fabric](https://fabricmc.net/) mod for Minecraft that adds a **Skills** window with experience bars for six categories:

- Combat
- Exploration
- Mining
- Woodcutting
- Farming
- Fishing

Each bar shows the category name and level, with the XP already earned and the XP needed to level up written above it (`340 / 500 XP`).

> **Status:** early prototype. The window and its layout work, but the values shown are fixed demo numbers. XP gain, saving and level-ups are not implemented yet.

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
├── SkillsModClient.java   Entrypoint, registers the K key and opens the screen
├── SkillsScreen.java      The window and the experience bars
├── SkillCategory.java     The six categories (translation key + colour)
├── SkillProgress.java     Level / xp / xp needed for one skill
└── SkillData.java         Placeholder data source (demo values)
src/main/resources/
├── fabric.mod.json
└── assets/skillsmod/lang/ en_us.json, fr_fr.json
```

## Roadmap

- [ ] Save skill data per player
- [ ] Award XP from gameplay (kills, mining, chopping, harvesting, fishing, exploring)
- [ ] Level-up thresholds and level-up feedback
- [ ] Replace the demo values in `SkillData`

## License

MIT