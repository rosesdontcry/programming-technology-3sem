def draw_board(board):
    for i in range(len(board)):
        print(' —— '.join(['+' if x == 0 else ('⬤' if x == 1 else '⬤') for x in board[i]]))
        if i != len(board) - 1:
            print('|    ' * len(board[i])   )
