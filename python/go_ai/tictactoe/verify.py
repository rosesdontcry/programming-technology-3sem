from tictactoe.checkpoint import load_net
from tictactoe.minimax import best_moves
from tictactoe.players import net_player
from tictactoe.render import render
from tictactoe.rules import initial_state, legal_moves, play, is_game_over


def walk(state, net_side, player, stats):
    if is_game_over(state):
        return
    if state.player == net_side:
        stats["checked"] += 1
        move = player(state)
        if move not in best_moves(state):
            stats["bad"].append((state, move))
        walk(play(state, move), net_side, player, stats)
    else:
        for m in legal_moves(state):
            walk(play(state, m), net_side, player, stats)


def main():
    player = net_player(load_net())
    for side, name in ((1, "X"), (-1, "O")):
        stats = {"checked": 0, "bad": []}
        walk(initial_state(), side, player, stats)
        print(f"сеть за {name}: позиций {stats['checked']}, нарушений {len(stats['bad'])}")
        for state, move in stats["bad"]:
            print(render(state))
            print("ходит", state.player, "сеть выбрала:", move, "лучшие:", best_moves(state))


if __name__ == "__main__":
    main()