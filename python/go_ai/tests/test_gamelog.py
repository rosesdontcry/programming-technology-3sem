import numpy as np

from tictactoe.gamelog import save_game, load_games


def test_save_and_load(tmp_path):
    p = tmp_path / "g.jsonl"
    save_game("minimax", "random", 1, [4, 0, 8, 1, 2], path=p)
    save_game("random", "minimax", 0, [0, 1, 2], path=p)
    games = load_games(p)
    assert len(games) == 2        # вторая запись не затёрла первую
    assert games[0] == {"x": "minimax", "o": "random", "winner": 1, "moves": [4, 0, 8, 1, 2]}
    assert games[1]["winner"] == 0


def test_load_missing_file(tmp_path):
    assert load_games(tmp_path / "nope.jsonl") == []


def test_numpy_moves_are_saved(tmp_path):
    p = tmp_path / "g.jsonl"
    save_game("net", "random", 0, [np.int64(4), np.int64(0)], path=p)
    assert load_games(p)[0]["moves"] == [4, 0]