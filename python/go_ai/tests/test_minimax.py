from functools import reduce

from tictactoe.rules import initial_state, play
from tictactoe.minimax import value, best_moves, _cache


def run(moves):
    return reduce(play, moves, initial_state())


def test_initial_position_is_draw():
    assert value(initial_state()) == 0


def test_can_win_in_one_move():
    # у крестиков 0 и 1, их ход: клетка 2 выигрывает
    assert value(run([0, 3, 1, 4])) == 1


def test_already_lost():
    # крестики уже выиграли, ходит нолик
    assert value(run([0, 3, 1, 4, 2])) == -1

def test_best_moves_single():
    assert best_moves(run([0, 4, 1])) == [2]       # единственный ход, закрывающий угрозу


def test_best_moves_win():
    assert best_moves(run([0, 3, 1, 4])) == [2]    # только клетка 2 выигрывает


def test_best_moves_empty_board():
    assert best_moves(initial_state()) == list(range(9))   # все первые ходы равноценны

def test_forced_loss():
    assert value(run([0, 1, 4])) == -1

def test_cache_has_all_positions():
    _cache.clear()
    value(initial_state())
    assert len(_cache) == 5478