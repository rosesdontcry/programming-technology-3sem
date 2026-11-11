import os
import ultralytics
base = os.path.dirname(ultralytics.__file__)
trackers_dir = os.path.join(base, "cfg", "trackers")
print(trackers_dir)
print(os.listdir(trackers_dir))