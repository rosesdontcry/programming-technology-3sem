from functools import reduce

import pytest

from tictactoe.rules import initial_state, play, winner
from tictactoe.arena import play_game, play_match
from tictactoe.players import random_player, minimax_player


def test_game_finishes_with_valid_result():
    w, moves = play_game(random_player, random_player)
    assert w in (1, -1, 0)
    assert 5 <= len(moves) <= 9  # быстрее всего выигрывают за 5 ходов


def test_moves_replay_to_same_result():
    w, moves = play_game(random_player, random_player)
    final = reduce(play, moves, initial_state())
    assert winner(final) == w


def test_minimax_vs_minimax_is_always_draw():
    for _ in range(20):
        w, _moves = play_game(minimax_player, minimax_player)
        assert w == 0


def test_minimax_never_loses_to_random():
    for _ in range(50):
        w, _ = play_game(minimax_player, random_player)  # минимакс за крестики
        assert w != -1
        w, _ = play_game(random_player, minimax_player)  # минимакс за нолики
        assert w != 1


def test_illegal_move_raises():
    always_4 = lambda state: 4
    with pytest.raises(ValueError):
        play_game(always_4, always_4)  # второй ход в клетку 4 занят


def test_match_counts_add_up():
    r = play_match(random_player, random_player, games_per_side=10)
    assert sum(r.values()) == 20


def test_match_minimax_vs_minimax_all_draws():
    r = play_match(minimax_player, minimax_player, games_per_side=10)
    assert r == {1: 0, 0: 20, -1: 0}


def test_match_minimax_never_loses_to_random():
    r = play_match(minimax_player, random_player, games_per_side=100)
    assert r[-1] == 0
    assert r[1] > 0  # а против random минимакс обязан иногда выигрывать


def test_match_sides_are_counted_for_a():
    r = play_match(random_player, minimax_player, games_per_side=100)
    assert r[1] == 0  # random не может выиграть у минимакса


def test_match_log():
    log = []
    play_match(random_player, minimax_player, games_per_side=5,
               log=log, name_a="random", name_b="minimax")
    assert len(log) == 10
    assert sum(1 for g in log if g["x"] == "random") == 5  # половина партий: random за крестики
    for g in log:
        final = reduce(play, g["moves"], initial_state())
        assert winner(final) == g["winner"]  # запись совпадает с реальным исходом
