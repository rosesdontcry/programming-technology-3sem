from ultralytics import YOLO

if __name__ == '__main__':
    model = YOLO('yolov8n.pt')
    results = model.train(
        data='datasets/eggs_v2/data.yaml',
        epochs=100,
        patience=20,
        imgsz=640,
        batch=16,
        device=0,
        amp=False,
        project='runs/detect',
        name='eggs_v2_run1'
    )