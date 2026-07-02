# TickSense

TickSense is a retrospective execution analytics plugin for Old School RuneScape.
It is planned as a RuneLite Plugin Hub plugin that records public RuneLite event
data, detects completed activities, and shows post-activity reports about
execution quality.

TickSense is not a live helper. It does not provide overlays, mechanic prompts,
input automation, menu mutation, networking, cloud sync, or AI summaries in the
MVP.

## Development

TickSense starts from the official RuneLite external plugin template and targets
Java 11 for Plugin Hub compatibility.

```powershell
.\gradlew test
.\gradlew run
```

The `run` task starts RuneLite in developer/debug mode and loads the local
TickSense plugin.
