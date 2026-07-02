#!/usr/bin/env python3
"""Select the next unblocked task from a large-feature-planning tasks.md file."""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path


TASK_ID_RE = re.compile(r"^T\d{3}$")


@dataclass
class Task:
    task_id: str
    completed: bool
    title: str
    description: str
    blockers: list[str]
    task_file: str


def split_row(line: str) -> list[str]:
    return [part.strip() for part in line.strip().strip("|").split("|")]


def parse_blockers(text: str) -> list[str]:
    text = text.strip()
    if not text or text.lower() == "none":
        return []
    return [item.strip() for item in text.split(",") if item.strip()]


def parse_task_file(cell: str) -> str:
    match = re.search(r"\((tasks/T\d{3}\.md)\)", cell)
    return match.group(1) if match else ""


def parse_tasks(path: Path) -> list[Task]:
    tasks: list[Task] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        if not line.startswith("| T"):
            continue
        cells = split_row(line)
        if len(cells) < 7 or not TASK_ID_RE.match(cells[0]):
            continue
        task_id, completed_cell, title, description, _issue, blockers, task_file = cells[:7]
        completed = completed_cell.lower() == "[x]"
        tasks.append(
            Task(
                task_id=task_id,
                completed=completed,
                title=title,
                description=description,
                blockers=parse_blockers(blockers),
                task_file=parse_task_file(task_file),
            )
        )
    return tasks


def select_task(tasks: list[Task], requested: str | None) -> tuple[Task | None, dict[str, list[str]]]:
    completed = {task.task_id for task in tasks if task.completed}
    by_id = {task.task_id: task for task in tasks}
    blocked: dict[str, list[str]] = {}

    if requested:
        requested = requested.upper()
        task = by_id.get(requested)
        if task is None:
            raise SystemExit(f"Unknown task id: {requested}")
        missing = [blocker for blocker in task.blockers if blocker not in completed]
        if task.completed:
            return None, {requested: ["already complete"]}
        if missing:
            return None, {requested: missing}
        return task, {}

    for task in tasks:
        if task.completed:
            continue
        missing = [blocker for blocker in task.blockers if blocker not in completed]
        if missing:
            blocked[task.task_id] = missing
            continue
        return task, blocked

    return None, blocked


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("tasks_md", type=Path)
    parser.add_argument("--task", help="Specific task ID to validate/select")
    parser.add_argument("--json", action="store_true", help="Emit JSON")
    args = parser.parse_args()

    tasks_md = args.tasks_md
    if not tasks_md.is_file():
        raise SystemExit(f"tasks.md not found: {tasks_md}")

    tasks = parse_tasks(tasks_md)
    selected, blocked = select_task(tasks, args.task)
    payload = {
        "tasks_md": str(tasks_md),
        "selected": None
        if selected is None
        else {
            "id": selected.task_id,
            "title": selected.title,
            "description": selected.description,
            "blockers": selected.blockers,
            "task_file": str(tasks_md.parent / selected.task_file) if selected.task_file else "",
        },
        "blocked": blocked,
        "complete_count": sum(1 for task in tasks if task.completed),
        "task_count": len(tasks),
    }

    if args.json:
        print(json.dumps(payload, indent=2))
    else:
        if selected is None:
            print("No unblocked incomplete task found.")
            if blocked:
                print("Blocked tasks:")
                for task_id, blockers in blocked.items():
                    print(f"- {task_id}: {', '.join(blockers)}")
        else:
            print(f"{selected.task_id} - {selected.title}")
            if selected.task_file:
                print(tasks_md.parent / selected.task_file)
    return 0 if selected is not None else 1


if __name__ == "__main__":
    sys.exit(main())
