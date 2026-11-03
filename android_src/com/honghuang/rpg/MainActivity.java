package com.honghuang.rpg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {
    private LinearLayout rootContainer;
    private GameVisualView visualView;
    private LinearLayout controlPanel;

    private CharacterProfile[] profiles;
    private MapLocation[] locations;

    private int curCharIdx = 0;
    private int curMapIdx = 0;

    // 玩家运行时数值
    private CharacterProfile selectedHero;
    private int playerCurHp = 1000;
    private int playerMaxHp = 1000;
    private int playerCultivation = 0;
    private int playerRealmLevel = 1;
    private String[] realmNames = {"凡体", "练气境", "筑基境", "金丹境", "元婴境", "化神境", "合体境", "大乘境", "渡劫真仙"};

    // 敌方运行时数值
    private MapLocation currentDungeon;
    private int enemyCurHp = 1000;
    private int enemyMaxHp = 1000;
    private int enemyCurTenacity = 100;
    private int enemyMaxTenacity = 100;
    private boolean isEnemyBroken = false;
    private int breakCooldownTicks = 0;

    private TextView tvCombatFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profiles = CharacterProfile.getProfiles();
        locations = MapLocation.getDefaultLocations();

        // 根布局：纯横屏左右对称分栏架构 (左侧 55% 画面渲染，右侧 45% 交互操作)
        rootContainer = new LinearLayout(this);
        rootContainer.setOrientation(LinearLayout.HORIZONTAL);
        rootContainer.setBackgroundColor(Color.parseColor("#080D14"));

        // 左侧画板
        visualView = new GameVisualView(this);
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f
        );
        rootContainer.addView(visualView, leftLp);

        // 右侧操作与信息交互面板
        controlPanel = new LinearLayout(this);
        controlPanel.setOrientation(LinearLayout.VERTICAL);
        controlPanel.setBackgroundColor(Color.parseColor("#0F1722"));
        controlPanel.setPadding(24, 20, 24, 20);

        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 4.5f
        );
        rootContainer.addView(controlPanel, rightLp);

        setContentView(rootContainer);

        // 首发进入角色形象自选
        showCharacterSelectUI();
    }

    // ==========================================
    // 界面 1: 横屏角色形象选择
    // ==========================================
    private void showCharacterSelectUI() {
        visualView.setMode("SELECT");
        visualView.setSelectedChar(curCharIdx);
        controlPanel.removeAllViews();

        TextView tvTitle = new TextView(this);
        tvTitle.setText("【 洪荒溯源 · 抉择本源道骨形象 】");
        tvTitle.setTextSize(16);
        tvTitle.setTextColor(Color.parseColor("#D4AF37"));
        tvTitle.setGravity(Gravity.CENTER);
        controlPanel.addView(tvTitle);

        CharacterProfile hero = profiles[curCharIdx];

        TextView tvHeroName = new TextView(this);
        tvHeroName.setText(hero.name + " (" + hero.title + ")");
        tvHeroName.setTextSize(18);
        tvHeroName.setTextColor(Color.parseColor("#48D1CC"));
        tvHeroName.setPadding(0, 10, 0, 4);
        controlPanel.addView(tvHeroName);

        TextView tvHeroStats = new TextView(this);
        tvHeroStats.setText("属性：" + hero.element + " ｜ 本命：" + hero.weapon + "\n气血 " + hero.baseHp + " ｜ 攻伐 " + hero.baseAtk + " ｜ 护体 " + hero.baseDef);
        tvHeroStats.setTextColor(Color.parseColor("#A4B0BE"));
        tvHeroStats.setTextSize(12);
        controlPanel.addView(tvHeroStats);

        ScrollView sv = new ScrollView(this);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
        );
        sv.setLayoutParams(svLp);

        TextView tvHeroLore = new TextView(this);
        tvHeroLore.setText("命格宿命：" + hero.lore);
        tvHeroLore.setTextColor(Color.parseColor("#CED6E0"));
        tvHeroLore.setTextSize(12);
        tvHeroLore.setPadding(0, 8, 0, 8);
        sv.addView(tvHeroLore);
        controlPanel.addView(sv);

        // 左右形象切换
        LinearLayout navLayout = new LinearLayout(this);
        navLayout.setOrientation(LinearLayout.HORIZONTAL);
        navLayout.setGravity(Gravity.CENTER);
        navLayout.setPadding(0, 8, 0, 8);

        Button btnPrev = new Button(this);
        btnPrev.setText("◀ 前一形象");
        btnPrev.setTextSize(12);
        btnPrev.setOnClickListener(v -> {
            curCharIdx = (curCharIdx - 1 + profiles.length) % profiles.length;
            showCharacterSelectUI();
        });
        navLayout.addView(btnPrev);

        Button btnNext = new Button(this);
        btnNext.setText("后一形象 ▶");
        btnNext.setTextSize(12);
        btnNext.setOnClickListener(v -> {
            curCharIdx = (curCharIdx + 1) % profiles.length;
            showCharacterSelectUI();
        });
        navLayout.addView(btnNext);
        controlPanel.addView(navLayout);

        // 确认启程
        Button btnConfirm = new Button(this);
        btnConfirm.setText("【 锁定道果 · 踏入大荒大世界 】");
        btnConfirm.setBackgroundColor(Color.parseColor("#B71540"));
        btnConfirm.setTextColor(Color.WHITE);
        btnConfirm.setTextSize(14);
        btnConfirm.setPadding(0, 14, 0, 14);
        btnConfirm.setOnClickListener(v -> {
            selectedHero = profiles[curCharIdx];
            playerMaxHp = selectedHero.baseHp;
            playerCurHp = playerMaxHp;
            showMapExplorationUI();
        });
        controlPanel.addView(btnConfirm);
    }

    // ==========================================
    // 界面 2: 横屏四海八荒可探索大地图
    // ==========================================
    private void showMapExplorationUI() {
        visualView.setMode("MAP");
        visualView.setSelectedMap(curMapIdx);
        controlPanel.removeAllViews();

        MapLocation loc = locations[curMapIdx];

        TextView tvMapTitle = new TextView(this);
        tvMapTitle.setText("【 四海八荒图志 · 当前神域节点 】");
        tvMapTitle.setTextSize(16);
        tvMapTitle.setTextColor(Color.parseColor("#D4AF37"));
        controlPanel.addView(tvMapTitle);

        TextView tvLocName = new TextView(this);
        tvLocName.setText(loc.name + " (" + loc.zone + ")");
        tvLocName.setTextSize(18);
        tvLocName.setTextColor(Color.parseColor("#2ECC71"));
        tvLocName.setPadding(0, 6, 0, 4);
        controlPanel.addView(tvLocName);

        TextView tvDanger = new TextView(this);
        tvDanger.setText("危险等级：" + loc.dangerTier + " ｜ 凶兽：" + loc.bossName + " (属性: " + loc.bossElement + ")");
        tvDanger.setTextSize(12);
        tvDanger.setTextColor(Color.parseColor("#E74C3C"));
        controlPanel.addView(tvDanger);

        ScrollView sv = new ScrollView(this);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
        );
        sv.setLayoutParams(svLp);

        TextView tvLocDesc = new TextView(this);
        tvLocDesc.setText("秘辛志异：" + loc.description);
        tvLocDesc.setTextSize(12);
        tvLocDesc.setTextColor(Color.parseColor("#BDC3C7"));
        tvLocDesc.setPadding(0, 6, 0, 6);
        sv.addView(tvLocDesc);
        controlPanel.addView(sv);

        // 秘境切换
        LinearLayout mapNav = new LinearLayout(this);
        mapNav.setOrientation(LinearLayout.HORIZONTAL);
        mapNav.setGravity(Gravity.CENTER);
        mapNav.setPadding(0, 6, 0, 6);

        Button btnPrevMap = new Button(this);
        btnPrevMap.setText("◀ 巡视上一域");
        btnPrevMap.setTextSize(12);
        btnPrevMap.setOnClickListener(v -> {
            curMapIdx = (curMapIdx - 1 + locations.length) % locations.length;
            showMapExplorationUI();
        });
        mapNav.addView(btnPrevMap);

        Button btnNextMap = new Button(this);
        btnNextMap.setText("巡视下一域 ▶");
        btnNextMap.setTextSize(12);
        btnNextMap.setOnClickListener(v -> {
            curMapIdx = (curMapIdx + 1) % locations.length;
            showMapExplorationUI();
        });
        mapNav.addView(btnNextMap);
        controlPanel.addView(mapNav);

        // 进入战场
        Button btnEnterCombat = new Button(this);
        btnEnterCombat.setText("【 祭出法宝 · 进入秘境诛妖封神 】");
        btnEnterCombat.setBackgroundColor(Color.parseColor("#C0392B"));
        btnEnterCombat.setTextColor(Color.WHITE);
        btnEnterCombat.setTextSize(13);
        btnEnterCombat.setPadding(0, 12, 0, 12);
        btnEnterCombat.setOnClickListener(v -> {
            currentDungeon = locations[curMapIdx];
            enemyMaxHp = currentDungeon.bossHp;
            enemyCurHp = enemyMaxHp;
            enemyMaxTenacity = 100;
            enemyCurTenacity = enemyMaxTenacity;
            isEnemyBroken = false;
            showCombatUI();
        });
        controlPanel.addView(btnEnterCombat);

        // 渡劫按钮
        Button btnCultivate = new Button(this);
        btnCultivate.setText("【 洞府入定 · 引动九霄天劫渡劫 】");
        btnCultivate.setBackgroundColor(Color.parseColor("#1B1464"));
        btnCultivate.setTextColor(Color.WHITE);
        btnCultivate.setTextSize(13);
        btnCultivate.setPadding(0, 10, 0, 10);
        btnCultivate.setOnClickListener(v -> triggerTribulation());
        controlPanel.addView(btnCultivate);
    }

    // ==========================================
    // 界面 3: 横屏高挑战图形对决战场
    // ==========================================
    private void showCombatUI() {
        visualView.setMode("COMBAT");
        updateVisualCombat();
        controlPanel.removeAllViews();

        TextView tvBattleInfo = new TextView(this);
        tvBattleInfo.setText("决战：" + currentDungeon.bossName + "\n真灵：" + selectedHero.name + " (" + realmNames[playerRealmLevel] + ")");
        tvBattleInfo.setTextColor(Color.parseColor("#F1C40F"));
        tvBattleInfo.setTextSize(14);
        controlPanel.addView(tvBattleInfo);

        // 技能按键网格 (流光斩、灭神印、诛仙神威、御风撤退)
        LinearLayout skillRow1 = new LinearLayout(this);
        skillRow1.setOrientation(LinearLayout.HORIZONTAL);
        skillRow1.setGravity(Gravity.CENTER);

        Button btnAttack = new Button(this);
        btnAttack.setText("【普攻·流光斩】");
        btnAttack.setTextSize(11);
        btnAttack.setOnClickListener(v -> executePlayerAction("NORMAL"));
        skillRow1.addView(btnAttack);

        Button btnSkillBreak = new Button(this);
        btnSkillBreak.setText("【五行·灭神印】");
        btnSkillBreak.setTextSize(11);
        btnSkillBreak.setOnClickListener(v -> executePlayerAction("SKILL_BREAK"));
        skillRow1.addView(btnSkillBreak);
        controlPanel.addView(skillRow1);

        LinearLayout skillRow2 = new LinearLayout(this);
        skillRow2.setOrientation(LinearLayout.HORIZONTAL);
        skillRow2.setGravity(Gravity.CENTER);

        Button btnUltimate = new Button(this);
        btnUltimate.setText("【本命·诛仙大阵】");
        btnUltimate.setTextSize(11);
        btnUltimate.setBackgroundColor(Color.parseColor("#8E44AD"));
        btnUltimate.setTextColor(Color.WHITE);
        btnUltimate.setOnClickListener(v -> executePlayerAction("ULTIMATE"));
        skillRow2.addView(btnUltimate);

        Button btnFlee = new Button(this);
        btnFlee.setText("【御风撤退】");
        btnFlee.setTextSize(11);
        btnFlee.setOnClickListener(v -> showMapExplorationUI());
        skillRow2.addView(btnFlee);
        controlPanel.addView(skillRow2);

        ScrollView sv = new ScrollView(this);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
        );
        sv.setLayoutParams(svLp);

        tvCombatFeed = new TextView(this);
        tvCombatFeed.setTextColor(Color.parseColor("#E0E0E0"));
        tvCombatFeed.setTextSize(11);
        tvCombatFeed.setText("乾坤战阵已开！请利用五行相克与破韧机制压制凶煞！");
        sv.addView(tvCombatFeed);
        controlPanel.addView(sv);
    }

    private void executePlayerAction(String action) {
        if (enemyCurHp <= 0) return;

        int dmg = 0;
        int tenDmg = 0;
        String logText = "";
        double breakMult = isEnemyBroken ? 1.6 : 1.0;

        if ("NORMAL".equals(action)) {
            dmg = (int)(selectedHero.baseAtk * 1.0 * breakMult);
            tenDmg = 15;
            logText = "【普攻】" + selectedHero.name + " 祭起剑芒，造成 " + dmg + " 伤害！";
        } else if ("SKILL_BREAK".equals(action)) {
            dmg = (int)(selectedHero.baseAtk * 1.8 * breakMult);
            tenDmg = 45;
            logText = "【大神通】五行神光爆发造成 " + dmg + " 伤害，削减首领韧性 45点！";
        } else if ("ULTIMATE".equals(action)) {
            dmg = (int)(selectedHero.baseAtk * 3.5 * breakMult);
            tenDmg = 80;
            logText = "【本命神威】万剑齐发！对首领造成毁天灭地 " + dmg + " 暴击！";
        }

        enemyCurHp = Math.max(0, enemyCurHp - dmg);
        if (!isEnemyBroken) {
            enemyCurTenacity = Math.max(0, enemyCurTenacity - tenDmg);
            if (enemyCurTenacity <= 0) {
                isEnemyBroken = true;
                breakCooldownTicks = 3;
                logText += "\n【破韧！】首领定力护盾瓦解，陷入 BREAK 虚弱状态！全伤害增幅 60%！";
                visualView.updateCombatState(
                    (float)playerCurHp / playerMaxHp,
                    (float)enemyCurHp / enemyMaxHp,
                    0f,
                    true,
                    "【 破韧虚弱 · 伤害暴增 】"
                );
            }
        } else {
            breakCooldownTicks--;
            if (breakCooldownTicks <= 0) {
                isEnemyBroken = false;
                enemyCurTenacity = enemyMaxTenacity;
                logText += "\n【警惕】首领重聚煞气，定力护甲复原！";
            }
        }

        if (enemyCurHp > 0) {
            int bossDmg = (int)(currentDungeon.bossAtk * (isEnemyBroken ? 0.5 : 1.0));
            playerCurHp = Math.max(0, playerCurHp - bossDmg);
            logText += "\n【首领反击】" + currentDungeon.bossName + " 狂暴释放凶煞反击，我方受到 " + bossDmg + " 点伤害！";
            visualView.updateCombatState(
                (float)playerCurHp / playerMaxHp,
                (float)enemyCurHp / enemyMaxHp,
                (float)enemyCurTenacity / enemyMaxTenacity,
                isEnemyBroken,
                "-" + dmg
            );
        } else {
            playerCultivation += currentDungeon.expReward;
            logText += "\n【斩煞获胜！】" + currentDungeon.bossName + " 灰飞烟灭！汲取鸿蒙灵气 +" + currentDungeon.expReward + " 修为！";
            visualView.updateCombatState(
                (float)playerCurHp / playerMaxHp,
                0f,
                0f,
                false,
                "【 胜 利 · 斩 获 灵 韵 】"
            );
        }

        if (playerCurHp <= 0) {
            logText += "\n【身陨道消】肉身气血竭尽，神魂遁回重聚！";
            playerCurHp = playerMaxHp;
        }

        feedLog(logText);
    }

    private void triggerTribulation() {
        int required = playerRealmLevel * 600;
        if (playerCultivation >= required) {
            playerRealmLevel++;
            playerCultivation -= required;
            playerMaxHp += 400;
            playerCurHp = playerMaxHp;
            Toast.makeText(this, "雷劫降临！淬炼道骨成功，晋升至：" + realmNames[playerRealmLevel] + "！气血上限大幅提升！", Toast.LENGTH_LONG).show();
            showMapExplorationUI();
        } else {
            Toast.makeText(this, "修为尚欠缺 " + (required - playerCultivation) + "，请先进入秘境诛煞！", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateVisualCombat() {
        visualView.updateCombatState(
            (float)playerCurHp / playerMaxHp,
            (float)enemyCurHp / enemyMaxHp,
            (float)enemyCurTenacity / enemyMaxTenacity,
            isEnemyBroken,
            ""
        );
    }

    private void feedLog(String msg) {
        if (tvCombatFeed != null) {
            String old = tvCombatFeed.getText().toString();
            tvCombatFeed.setText(msg + "\n---\n" + old);
        }
    }
}
