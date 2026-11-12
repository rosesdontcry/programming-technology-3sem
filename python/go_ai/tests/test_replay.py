from tictactoe.replay import lost_games, replay_text


def test_lost_games_both_sides():
    games = [
        {"x": "a", "o": "b", "winner": 1, "moves": []},    # a выиграл
        {"x": "a", "o": "b", "winner": -1, "moves": []},   # a проиграл за X
        {"x": "b", "o": "a", "winner": 1, "moves": []},    # a проиграл за O
        {"x": "b", "o": "a", "winner": 0, "moves": []},    # ничья
    ]
    assert lost_games(games, "a") == [1, 2]
    assert lost_games(games, "b") == [0]


def test_replay_shows_every_move():
    record = {"x": "a", "o": "b", "winner": 1, "moves": [0, 3, 1, 4, 2]}
    text = replay_text(record)
    assert text.count("Ход ") == 5
    assert "Ход 5: X -> 2" in text
    assert "Ход 2: O -> 3" in text


def test_replay_result_line():
    record = {"x": "a", "o": "b", "winner": 1, "moves": [0, 3, 1, 4, 2]}
    assert replay_text(record).endswith("Итог: победил X")