# TickSense

TickSense helps Old School RuneScape players review how cleanly they played.

After a kill, wave, skilling loop, or session, TickSense shows where ticks were
lost, where execution was clean, and which moments were worth improving next
time.

It answers:

```text
How well did I play that?
```

It does not answer:

```text
What should I click next?
```

## What TickSense Does

TickSense watches your gameplay through RuneLite and builds reports after the
fact. A report can show things like:

- how many ticks were lost;
- how quickly you responded to an opportunity;
- where downtime happened;
- which actions were tick-perfect;
- which moments were late, missed, or repeated;
- how recent attempts compare with each other.

The goal is simple: play normally, then review what actually happened.

## Not A Live Helper

TickSense does not play the game for you.

It does not:

- click for you;
- press keys for you;
- change or reorder menus;
- tell you to click, pray, move, or attack now;
- solve mechanics live;
- show future-action countdowns;
- inspect packets or game memory.

TickSense is about feedback, not instructions.

## Perfect Tick Feedback

TickSense may support a small real-time-feeling reward when you have already
done something perfectly.

Example:

```text
Tick Perfect!
```

This is after-the-fact feedback. TickSense must first see that the action was
already completed perfectly, then it can show the message as quickly as possible
after that confirmation.

It is not:

```text
Click now
Pray now
Move now
```

Planned controls:

- turn Perfect Tick feedback on or off entirely;
- turn the text on or off;
- turn the sound on or off;
- choose where the text appears;
- limit how often it can appear.

The idea is closer to a satisfying XP drop than a boss helper.

## Activity Reports

TickSense is built to support different kinds of OSRS content.

Current supported or in-progress areas include:

- Gem Mining
- Construction
- Araxxor
- Vardorvis
- Inferno
- Scurrius
- Phantom Muspah
- Hunllef
- Corrupted Gauntlet

Some activities already produce reports. Others are currently observe-only while
their IDs, timings, and finish conditions are verified from replayable evidence.

## Example Report Ideas

Bossing:

```text
Araxxor kill
Total tick loss: 9
Spider engagement: 2 ticks late
Boss re-engagement: 1 tick late
Best moment: tick-perfect spider attack
Worst moment: 3 ticks lost after spider spawn
```

Skilling:

```text
Gem mining session
Idle ticks: 6
Redundant clicks: 3
Best rock response: tick-perfect
Average rock response: 1.2 ticks
```

Inferno or waves:

```text
Wave review
Wave duration: 73 ticks
Supply usage: 1 brew, 1 restore
Late responses: 2
Deaths or damage windows: reviewed after the wave
```

## Privacy

TickSense is local-first.

Data is stored under:

```text
~/.runelite/ticksense/
```

TickSense does not need cloud sync, a website account, or an external service for
its core reports. Debug capture is optional and intended for fixing activity
detection or ID issues.

## Development

TickSense targets Java 11 for RuneLite Plugin Hub compatibility.

```powershell
.\gradlew test
.\gradlew run
```

The `run` task starts RuneLite in developer/debug mode and loads the local
TickSense plugin.

More detail:

- [Architecture](docs/ARCHITECTURE.md)
- [Java and RuneLite standards](docs/JAVA_RUNELITE_STANDARDS.md)
- [Boss fixture capture checklist](docs/BOSS_FIXTURE_CAPTURE.md)
