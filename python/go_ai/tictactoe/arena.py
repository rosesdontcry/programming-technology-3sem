from tictactoe.rules import initial_state, play, winner, is_game_over


def play_game(player_x, player_o):
    state = initial_state()
    moves = []
    while not is_game_over(state):
        player = player_x if state.player == 1 else player_o
        move = player(state)
        moves.append(move)
        state = play(state, move)
    return winner(state), moves


def play_match(player_a, player_b, games_per_side=100, log=None, name_a="A", name_b="B"):
    results = {1: 0, 0: 0, -1: 0}
    for _ in range(games_per_side):
        w, moves = play_game(player_a, player_b)
        results[w] += 1
        if log is not None:
            log.append({"x": name_a, "o": name_b, "winner": w, "moves": moves})

        w, moves = play_game(player_b, player_a)
        results[-w] += 1
        if log is not None:
            log.append({"x": name_b, "o": name_a, "winner": w, "moves": moves})
    return results


def print_summary(name_a, name_b, results):
    total = sum(results.values())
    print(f"{name_a} против {name_b}, партий: {total}")
    print(f"  побед:     {results[1]}")
    print(f"  ничьих:    {results[0]}")
    print(f"  поражений: {results[-1]}")
