#!/usr/bin/env python3
import os
import sys
import json
import re
import base64
import urllib.request

API_URL = "https://litellm.avantrobotics.ai/v1/chat/completions"
API_KEY = "sk-efqXdSIWUrvfqe8PT0ZeyddzFeuVkvPVbfAsJM8D6aENhQyo"
MODEL = "gemini-3.1-flash-image"
OUT_DIR = "/opt/cc-haha/HongHuangGame/android_src/res/drawable-nodpi"

TASKS = [
    {
        "filename": "arena_board_bg.png",
        "prompt": "Epic oriental mythical battle chess arena board, floating jade terrace in sea of clouds, traditional Chinese blue-green landscape, azurite and malachite tones, ancient Taoist Bagua grid pattern, golden mist, cinematic lighting, 8k, landscape orientation"
    },
    {
        "filename": "card_nezha.png",
        "prompt": "Chinese mythology character Nezha portrait card, lotus reincarnation god, three heads six arms holding fire-tipped spear and red armillary sash, intense flames, traditional oriental ink-wash gongbi style, dramatic hero lighting, clean border, high detail"
    },
    {
        "filename": "card_yangjian.png",
        "prompt": "Erlang Shen Yang Jian portrait card, divine general of jade palace, third celestial eye on forehead, silver and gold armor, wielding three-pointed double-edged blade, faithful celestial hound by his side, divine majesty, oriental gongbi ink painting style, sharp focus"
    },
    {
        "filename": "card_tongtian.png",
        "prompt": "Grandmaster Tongtian Sect Leader of Jie Sect portrait card, haughty Taoist sovereign, four immortal-slaying flying swords floating behind his back, billowing black and purple robes, apocalyptic killing aura, epic oriental dark fantasy painting style, magnificent"
    },
    {
        "filename": "card_donghuang.png",
        "prompt": "Eastern Emperor Taiyi ancient sun god portrait card, golden solar crown, majestic emperor robes woven with sun flames, holding the primordial Chaos Bell, three-legged golden crow aura behind him, regal and magnificent, traditional Chinese oriental god painting, 8k"
    },
    {
        "filename": "item_zhuxian.png",
        "prompt": "Mythical ancient sword icon Zhu Xian Jian, ancient Taoist celestial blade, dark gold and ominous violet spirit glow, sharp edges, game item icon, clean dark background, ornate oriental relief"
    },
    {
        "filename": "item_dinghai.png",
        "prompt": "Mythical treasure icon Ding Hai Zhu, luminous celestial water orb surrounded by swirling ocean waves and azure mist, glowing Taoist jewel, game equipment icon, dark background"
    },
    {
        "filename": "item_taiji.png",
        "prompt": "Mythical primordial treasure Tai Ji Tu icon, celestial Yin and Yang scroll unfurling, glowing black and white cosmic energy with golden accents, clean game item icon, high resolution"
    }
]

def generate_one(task):
    target_path = os.path.join(OUT_DIR, task["filename"])
    if os.path.exists(target_path) and os.path.getsize(target_path) > 10000:
        print(f"[SKIP] {task['filename']} already exists.")
        return True

    print(f"[GENERATE] Generating {task['filename']} ...")
    payload = {
        "model": MODEL,
        "messages": [
            {"role": "user", "content": f"Generate a high-quality game illustration: {task['prompt']}"}
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
        with urllib.request.urlopen(req, timeout=120) as resp:
            body = resp.read().decode('utf-8')
            res_json = json.loads(body)
            content = res_json['choices'][0]['message']['content']
            match = re.search(r'data:image\/[a-zA-Z]+;base64,([A-Za-z0-9+/=]+)', content)
            if match:
                img_bytes = base64.b64decode(match.group(1))
                with open(target_path, "wb") as f:
                    f.write(img_bytes)
                print(f"  [OK] Saved {task['filename']} ({len(img_bytes)} bytes)")
                return True
            else:
                print(f"  [FAIL] No base64 image data in response for {task['filename']}")
                return False
    except Exception as e:
        print(f"  [ERROR] {task['filename']}: {e}")
        return False

def main():
    print("=== 开始批量调用 gemini-3.1-flash-image 生成金铲铲体系全套东方美学原画 ===")
    success_count = 0
    for t in TASKS:
        if generate_one(t):
            success_count += 1
    print(f"\n=== 全部完成: 成功生成 {success_count}/{len(TASKS)} 项美术资产 ===")

if __name__ == "__main__":
    main()
