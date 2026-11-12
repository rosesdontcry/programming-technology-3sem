from functools import reduce

from tictactoe.rules import initial_state, play, legal_moves
from tictactoe.players import random_player, minimax_player


def run(moves):
    return reduce(play, moves, initial_state())


def test_random_player_returns_legal_move():
    s = run([0, 4])
    for _ in range(50):
        assert random_player(s) in legal_moves(s)


def test_minimax_player_takes_win():
    assert minimax_player(run([0, 3, 1, 4])) == 2


def test_minimax_player_blocks():
    assert minimax_player(run([0, 4, 1])) == 2