class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def __eq__(self, other):
        return self.x == other.x and self.y == other.y


class Segment:
    def __init__(self, p1, p2):
        self.p1 = p1
        self.p2 = p2


def cross(a, b, c):
    value = (b.y - a.y) * (c.x - b.x) - (b.x - a.x) * (c.y - b.y)

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
    o1 = cross(a.p1, a.p2, b.p1)
    o2 = cross(a.p1, a.p2, b.p2)
    o3 = cross(b.p1, b.p2, a.p1)
    o4 = cross(b.p1, b.p2, a.p2)

    if o1 != o2 and o3 != o4:
        return True

    if o1 == 0 and on_segment(a.p1, a.p2, b.p1):
        return True
    if o2 == 0 and on_segment(a.p1, a.p2, b.p2):
        return True
    if o3 == 0 and on_segment(b.p1, b.p2, a.p1):
        return True
    if o4 == 0 and on_segment(b.p1, b.p2, a.p2):
        return True

    return False


def find_intersection(a, b):
    x1, y1 = a.p1.x, a.p1.y
    x2, y2 = a.p2.x, a.p2.y

    x3, y3 = b.p1.x, b.p1.y
    x4, y4 = b.p2.x, b.p2.y

    denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)

    if abs(denom) < 1e-9:
        return find_collinear_overlap(a, b)

    t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom
    s = ((x1 - x3) * (y1 - y2) - (y1 - y3) * (x1 - x2)) / denom

    if 0 <= t <= 1 and 0 <= s <= 1:
        x_int = x1 + t * (x2 - x1)
        y_int = y1 + t * (y2 - y1)
        return Point(round(x_int, 6), round(y_int, 6))

    return None


def find_collinear_overlap(a, b):
    if cross(a.p1, a.p2, b.p1) != 0 or cross(a.p1, a.p2, b.p2) != 0:
        return None

    if a.p1.x != a.p2.x:
        points = sorted([a.p1, a.p2, b.p1, b.p2], key=lambda p: p.x)
    else:
        points = sorted([a.p1, a.p2, b.p1, b.p2], key=lambda p: p.y)

    p_start, p_end = points[1], points[2]

    # Проверяем, что p_start и p_end реально лежат на ОБОИХ отрезках
    if not (on_segment(a.p1, a.p2, p_start) and on_segment(b.p1, b.p2, p_start)):
        return None
    if not (on_segment(a.p1, a.p2, p_end) and on_segment(b.p1, b.p2, p_end)):
        return None

    if p_start == p_end:
        return p_start

    return Segment(p_start, p_end)