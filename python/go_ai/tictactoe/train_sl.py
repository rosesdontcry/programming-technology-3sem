import torch

from tictactoe.dataset import build_dataset
from tictactoe.model import Net, masked_logits


def loss_fn(net, x, y):
    log_p = torch.log_softmax(masked_logits(net(x), x), dim=1)
    return -(y * log_p).sum(dim=1).mean()


def accuracy(net, x, y):
    # доля позиций, где ход сети входит в множество лучших ходов
    with torch.no_grad():
        pred = masked_logits(net(x), x).argmax(dim=1)
        return (y[torch.arange(len(y)), pred] > 0).float().mean().item()


def train(epochs=200, batch_size=128, lr=1e-3, seed=0):
    torch.manual_seed(seed)
    x, y = build_dataset()
    net = Net()
    opt = torch.optim.Adam(net.parameters(), lr=lr)
    history = []
    for epoch in range(epochs):
        order = torch.randperm(len(x))              # перемешиваем позиции
        total = 0.0
        for i in range(0, len(x), batch_size):
            idx = order[i:i + batch_size]
            loss = loss_fn(net, x[idx], y[idx])
            opt.zero_grad()                         # сбросить старые градиенты
            loss.backward()                         # посчитать новые
            opt.step()                              # сдвинуть веса
            total += loss.item() * len(idx)
        history.append(total / len(x))
        if epoch % 20 == 0:
            print(f"эпоха {epoch}: loss {history[-1]:.4f}")
    return net, history, (x, y)