# Skills (Fabric, Minecraft 26.3)

Press **K** in-game to open a window with six experience bars:
Combat, Exploration, Mining, Woodcutting, Farming, Fishing.
Each bar shows the category + level, and `xp possessed / xp needed` above it.
Values are demo placeholders (see `SkillData.java`).

Requirements: JDK 25, Fabric Loader 0.19.5+, Fabric API 0.163.0+26.3 (or newer 26.3 build).

## Build
The Gradle wrapper scripts/jar are not included. Either:
  * run `gradle wrapper --gradle-version 9.6.0` in this folder (needs Gradle installed), or
  * generate a project at https://fabricmc.net/develop/template/ and copy its
    `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` here.
Then:  `./gradlew build`  ->  jar in `build/libs/skillsmod-0.1.0.jar`
Dev run:  `./gradlew runClient`
