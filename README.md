# Day & Night Cycle Plugin

A RuneLite plugin that adds a dynamic day/night cycle to Old School RuneScape. Sky colors shift in real time based on astronomical sunrise/sunset calculations for your chosen geographic location.

## Demo

https://github.com/user-attachments/assets/0551d405-bd74-4363-867e-489dd96e1ce5

https://github.com/user-attachments/assets/5bb3f919-12b8-4d65-8cec-83a1265ccaf1

## Features

- **Real-Time Sky Cycle** — Skybox colors follow actual sunrise and sunset times computed from geographic coordinates
- **44 Pre-Configured Cities** — Spanning 6 continents, each with correct timezone and coordinates
- **Custom Coordinates** — Input any latitude/longitude for location-accurate calculations
- **Custom Time Override** — Set a specific virtual time (HH:MM:SS)
- **Fast Test Mode** — Compress a full day/night cycle into a configurable duration (default 20s)
- **Smooth Color Interpolation** — Sine-curve blending with two-phase transitions for natural color progression
- **Customizable Colors** — Configure sunrise, day, sunset, and night colors independently
- **Virtual Time Overlay** — On-screen display of the current virtual time

## How It Works

The plugin uses an astronomical algorithm (Williams' method) to compute exact sunrise/sunset times for the selected location. Sky color is then interpolated across four phases — night, sunrise, day, sunset — with 30-minute transition buffers and sine-curve smoothing for realistic gradients.

## Tech Stack

- Java 11
- RuneLite Plugin API
- Gradle
- Lombok

## Build & Install

```bash
git clone https://github.com/rodhfr/DayNightCycle.git
cd DayNightCycle
./gradlew build
```

To test locally with RuneLite:

```bash
./gradlew run
```

To install in RuneLite, place the built JAR in the plugin directory and enable "Day-Night Cycle" in settings.

## Project Structure

```
DayNightCycle/
├── src/main/java/com/example/
│   ├── DayNightCyclePlugin.java    # Main plugin — rendering, sun calculation, color blending
│   ├── DayNightCycleConfig.java    # Configuration interface — cities, colors, time modes
│   └── DayNightCycleOverlay.java   # Virtual time HUD overlay
├── src/test/java/com/example/
│   ├── ExamplePluginTest.java      # Test launcher
│   └── Mainrunner.java             # Alternative test launcher
├── build.gradle
├── runelite-plugin.properties
└── LICENSE                         # BSD 2-Clause
```

## Legal

This is a modification of the original Skybox plugin. All copyright from the original author is maintained.

## License

BSD 2-Clause — see [LICENSE](LICENSE).
