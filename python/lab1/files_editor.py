import csv
from geometry import Point, Segment


def point_to_float(point):
    return Point(*map(float, point.split(';')))

def format_number(num):
    if num == int(num):
        return str(int(num))
    return str(num)

def point_to_str(point):
    return f"({format_number(point.x)}; {format_number(point.y)})"


def add_new_pair(pair_id, a, b):
    with open("input.csv", 'a', newline='') as file:
        writer = csv.writer(file)

        writer.writerow([
            pair_id,
            point_to_str(a.p1),
            point_to_str(a.p2),
            point_to_str(b.p1),
            point_to_str(b.p2),
        ])


def read_input():
    pairs = []

    with open("input.csv", 'r', newline='') as file:
        reader = csv.reader(file)
        next(reader)

        for row in reader:
            pair_id = int(row[0])
            a1 = point_to_float(row[1])
            a2 = point_to_float(row[2])
            b1 = point_to_float(row[3])
            b2 = point_to_float(row[4])

            a = Segment(a1, a2)
            b = Segment(b1, b2)

            pairs.append((pair_id, a, b))

        return pairs


def get_last_id(pairs):
    if not pairs:
        return 0
    existing_ids = [pair_id for pair_id, _, _ in pairs]
    return max(existing_ids)


def result_format(intersection):
    if intersection is None:
        return '-'
    elif isinstance(intersection, Point):
        return point_to_str(intersection)
    elif isinstance(intersection, Segment):
        return f"{point_to_str(intersection.p1)}-{point_to_str(intersection.p2)}"
    else:
        return "hz"


def write_output(results):
    with open("output.csv", 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(['id', 'a1', 'a2', 'b1', 'b2', 'intersects', 'intersection'])

        for pair_id, a, b, intersects, intersection in results:
            intersects_str = "da" if intersects else "net"
            result_str = result_format(intersection)

            writer.writerow([
                pair_id,
                point_to_str(a.p1), point_to_str(a.p2),
                point_to_str(b.p1), point_to_str(b.p2),
                intersects_str,
                result_str
            ])
