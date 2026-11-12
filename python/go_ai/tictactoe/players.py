import random
import numpy as np
import torch

from tictactoe.model import encode, masked_logits
from tictactoe.rules import legal_moves
from tictactoe.minimax import minimax_move
from tictactoe.render import render


def random_player(state):
    return random.choice(legal_moves(state))


def minimax_player(state):
    return minimax_move(state)


def human_player(state):
    print(render(state))
    allowed = legal_moves(state)
    while True:
        text = input(f"Ваш ход {allowed}: ")
        try:
            move = int(text)
        except ValueError:
            print("Нужно число.")
            continue
        if move in allowed:
            return move
        print("Эта клетка занята или не существует.")

def net_player(net):
    def player(state):
        x = torch.from_numpy(encode(state)).unsqueeze(0)
        with torch.no_grad():
            logits = masked_logits(net(x), x)
        return int(logits.argmax(1).item())

    return player
