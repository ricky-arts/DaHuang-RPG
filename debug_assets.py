import zipfile

with zipfile.ZipFile('/opt/cc-haha/HongHuangGame/Releases/DaHuang-v4.0.0-TFT.apk') as z:
    for name in z.namelist():
        if 'png' in name:
            print(name)
