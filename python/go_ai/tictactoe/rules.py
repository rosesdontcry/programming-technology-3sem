import numpy as np


class State:
    def __init__(self, board, player):
        board.flags.writeable = False  # объект NumPy
        self.board = board
        self.player = player  # 1 (крестики), -1 (нолики)


def initial_state():
    board = np.zeros((3, 3))
    return State(board, 1)


def legal_moves(state):
    if is_game_over(state) :
        return []
    flat = state.board.flatten()
    return [i for i in range(9) if flat[i] == 0]


def play(state, move):
    if move not in legal_moves(state):
        raise ValueError(f"unacceptable move: {move}")

    new_board = state.board.copy()
    row, col = divmod(move, 3)
    new_board[row, col] = state.player
    return State(new_board, -state.player)


LINES = [
    (0, 1, 2), (3, 4, 5), (6, 7, 8),
    (0, 3, 6), (1, 4, 7), (2, 5, 8),
    (0, 4, 8), (2, 4, 6),
]


def winner(state):
    flat = state.board.flatten()
    for a, b, c in LINES:
        total = flat[a] + flat[b] + flat[c]
        if total == 3:
            return 1
        if total == -3:
            return -1
    if 0 not in flat:
        return 0
    return None


def is_game_over(state):
    return winner(state) is not None
