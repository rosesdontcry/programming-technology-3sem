from functools import reduce

from tictactoe.rules import initial_state, legal_moves, play, winner, is_game_over
from tictactoe.render import render
from tictactoe.minimax import value

NAMES = {1: "X", -1: "O"}


def show_tree(state, max_depth, depth=0):
    moves = legal_moves(state)
    scores = [-value(play(state, m)) for m in moves]   # оценка для того, кто ходит
    best = max(scores)
    for move, score in zip(moves, scores):
        child = play(state, move)
        note = ""
        if is_game_over(child):
            w = winner(child)
            note = "  <- конец: ничья" if w == 0 else f"  <- конец: победил {NAMES[w]}"
        star = " *" if score == best else ""
        print("    " * depth + f"{NAMES[state.player]} -> {move}: {score:+d}{star}{note}")
        if depth + 1 < max_depth and not is_game_over(child):
            show_tree(child, max_depth, depth + 1)


if __name__ == "__main__":
    s = reduce(play, [0, 1, 5, 4, 6, 8], initial_state())
    print(render(s))
    print()
    show_tree(s, max_depth=3)