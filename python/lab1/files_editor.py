import csv
from geometry import Point, Segment


def point_to_float(point):
    return Point(*map(float, point.split(';')))


def point_to_str(point):
    return f"{point.x}; {point.y}"


def append_in_input(id_pairs, a, b):
    with open("input.csv", 'a') as file:
        writer = csv.writer(file)

        writer.writerow([
            id_pairs,
            point_to_str(a.p1),
            point_to_str(a.p2),
            point_to_str(b.p1),
            point_to_str(b.p2),
        ])


def read_input():
    pairs = []

    with open("input.csv", 'r') as file:
        reader = csv.DictReader(file)
        for row in reader:
            if not row.get('id'):
                continue

            pair_id = int(row['id'])
            a1 = point_to_float(row['a1'])
            a2 = point_to_float(row['a2'])
            b1 = point_to_float(row['b1'])
            b2 = point_to_float(row['b2'])

            a = Segment(a1, a2)
            b = Segment(b1, b2)

            pairs.append((pair_id, a, b))

        return pairs


def get_last_id(pairs):
    if not pairs:
        return 1
    existing_ids = [pair_id for pair_id, _, _ in pairs]
    return max(existing_ids) + 1


def format_results():
    return "YES"


def write_output(results):
    with open("output.csv", 'w') as file:
        writer = csv.writer(file)
        writer.writerow(['id', 'a1', 'a2', 'b1', 'b2', 'intersects', 'intersection'])

        for pair_id, a, b ,intersects, intersection_result in results:
            intersects_str = "DA" if intersects else "NET"
            result_str = format_results()

            writer.writerow([
                pair_id,
                point_to_str(a.p1), point_to_str(a.p2),
                point_to_str(b.p1), point_to_str(b.p2),
                intersects_str,
                result_str
            ])