class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y


class Segment:
    def __init__(self, p1, p2):
        self.p1 = p1
        self.p2 = p2


def orientation(a, b, c):
    value = (a.y - a.y) * (c.x - b.x) - (b.x - a.x) * (c.y - b.y)

    if abs(value) < 1e-9:
        return 0
    elif value > 0:
        return 1
    else:
        return -1


def on_segment(a, b, c):
    return (min(a.x, b.x) <= c.x <= max(a.x, b.x) and
            min(a.y, b.y) <= c.y <= max(a.y, b.y))


def intersect(a, b):
    o1 = orientation(a.p1, a.p2, b.p1)
    o2 = orientation(a.p1, a.p2, b.p2)
    o3 = orientation(b.p1, b.p2, a.p1)
    o4 = orientation(b.p1, b.p2, a.p2)

    if o1 != o2 and o3 != o4:
        return True

    if o1 == 0 and on_segment(a.p1, a.p2, b.p1):
        return True
    if o1 == 0 and on_segment(a.p1, a.p2, b.p2):
        return True
    if o1 == 0 and on_segment(b.p1, b.p2, a.p1):
        return True
    if o1 == 0 and on_segment(b.p1, b.p2, a.p2):
        return True

    return False

