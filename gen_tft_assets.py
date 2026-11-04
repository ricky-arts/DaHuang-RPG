#!/usr/bin/env python3
import os
import sys
import json
import re
import base64
import urllib.request
from PIL import Image

API_URL = "https://litellm.avantrobotics.ai/v1/chat/completions"
API_KEY = "sk-efqXdSIWUrvfqe8PT0ZeyddzFeuVkvPVbfAsJM8D6aENhQyo"
MODEL = "gemini-3.1-flash-image"
OUT_DIR = "/opt/cc-haha/HongHuangGame/android_src/res/drawable-nodpi"

# 严格按金铲铲之战标准体系资产定义
TASKS = [
    {
        "filename": "tft_arena_board.png",
        "prompt": "TFT style auto chess battle arena board top down view, hexagonal grid tiles on floating celestial island, oriental mythical theme, blue green jade stone tiles, ancient Taoist border runes, gold fog, cinematic fantasy game arena, clean UI readable, high resolution",
        "resize": (960, 540)
    },
    {
        "filename": "tft_card_nezha.png",
        "prompt": "TFT style champion character card portrait of Nezha, fiery lotus prince, holding flame spear, red silk ribbons floating, confident heroic anime oriental game art style, clean border card framing, high detail",
        "resize": (360, 480)
    },
    {
        "filename": "tft_card_yangjian.png",
        "prompt": "TFT style champion character card portrait of Yang Jian Erlang Shen, celestial warrior in silver armor with third divine eye, three pointed double edged spear, noble divine general, oriental game hero portrait, clean border framing",
        "resize": (360, 480)
    },
    {
        "filename": "tft_card_tongtian.png",
        "prompt": "TFT style champion character card portrait of Grandmaster Tongtian, 5 cost legendary mythical carry, four flying swords orbiting him, flowing dark robes, immense magical aura, epic game card art, sharp focus",
        "resize": (360, 480)
    },
    {
        "filename": "tft_item_sword.png",
        "prompt": "TFT item icon B.F. Sword styled in Chinese bronze mythical style, glowing blue green sharp blade, clean dark square game icon background with golden border",
        "resize": (128, 128)
    },
    {
        "filename": "tft_item_bow.png",
        "prompt": "TFT item icon Recurve Bow styled in Chinese golden solar mythical style, glowing golden string and curved limbs, clean dark square game icon with golden border",
        "resize": (128, 128)
    },
    {
        "filename": "tft_item_tear.png",
        "prompt": "TFT item icon Tear of the Goddess styled as celestial blue spirit water drop, glowing ethereal mist, clean dark square game icon with border",
        "resize": (128, 128)
    },
    {
        "filename": "tft_item_armor.png",
        "prompt": "TFT item icon Chain Vest styled as ancient Black Tortoise shell armor plate, heavy metal and stone plates, clean dark square game icon with border",
        "resize": (128, 128)
    },
    {
        "filename": "tft_item_ie.png",
        "prompt": "TFT combined item icon Infinity Edge styled as ornate golden mythical broadsword, gleaming red critical strike energy, square game icon with radiant border",
        "resize": (128, 128)
    },
    {
        "filename": "tft_item_guinsoo.png",
        "prompt": "TFT combined item icon Guinsoo Rageblade styled as fiery phoenix feathered scimitar, stacks attack speed, glowing yellow and crimson flame edges, square game icon",
        "resize": (128, 128)
    }
]

def generate_and_save(task):
    target_path = os.path.join(OUT_DIR, task["filename"])
    if os.path.exists(target_path) and os.path.getsize(target_path) > 5000:
        print(f"[EXISTS] {task['filename']} exists, skipping.")
        return True

    print(f"[GENERATE] Calling gemini-3.1-flash-image for {task['filename']} ...")
    payload = {
        "model": MODEL,
        "messages": [
            {"role": "user", "content": f"Generate a video game asset: {task['prompt']}"}
        ]
    }
    data_bytes = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(
        API_URL,
        data=data_bytes,
        headers={
            "Authorization": f"Bearer {API_KEY}",
            "Content-Type": "application/json"
        }
    )

    try:
        with urllib.request.urlopen(req, timeout=90) as resp:
            body = resp.read().decode('utf-8')
            res_json = json.loads(body)
            content = res_json['choices'][0]['message']['content']
            match = re.search(r'data:image\/[a-zA-Z]+;base64,([A-Za-z0-9+/=]+)', content)
            if match:
                raw_bytes = base64.b64decode(match.group(1))
                # 写入临时文件并通过 Pillow 优化尺寸，防止 APK 包体过大
                tmp_path = target_path + ".tmp"
                with open(tmp_path, "wb") as f:
                    f.write(raw_bytes)

                with Image.open(tmp_path) as im:
                    im = im.convert("RGBA")
                    if "resize" in task:
                        im = im.resize(task["resize"], Image.Resampling.LANCZOS)
                    im.save(target_path, "PNG", optimize=True)

                if os.path.exists(tmp_path):
                    os.remove(tmp_path)

                print(f"  [OK] Saved {task['filename']} ({os.path.getsize(target_path)} bytes)")
                return True
            else:
                print(f"  [WARN] No base64 in response for {task['filename']}")
                return False
    except Exception as e:
        print(f"  [ERR] {task['filename']}: {e}")
        return False

def main():
    print("=== 开始批量生成《大荒：洪荒弈道》纯正金铲铲自走棋体系全套美术资产 ===")
    os.makedirs(OUT_DIR, exist_ok=True)
    count = 0
    for t in TASKS:
        if generate_and_save(t):
            count += 1
    print(f"=== 完成: 成功生成并轻量化处理 {count}/{len(TASKS)} 项金铲铲核心视觉资源 ===")

if __name__ == "__main__":
    main()
