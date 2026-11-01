# SOLO SYSTEM V5 — Android project

Dark blue/black Solo Leveling-inspired personal RPG tracker. Russian UI, local progress storage, daily and weekly quests, EXP/levels, player stats, dungeon challenge cards, S-rank game penalties.

## Requirements
- Android Studio with Android SDK Platform 36 installed
- JDK 17
- Internet for the first Gradle dependency sync

## Open and build
1. Extract the ZIP.
2. Open the `solo_system_v5` folder in Android Studio.
3. Android Studio will import the Gradle project. If prompted, use Gradle 8.11.1 and JDK 17.
4. Run **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Debug APK should appear at `app/build/outputs/apk/debug/app-debug.apk`.

## Important note about this source package
This archive contains the Gradle wrapper configuration and scripts are not bundled with a wrapper JAR. If Android Studio reports that the wrapper JAR is missing, use Android Studio's Gradle sync/import support to regenerate the wrapper, or run `gradle wrapper --gradle-version 8.11.1` from a machine with Gradle installed. The app has not been compiled or device-tested in this environment.

## Gameplay notes
- Daily quests and weekly challenges are generated from the built-in quest set.
- Completion grants EXP. A missed daily quest applies its configured EXP penalty the next time the app is opened after the date changes. Repeated missed quests can double the penalty.
- EXP cannot fall below zero. The app does not impose real-world penalties or encourage unsafe exercise; quests are adjustable personal challenges.
- Data is stored locally in SharedPreferences and remains on the device.
