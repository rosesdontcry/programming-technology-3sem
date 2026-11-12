from tictactoe.rules import initial_state, play
from tictactoe.render import render


def lost_games(games, name):
    # номера партий в списке, где игрок с таким именем проиграл
    result = []
    for i, g in enumerate(games):
        if g["x"] == name and g["winner"] == -1:
            result.append(i)
        elif g["o"] == name and g["winner"] == 1:
            result.append(i)
    return result


def replay_text(record):
    state = initial_state()
    parts = [f"X: {record['x']}, O: {record['o']}", render(state)]
    for n, move in enumerate(record["moves"], start=1):
        who = "X" if state.player == 1 else "O"
        state = play(state, move)
        parts.append(f"\nХод {n}: {who} -> {move}\n" + render(state))
    w = record["winner"]
    parts.append("\nИтог: " + {1: "победил X", -1: "победил O", 0: "ничья"}[w])
    return "\n".join(parts)