from tictactoe.arena import play_match, print_summary
from tictactoe.checkpoint import load_net
from tictactoe.players import net_player, random_player, minimax_player


def main():
    player = net_player(load_net())

    results = play_match(player, random_player, games_per_side=500)
    print_summary("net", "random", results)

    results = play_match(player, minimax_player, games_per_side=100)
    print_summary("net", "minimax", results)


if __name__ == "__main__":
    main()