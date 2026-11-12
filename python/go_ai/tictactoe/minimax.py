from tictactoe.rules import legal_moves, play, winner, is_game_over
import random

_cache = {}   # позиция -> оценка


def value(state):
    key = (state.board.tobytes(), state.player)
    if key in _cache:
        return _cache[key]
    if is_game_over(state):
        result = winner(state) * state.player
    else:
        result = -1
        for move in legal_moves(state):
            score = -value(play(state, move))
            if score > result:
                result = score
    _cache[key] = result
    return result

def move_scores(state):
    return {m: -value(play(state, m)) for m in legal_moves(state)}


def best_moves(state):
    scores = move_scores(state)
    top = max(scores.values())
    return [m for m, s in scores.items() if s == top]


def minimax_move(state):
    return random.choice(best_moves(state))