import os
import glob
import hashlib
from collections import defaultdict
import cv2
import yaml

DATASET_DIR = "datasets/eggs_v2"  # поправь под свой путь

def load_data_yaml(path):
    with open(os.path.join(path, "data.yaml"), "r") as f:
        return yaml.safe_load(f)

def check_split(split_dir, img_dir_name="images", lbl_dir_name="labels"):
    img_dir = os.path.join(split_dir, img_dir_name)
    lbl_dir = os.path.join(split_dir, lbl_dir_name)

    images = glob.glob(os.path.join(img_dir, "*"))
    print(f"\n=== {split_dir} ===")
    print(f"Всего изображений: {len(images)}")

    # 1. Поиск дубликатов файлов по хэшу содержимого
    hashes = defaultdict(list)
    for img_path in images:
        with open(img_path, "rb") as f:
            h = hashlib.md5(f.read()).hexdigest()
        hashes[h].append(img_path)

    dupes = {h: paths for h, paths in hashes.items() if len(paths) > 1}
    if dupes:
        print(f"\n⚠ Найдены ТОЧНЫЕ дубликаты файлов ({len(dupes)} групп):")
        for h, paths in dupes.items():
            print("  ", paths)
    else:
        print("Точных дубликатов по хэшу не найдено.")

    # 2. Анализ боксов: размер, пропорции, количество
    no_label_images = []
    suspicious_boxes = []
    box_count_per_image = []

    for img_path in images:
        fname = os.path.splitext(os.path.basename(img_path))[0]
        lbl_path = os.path.join(lbl_dir, fname + ".txt")

        img = cv2.imread(img_path)
        if img is None:
            print(f"⚠ Не удалось прочитать изображение: {img_path}")
            continue
        h_img, w_img = img.shape[:2]

        if not os.path.exists(lbl_path) or os.path.getsize(lbl_path) == 0:
            no_label_images.append(img_path)
            box_count_per_image.append(0)
            continue

        with open(lbl_path, "r") as f:
            lines = [l.strip() for l in f if l.strip()]

        box_count_per_image.append(len(lines))

        for line in lines:
            parts = line.split()
            cls_id, xc, yc, w, h = int(parts[0]), *map(float, parts[1:5])

            box_w_px = w * w_img
            box_h_px = h * h_img
            area_ratio = (w * h)  # доля площади кадра
            aspect = box_w_px / box_h_px if box_h_px > 0 else 0

            # Эвристики подозрительности:
            # - бокс занимает слишком большую долю кадра (яйцо редко > 60% кадра)
            # - бокс сильно вытянут (яйцо примерно 0.7-1.3 по аспекту, не 3:1 и не 1:3)
            flags = []
            if area_ratio > 0.5:
                flags.append(f"очень большая площадь ({area_ratio:.2f} от кадра)")
            if aspect > 1.8 or aspect < 0.55:
                flags.append(f"нетипичный aspect ratio ({aspect:.2f})")
            if area_ratio < 0.0005:
                flags.append(f"подозрительно маленький бокс ({area_ratio:.5f} от кадра)")

            if flags:
                suspicious_boxes.append((img_path, flags))

    print(f"\nИзображений без разметки (потенциальные hard negatives): {len(no_label_images)}")
    for p in no_label_images[:20]:
        print("  ", p)
    if len(no_label_images) > 20:
        print(f"  ... и ещё {len(no_label_images) - 20}")

    print(f"\n⚠ Подозрительные боксы (нетипичная форма/размер): {len(suspicious_boxes)}")
    for p, flags in suspicious_boxes:
        print(f"  {p}: {flags}")

    print(f"\nРаспределение числа боксов на фото:")
    from collections import Counter
    c = Counter(box_count_per_image)
    for k in sorted(c.keys()):
        print(f"  {k} боксов: {c[k]} фото")


if __name__ == "__main__":
    data = load_data_yaml(DATASET_DIR)
    print("Классы в data.yaml:", data.get("names"))

    for split_folder in ["train", "valid", "test"]:
        full_path = os.path.join(DATASET_DIR, split_folder)
        if os.path.exists(full_path):
            check_split(full_path)
        else:
            print(f"Папка {split_folder} не найдена, пропускаю")