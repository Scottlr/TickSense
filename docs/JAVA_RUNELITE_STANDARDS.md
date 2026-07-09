# Java and RuneLite Standards

TickSense targets RuneLite Plugin Hub compatibility, so production and test code must compile against Java 11 APIs and language features.

## Java 11

- Keep Gradle locked to `options.release.set(11)`.
- Prefer Java 11 immutable factories such as `List.of`, `Set.of`, and `Map.of` for small fixed data.
- Use Java 11 conveniences such as `String.isBlank`, `Optional.isEmpty`, and `Files.readString` where they improve readability.
- Do not use post-Java-11 features such as records, sealed classes, text blocks, pattern matching, switch expressions, or `Stream.toList`. Gradle's Java 11 `--release` setting is the compatibility guardrail.

## RuneLite Boundaries

- Keep live RuneLite API objects at the plugin edge in `com.ticksense.runelite`.
- UI classes may use RuneLite client UI types such as `ColorScheme` and `PluginPanel`.
- Activity ID catalogs may import RuneLite constants such as `NpcID`, `ItemID`, `ObjectID`, `AnimationID`, `InventoryID`, `WidgetID`, and `gameval.InterfaceID` for named widget groups/components.
- Core, telemetry, analytics, storage, and activity strategy logic should consume normalized TickSense types and ID catalog helpers rather than RuneLite API objects.

## ID Catalogs

- Prefer RuneLite constants over raw numeric IDs whenever RuneLite exposes a constant.
- Prefer intent methods such as `isBossNpcId(int)` or `isSupplyItemId(int)` over exposing collection internals to callers.
- Array getters may remain for fixture assertions and strategy constructors, but they must return cloned arrays.
- Unknown or unverified mechanic IDs belong in observe-only diagnostics or fixture notes until source-owned replay evidence verifies them.

## Style

- Avoid wildcard imports.
- Use small immutable collections over mutable collections when data is fixed.
- Avoid broad style-only rewrites unless they pay for themselves with clearer boundaries or stronger compatibility checks.
- Do not add tests that exist only to police formatting or style. Prefer compiler settings, existing architectural boundary tests, and code review for standards.
