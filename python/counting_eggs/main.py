import cv2
import time
from ultralytics import YOLO

model = YOLO('best.pt')

cap = cv2.VideoCapture(1, cv2.CAP_DSHOW)

if not cap.isOpened():
    exit()

CONFIRM_FRAMES = 8  # debounce-порог, пока предварительный — скорректируем по замеру FPS
confirmed_ids = set()
pending_ids = {}  # track_id -> подряд увиденных кадров

prev_time = time.time()
fps = 0.0

while True:
    ret, frame = cap.read()
    if not ret:
        print("Кадр не получен")
        break

    results = model.track(frame, persist=True, conf=0.5, tracker='trackers/my_tracktrack.yaml')
    annotated_frame = results[0].plot()

    current_ids = set()
    if results[0].boxes is not None and results[0].boxes.id is not None:
        current_ids = set(int(i) for i in results[0].boxes.id.cpu().tolist())

    for tid in current_ids:
        pending_ids[tid] = pending_ids.get(tid, 0) + 1
        if pending_ids[tid] >= CONFIRM_FRAMES and tid not in confirmed_ids:
            confirmed_ids.add(tid)

    disappeared = set(pending_ids.keys()) - current_ids
    for tid in disappeared:
        del pending_ids[tid]

    now = time.time()
    dt = now - prev_time
    prev_time = now
    if dt > 0:
        fps = 0.9 * fps + 0.1 * (1.0 / dt)  # сглаженный FPS

    count = len(confirmed_ids)

    cv2.putText(annotated_frame, f"Eggs: {count}", (20, 50),
                cv2.FONT_HERSHEY_SIMPLEX, 1.3, (0, 255, 0), 3)
    #cv2.putText(annotated_frame, f"FPS: {fps:.1f}", (20, 90),
    #            cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 200, 255), 2)

    cv2.imshow("Eggs", annotated_frame)

    key = cv2.waitKey(1) & 0xFF
    if key == ord('q'):
        break
    elif key == ord('r'):
        confirmed_ids.clear()
        pending_ids.clear()
        print("Счётчик сброшен")

cap.release()
cv2.destroyAllWindows()