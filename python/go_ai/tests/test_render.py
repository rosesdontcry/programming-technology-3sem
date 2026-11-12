from functools import reduce

from tictactoe.rules import initial_state, play
from tictactoe.render import render


def run(moves):
    return reduce(play, moves, initial_state())


def test_render_has_5_lines():
    assert len(render(initial_state()).split("\n")) == 5   # 3 строки и 2 разделителя


def test_render_counts_marks():
    text = render(run([4, 0, 8]))
    assert text.count("X") == 2
    assert text.count("O") == 1


def test_render_lines_have_equal_width():
    lines = render(run([4, 0, 8])).split("\n")
    assert len(set(len(line) for line in lines)) == 1      # все длины одинаковы


def test_render_positions():
    lines = render(run([4, 0, 8])).split("\n")
    assert lines[0].startswith("O")      # нолик в углу 0
    assert "X" in lines[2]               # крестик в углу 8