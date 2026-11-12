import numpy as np
import torch
import torch.nn as nn

MASK_VALUE = -1e9


def encode(state):
    # доска глазами того, кто ходит: свои = +1, чужие = -1, пусто = 0
    return (state.board * state.player).flatten().astype(np.float32)


class Net(nn.Module):
    def __init__(self, hidden=64):
        super().__init__()
        self.layers = nn.Sequential(
            nn.Linear(9, hidden), nn.ReLU(),
            nn.Linear(hidden, hidden), nn.ReLU(),
            nn.Linear(hidden, 9),
        )

    def forward(self, x):
        return self.layers(x)


def masked_logits(logits, x):
    # x это закодированная доска: занятые клетки не равны 0
    return logits.masked_fill(x != 0, MASK_VALUE)