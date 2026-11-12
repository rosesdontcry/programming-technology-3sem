def render(state):
    rows = []
    for row in range(3):
        cells = []
        for col in range(3):
            value = state.board[row, col]
            if value == 1:
                cells.append("X")
            elif value == -1:
                cells.append("O")
            elif value == 0:
                cells.append(f"{3 * row + col}")
            else:
                raise ValueError(f"i donk know what is it: {value}")

        rows.append(" | ".join(cells))

    return "\n--+---+--\n".join(rows)
