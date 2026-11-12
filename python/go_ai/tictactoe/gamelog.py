import json
from pathlib import Path

# logs/ лежит рядом с папкой tictactoe, независимо от того, откуда запущена программа
LOG_PATH = Path(__file__).resolve().parent.parent / "logs" / "games.jsonl"


def save_game(name_x, name_o, winner, moves, path=LOG_PATH):
    record = {
        "x": name_x,
        "o": name_o,
        "winner": winner,
        "moves": [int(m) for m in moves],
    }
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "a", encoding="utf-8") as f:
        f.write(json.dumps(record) + "\n")


def load_games(path=LOG_PATH):
    if not path.exists():
        return []
    with open(path, encoding="utf-8") as f:
        return [json.loads(line) for line in f if line.strip()]