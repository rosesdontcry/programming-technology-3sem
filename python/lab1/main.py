import os

from files_editor import *
from geometry import *
from visual import *


def read_write():
    pairs = read_input()
    results = []

    for pair_id, a, b in pairs:
        intersects = intersect(a, b)
        intersection = find_intersection(a,b) if intersects else None
        results.append((pair_id, a, b, intersects, intersection))

    write_output(results)
    return results


def print_pairs(results):
    print(f"{'id':<4}{'a1':<19}{'a2':<17}{'b1':<19}{'b2':<19}{'intersection':<22}")

    for pair_id, a, b, intersects, intersection, in results:
        a1_str = f"{point_to_str(a.p1)}"
        a2_str = f"{point_to_str(a.p2)}"
        b1_str = f"{point_to_str(b.p1)}"
        b2_str = f"{point_to_str(b.p2)}"

        result_str = result_format(intersection)

        print(f"{pair_id:<4}{a1_str:<19}{a2_str:<19}{b1_str:<19}{b2_str:<19}{result_str:<20}")
    print("\n\n")


def input_new_pairs():
    try:
        x1, y1 = map(float, input("a1: ").split())
        x2, y2 = map(float, input("a2: ").split())
        x3, y3 = map(float, input("b1: ").split())
        x4, y4 = map(float, input("b2: ").split())
    except ValueError:
        print("...")
        return

    segment1 = Segment(Point(x1, y1), Point(x2, y2))
    segment2 = Segment(Point(x3, y3), Point(x4, y4))

    existing_pairs = read_input()
    new_id = get_last_id(existing_pairs)

    add_new_pair(new_id, segment1, segment2)
    print(f"Пара №{new_id} добавлена.")


def main():
    results = read_write()
    os.system('cls')

    while True:
        print_pairs(results)
        print(f"id pairs - visual\n"
              f"n - add new pair\n"
              f"e - exit\n")

        choice = input(">").strip().lower()

        if choice == 'e':
            break

        elif choice == 'n':
            input_new_pairs()
            results = read_write()
            print_pairs(results)

        elif choice.isdigit():
            pair_id = int(choice)
            found = False

            for pid, a, b, intersects, intersection in results:
                if pid == pair_id:
                    plot_pair(a, b, intersection)
                    found = True
                    break
            os.system('cls')

        else:
            os.system('cls')


if __name__ == "__main__":
    main()
