import torch

from tictactoe.checkpoint import load_net
from tictactoe.dataset import build_dataset
from tictactoe.model import masked_logits
from tictactoe.render import render
from tictactoe.rules import State


def main():
    net = load_net()
    x, y = build_dataset()
    with torch.no_grad():
        pred = masked_logits(net(x), x).argmax(1)
    bad = y[torch.arange(len(y)), pred] == 0
    print("ошибок:", bad.sum().item(), "из", len(x))

    for i in bad.nonzero().flatten().tolist():
        board = x[i].numpy().reshape(3, 3).astype(int)
        empty = int((board == 0).sum())
        best = y[i].nonzero().flatten().tolist()
        print()
        print(f"свободных клеток: {empty}")
        print(render(State(board, 1)))
        print("сеть выбрала:", pred[i].item(), "лучшие ходы:", best)


if __name__ == "__main__":
    main()