import numpy as np
import torch

from tictactoe.rules import initial_state, legal_moves, play, is_game_over
from tictactoe.minimax import best_moves
from tictactoe.model import encode


def collect(state, seen):
    key = state.board.tobytes()
    if key in seen:
        return
    seen[key] = state
    for move in legal_moves(state):
        collect(play(state, move), seen)


def build_dataset():
    seen = {}
    collect(initial_state(), seen)
    xs, ys = [], []
    for state in seen.values():
        if is_game_over(state):
            continue
        good = best_moves(state)
        target = np.zeros(9, dtype=np.float32)
        target[good] = 1 / len(good)          # поровну на все лучшие ходы
        xs.append(encode(state))
        ys.append(target)
    return torch.from_numpy(np.stack(xs)), torch.from_numpy(np.stack(ys))