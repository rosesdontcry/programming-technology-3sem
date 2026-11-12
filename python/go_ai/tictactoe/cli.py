from functools import reduce

from tictactoe.rules import initial_state, play
from tictactoe.render import render
from tictactoe.arena import play_game
from tictactoe.players import human_player, minimax_player


def human_vs_minimax():
    side = input("Играть за X (ходит первым) или за O? [x/o]: ").strip().lower()
    if side == "o":
        human_side = -1
        w, moves = play_game(random_players, human_player)
    else:
        human_side = 1
        w, moves = play_game(human_player, minimax_player)

    print(render(reduce(play, moves, initial_state())))    # итоговая доска
    if w == 0:
        print("Ничья")
    elif w == human_side:
        print("Вы выиграли")
    else:
        print("Вы проиграли")


if __name__ == "__main__":
    human_vs_minimax()