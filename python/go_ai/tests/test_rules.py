from functools import reduce

import numpy as np
import pytest

from tictactoe.rules import State, initial_state, legal_moves, play, winner, is_game_over


def run(moves):
    """Сыграть список ходов с начала партии."""
    return reduce(play, moves, initial_state())


def test_initial_state():
    s = initial_state()
    assert winner(s) is None
    assert legal_moves(s) == list(range(9))
    assert s.player == 1


def test_play_does_not_change_old_state():
    s = initial_state()
    play(s, 4)
    assert s.board.sum() == 0     # старая доска осталась пустой
    assert s.player == 1


def test_move_goes_to_right_cell():
    s = play(initial_state(), 1)
    assert s.board[0, 1] == 1     # ход 1 -> строка 0, столбец 1
    assert s.board[1, 0] == 0     # а не наоборот


def test_occupied_cell_raises():
    s = play(initial_state(), 4)
    with pytest.raises(ValueError):
        play(s, 4)


def test_out_of_range_raises():
    for bad in (-1, 9):
        with pytest.raises(ValueError):
            play(initial_state(), bad)


def test_all_8_lines_win_for_both_players():
    boards = []
    for i in range(3):
        row = np.zeros((3, 3), dtype=np.int8); row[i, :] = 1
        col = np.zeros((3, 3), dtype=np.int8); col[:, i] = 1
        boards += [row, col]
    boards.append(np.eye(3, dtype=np.int8))
    boards.append(np.fliplr(np.eye(3, dtype=np.int8)).copy())
    assert len(boards) == 8
    for b in boards:
        for who in (1, -1):
            assert winner(State(b * who, 1)) == who


def test_draw():
    s = run([0, 1, 2, 4, 3, 5, 7, 6, 8])
    assert winner(s) == 0
    assert is_game_over(s)


def test_win_on_last_move_is_win_not_draw():
    s = run([2, 0, 3, 1, 6, 4, 7, 5, 8])   # девятый ход собирает линию 6-7-8
    assert winner(s) == 1


def test_no_moves_after_win():
    s = run([0, 3, 1, 4, 2])
    assert legal_moves(s) == []
    with pytest.raises(ValueError):
        play(s, 5)