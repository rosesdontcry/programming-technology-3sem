import matplotlib
matplotlib.use('QtAgg')

import matplotlib.pyplot as plt
from geometry import Point, Segment


def plot_pair(a, b, intersection=None):
    fig, ax = plt.subplots()

    x1 = [a.p1.x, a.p2.x]
    y1 = [a.p1.y, a.p2.y]
    ax.plot(x1, y1, label="a", linewidth=2)

    x2 = [b.p1.x, b.p2.x]
    y2 = [b.p1.y, b.p2.y]
    ax.plot(x2, y2, label="b", linewidth=2)


    if isinstance(intersection, Point):
        ax.scatter(intersection.x, intersection.y, label="intersection", zorder=5, color='k')

    elif isinstance(intersection, Segment):
        x_overlap = [intersection.p1.x, intersection.p2.x]
        y_overlap = [intersection.p1.y, intersection.p2.y]
        ax.plot(x_overlap, y_overlap, label="intersection", zorder=5, color='k', linewidth=3, marker='o')
        title = "Intersection"



    plt.tight_layout()
    plt.show()
