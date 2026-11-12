from pathlib import Path

import torch

from tictactoe.model import Net

DEFAULT_PATH = Path(__file__).resolve().parent.parent / "checkpoints" / "sl.pt"


def save_net(net, path=DEFAULT_PATH):
    path = Path(path)
    path.parent.mkdir(exist_ok=True)
    torch.save(net.state_dict(), path)


def load_net(path=DEFAULT_PATH):
    net = Net()
    net.load_state_dict(torch.load(path))
    net.eval()
    return net