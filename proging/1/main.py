import os

from files_editor import *
from geometry import *
from visual import *


def read_write():
    pairs = read_input()
    results = []

    for pair_id, a, b, in pairs:
        intersects = intersect(a, b)
        results.append((pair_id, a, b, intersects, "0"))

    write_output(results)
    return results


def print_pairs(results):
    print(f"{'id':<4}{'A1':<15}{'A2':<15}{'B1':<15}{'B2':<15}{'insect':<22}")

    for pair_id, a, b, intersects, intersection, in results:
        a1_str = f"({a.p1.x},{a.p1.y})"
        a2_str = f"({a.p2.x},{a.p2.y})"
        b1_str = f"({b.p1.x},{b.p1.y})"
        b2_str = f"({b.p2.x},{b.p2.y})"

        if not intersects:
            result_str = "нет"
        elif intersection == '0':
            result_str = f"точка (,)"
        else:
            result_str = f"отрезок (,)-(,)"

        print(f"{pair_id:<4}{a1_str:<15}{a2_str:<15}{b1_str:<15}{b2_str:<15}{result_str:<20}")
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

    append_in_input(new_id, segment1, segment2)
    print(f"Пара №{new_id} добавлена.")


def main():
    results = read_write()

    while True:
        print_pairs(results)

        print(f"id pairs = visual\n"
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
            os.system('cls')
            continue

        else:
            os.system('cls')



if __name__ == "__main__":
    main()