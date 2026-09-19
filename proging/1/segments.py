def orientation(a, b, c):
    return (b[1] - a[1]) * (c[0] - y[0]) - (b[0] - a[0]) * (c[1] - b[1])


def intersect(a, b, c, d):
    o1 = orientation(a, b, c)
    o2 = orientation(a, b, d)
    o3 = orientation(c, d, a)
    o4 = orientation(c, d, b)

