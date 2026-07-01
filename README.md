# CrimsonBoost

Mobile game booster for Minecraft launchers (Zalith Launcher, Zalith Launcher 2 Plus,
PojavLauncher, and vanilla Minecraft) plus any other app you add. No root required.

## What it does on Boost
- Kills background processes to free RAM/CPU headroom (keeps system-critical + input
  method + your target app untouched)
- Enables Do Not Disturb for the duration of the session (optional, requires a
  one-time permission grant)
- Launches the target app right after the boost pass

## Build
GitHub Actions builds a debug and an unsigned release APK on every push to `main`
(see `.github/workflows/build.yml`). Grab the APK from the workflow run's
**Artifacts** section, or trigger a manual build from the Actions tab
(`workflow_dispatch`).

To build locally with Android Studio: open the project root, let Gradle sync,
then `Build > Build APK(s)`.

## Add apps
Tap the **+** button, search your installed apps, and tap one to add it to your
boost list. Nothing is hardcoded — add Zalith Launcher, PojavLauncher, Minecraft,
Roblox, or anything else you play.
