from tictactoe.rules import initial_state, legal_moves, play, winner, is_game_over


def count_games(state, results):
    if is_game_over(state):
        results[winner(state)] += 1
        return
    for move in legal_moves(state):
        count_games(play(state, move), results)


def collect_positions(state, seen):
    key = state.board.tobytes()
    if key in seen:
        return
    seen.add(key)
    for move in legal_moves(state):
        collect_positions(play(state, move), seen)


def test_number_of_games():
    results = {1: 0, -1: 0, 0: 0}
    count_games(initial_state(), results)
    assert results[1] == 131184
    assert results[-1] == 77904
    assert results[0] == 46080


def test_number_of_positions():
    seen = set()
    collect_positions(initial_state(), seen)
    assert len(seen) == 5478