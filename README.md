# 《大荒》（DaHuang）- 东方神话洪荒单机安卓RPG

《大荒》是一款以中国上古神话洪荒世界为背景的单机动作角色扮演（RPG）安卓游戏。玩家将扮演开天辟地后遁去的一缕变数真灵，在天道、地道与人道三才激荡的洪荒大劫中修真证道、历经天劫、降伏神魔、炼就本命法宝，最终决定洪荒天地走向。

---

## 一、 游戏特色与核心系统

1. **东方美学与国风神韵**
   - 视觉采用中国传统青绿矿彩与工笔水墨流云设色（石青、石绿、赭石、丹砂、玄黑）；
   - 人物形制承袭魏晋道骨与上古神威，界面计白当黑、虚实相生。

2. **即时动作战斗与五行生克**
   - **五行生克矩阵**：金克木（破甲） $\rightarrow$ 木克土（吸髓） $\rightarrow$ 土克水（重力） $\rightarrow$ 水克火（冰涸） $\rightarrow$ 火克金（灼伤）；
   - **破韧机制**：削减定力韧性，破韧后陷入 4 秒虚弱（Break 增伤 50%）；
   - **极限闪避**：时空减速「刹那神念」，刷新身法位移。

3. **修真九境与因果天劫**
   - 练气 $\rightarrow$ 筑基 $\rightarrow$ 金丹 $\rightarrow$ 元婴 $\rightarrow$ 化神 $\rightarrow$ 炼虚 $\rightarrow$ 合体 $\rightarrow$ 大乘 $\rightarrow$ 渡劫 $\rightarrow$ 混元大罗金仙 $\rightarrow$ 天道境；
   - 善恶因果值影响雷劫威能：业障深重降临九幽寂灭紫霄黑雷，功德高深引动太乙度厄青雷；
   - 心魔镜像决战与因果业镜。

4. **装备词缀与本命法宝养成**
   - 8 大品质（凡器至混沌灵宝）与 8 大槽位；
   - 诛仙飞剑、翻天宝印、山河宝鼎、诸天阵旗、太古遗音五大本命器胚；
   - 精血祭炼唤醒器灵，异火淬炼 0~10 星，随大境界突破 1~9 阶并觉醒专属神威大招。

5. **四大史诗终局分支**
   - **封神永昌**（顺应天道·执掌天庭）
   - **重立地仙**（合道地书·人人如龙）
   - **太初破虚**（独断万古·飞升混沌）
   - **万劫俱灭**（身化魔尊·重启洪荒）

6. **单机安全与防篡改架构**
   - 内存动态随机异或掩码（`ObfuscatedValue`），防止内存修改器搜索；
   - 硬件指纹派生 AES-256 + HMAC-SHA256 完整性防篡改本地存档；
   - A/B 双缓冲原子写盘，防止切后台或低电量断电坏档。

---

## 二、 工程目录结构

```
HongHuangGame/
├── Assets/_Project/
│   ├── AndroidManifest.xml                        # 安卓平台清单文件 (Package: com.honghuang.rpg)
│   ├── Scripts/
│   │   ├── Bootstrap/GameBootstrapper.cs          # 游戏入口与全局引导
│   │   ├── Core/                                  # 事件总线、对象池、防修改、AES-256存档引擎
│   │   ├── Gameplay/                              # 战斗引擎、AI行为树、境界管理、本命法宝、剧情树
│   │   ├── Data/Tables/TableManager.cs            # 只读静态配置表驱动器
│   │   ├── UI/                                    # 导航栈 UIManager 与核心面板
│   │   └── Shaders/WaterInkAura.shader            # 国风水墨气劲流光着色器
│   └── AddressableAssets/                         # 可寻址资源根目录
├── Configs/                                       # 角色模版与神通技能数据表
├── ProjectSettings/PlayerSettings.json            # Unity IL2CPP、ARM64、横屏配置
└── Tests/                                         # 自动化单元测试与端到端回归套件
```

---

## 三、 自动化自验证

项目包含完备的自动化测试套件：
- `verify_core_logic.ts`：测试防修改数值、AES-256/HMAC 存储安全、五行生克与非线性境界压制；
- `verify_full_system.ts`：测试本命法宝祭炼成长、山海图志收录、四大终局及双曲防御无溢出；
- `verify_end_to_end.ts`：测试 BOSS 行为树战术流、配置表驱动与 UI 导航栈；
- `verify_runtime_playable.ts`：测试移动端摇杆输入、音频、击杀结算、突破及切后台原子存盘。

运行指令：
```bash
bun run Tests/verify_core_logic.ts
bun run Tests/verify_full_system.ts
bun run Tests/verify_end_to_end.ts
bun run Tests/verify_runtime_playable.ts
```
所有测试均通过（100% PASS）。
