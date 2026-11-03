package com.honghuang.rpg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {
    private FrameLayout rootContainer;
    private GameVisualView visualView;
    private LinearLayout panelContainer;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profiles = CharacterProfile.getProfiles();
        locations = MapLocation.getDefaultLocations();

        rootContainer = new FrameLayout(this);
        rootContainer.setBackgroundColor(Color.parseColor("#090E14"));

        visualView = new GameVisualView(this);
        rootContainer.addView(visualView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 550
        ));

        panelContainer = new LinearLayout(this);
        panelContainer.setOrientation(LinearLayout.VERTICAL);
        panelContainer.setPadding(30, 20, 30, 20);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        lp.topMargin = 550;
        rootContainer.addView(panelContainer, lp);

        setContentView(rootContainer);

        // 默认进入角色选择模式
        showCharacterSelectUI();
    }

    // ==========================================
    // 界面 1: 开局三大角色形象自选
    // ==========================================
    private void showCharacterSelectUI() {
        visualView.setMode("SELECT");
        visualView.setSelectedChar(curCharIdx);
        panelContainer.removeAllViews();

        TextView tvTitle = new TextView(this);
        tvTitle.setText("【 洪荒溯源 · 抉择本源道骨形象 】");
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(Color.parseColor("#D4AF37"));
        tvTitle.setGravity(Gravity.CENTER);
        panelContainer.addView(tvTitle);

        CharacterProfile hero = profiles[curCharIdx];

        TextView tvHeroName = new TextView(this);
        tvHeroName.setText(hero.name + " (" + hero.title + ")");
        tvHeroName.setTextSize(20);
        tvHeroName.setTextColor(Color.parseColor("#48D1CC"));
        tvHeroName.setPadding(0, 15, 0, 5);
        panelContainer.addView(tvHeroName);

        TextView tvHeroStats = new TextView(this);
        tvHeroStats.setText("灵根属性：" + hero.element + " | 初始本命：" + hero.weapon + "\n基础身魄：气血 " + hero.baseHp + " | 攻伐 " + hero.baseAtk + " | 护体 " + hero.baseDef);
        tvHeroStats.setTextColor(Color.parseColor("#A4B0BE"));
        tvHeroStats.setTextSize(13);
        panelContainer.addView(tvHeroStats);

        TextView tvHeroLore = new TextView(this);
        tvHeroLore.setText("命格宿命：" + hero.lore);
        tvHeroLore.setTextColor(Color.parseColor("#CED6E0"));
        tvHeroLore.setTextSize(12);
        tvHeroLore.setPadding(0, 10, 0, 15);
        panelContainer.addView(tvHeroLore);

        // 切换角色按钮组
        LinearLayout navLayout = new LinearLayout(this);
        navLayout.setOrientation(LinearLayout.HORIZONTAL);
        navLayout.setGravity(Gravity.CENTER);

        Button btnPrev = new Button(this);
        btnPrev.setText("◀ 前一形象");
        btnPrev.setOnClickListener(v -> {
            curCharIdx = (curCharIdx - 1 + profiles.length) % profiles.length;
            showCharacterSelectUI();
        });
        navLayout.addView(btnPrev);

        Button btnNext = new Button(this);
        btnNext.setText("后一形象 ▶");
        btnNext.setOnClickListener(v -> {
            curCharIdx = (curCharIdx + 1) % profiles.length;
            showCharacterSelectUI();
        });
        navLayout.addView(btnNext);

        panelContainer.addView(navLayout);

        // 确认启程
        Button btnConfirm = new Button(this);
        btnConfirm.setText("【 锁定道果 · 踏入大荒大世界 】");
        btnConfirm.setBackgroundColor(Color.parseColor("#B71540"));
        btnConfirm.setTextColor(Color.WHITE);
        btnConfirm.setPadding(0, 25, 0, 25);
        btnConfirm.setOnClickListener(v -> {
            selectedHero = profiles[curCharIdx];
            playerMaxHp = selectedHero.baseHp;
            playerCurHp = playerMaxHp;
            showMapExplorationUI();
        });
        panelContainer.addView(btnConfirm);
    }

    // ==========================================
    // 界面 2: 四海八荒可探索节点大地图
    // ==========================================
    private void showMapExplorationUI() {
        visualView.setMode("MAP");
        visualView.setSelectedMap(curMapIdx);
        panelContainer.removeAllViews();

        MapLocation loc = locations[curMapIdx];

        TextView tvMapTitle = new TextView(this);
        tvMapTitle.setText("【 四海八荒图志 · 当前神域节点 】");
        tvMapTitle.setTextSize(17);
        tvMapTitle.setTextColor(Color.parseColor("#D4AF37"));
        panelContainer.addView(tvMapTitle);

        TextView tvLocName = new TextView(this);
        tvLocName.setText(loc.name + " (" + loc.zone + ")");
        tvLocName.setTextSize(19);
        tvLocName.setTextColor(Color.parseColor("#2ECC71"));
        tvLocName.setPadding(0, 10, 0, 5);
        panelContainer.addView(tvLocName);

        TextView tvLocDesc = new TextView(this);
        tvLocDesc.setText("危险等阶：" + loc.dangerTier + "\n秘境异兽：" + loc.bossName + " (属性: " + loc.bossElement + ")\n秘辛志异：" + loc.description);
        tvLocDesc.setTextSize(12);
        tvLocDesc.setTextColor(Color.parseColor("#BDC3C7"));
        tvLocDesc.setPadding(0, 5, 0, 15);
        panelContainer.addView(tvLocDesc);

        // 节点切换
        LinearLayout mapNav = new LinearLayout(this);
        mapNav.setOrientation(LinearLayout.HORIZONTAL);
        mapNav.setGravity(Gravity.CENTER);

        Button btnPrevMap = new Button(this);
        btnPrevMap.setText("◀ 巡视上一域");
        btnPrevMap.setOnClickListener(v -> {
            curMapIdx = (curMapIdx - 1 + locations.length) % locations.length;
            showMapExplorationUI();
        });
        mapNav.addView(btnPrevMap);

        Button btnNextMap = new Button(this);
        btnNextMap.setText("巡视下一域 ▶");
        btnNextMap.setOnClickListener(v -> {
            curMapIdx = (curMapIdx + 1) % locations.length;
            showMapExplorationUI();
        });
        mapNav.addView(btnNextMap);

        panelContainer.addView(mapNav);

        // 进入战场挑战
        Button btnEnterCombat = new Button(this);
        btnEnterCombat.setText("【 祭出法宝 · 进入秘境诛妖封神 】");
        btnEnterCombat.setBackgroundColor(Color.parseColor("#C0392B"));
        btnEnterCombat.setTextColor(Color.WHITE);
        btnEnterCombat.setOnClickListener(v -> {
            currentDungeon = locations[curMapIdx];
            enemyMaxHp = currentDungeon.bossHp;
            enemyCurHp = enemyMaxHp;
            enemyMaxTenacity = 100;
            enemyCurTenacity = enemyMaxTenacity;
            isEnemyBroken = false;
            showCombatUI();
        });
        panelContainer.addView(btnEnterCombat);

        // 闭关洞府渡劫入口
        Button btnCultivate = new Button(this);
        btnCultivate.setText("【 洞府入定 · 引动九霄天劫渡劫 】");
        btnCultivate.setBackgroundColor(Color.parseColor("#1B1464"));
        btnCultivate.setTextColor(Color.WHITE);
        btnCultivate.setOnClickListener(v -> triggerTribulation());
        panelContainer.addView(btnCultivate);
    }

    // ==========================================
    // 界面 3: 高挑战性与策略娱乐性图形战场
    // ==========================================
    private TextView tvCombatFeed;

    private void showCombatUI() {
        visualView.setMode("COMBAT");
        updateVisualCombat();
        panelContainer.removeAllViews();

        TextView tvBattleInfo = new TextView(this);
        tvBattleInfo.setText("决战：" + currentDungeon.bossName + " ｜ 宿命真灵：" + selectedHero.name + " (" + realmNames[playerRealmLevel] + ")");
        tvBattleInfo.setTextColor(Color.parseColor("#F1C40F"));
        tvBattleInfo.setTextSize(15);
        panelContainer.addView(tvBattleInfo);

        // 技能槽按钮组 (攻击、破韧技能、本命大招、身法闪避)
        LinearLayout skillRow = new LinearLayout(this);
        skillRow.setOrientation(LinearLayout.HORIZONTAL);
        skillRow.setGravity(Gravity.CENTER);

        Button btnAttack = new Button(this);
        btnAttack.setText("【普攻·流光斩】");
        btnAttack.setOnClickListener(v -> executePlayerAction("NORMAL"));
        skillRow.addView(btnAttack);

        Button btnSkillBreak = new Button(this);
        btnSkillBreak.setText("【五行·灭神印】");
        btnSkillBreak.setOnClickListener(v -> executePlayerAction("SKILL_BREAK"));
        skillRow.addView(btnSkillBreak);

        panelContainer.addView(skillRow);

        LinearLayout skillRow2 = new LinearLayout(this);
        skillRow2.setOrientation(LinearLayout.HORIZONTAL);
        skillRow2.setGravity(Gravity.CENTER);

        Button btnUltimate = new Button(this);
        btnUltimate.setText("【本命·诛仙辟易大阵】");
        btnUltimate.setBackgroundColor(Color.parseColor("#8E44AD"));
        btnUltimate.setTextColor(Color.WHITE);
        btnUltimate.setOnClickListener(v -> executePlayerAction("ULTIMATE"));
        skillRow2.addView(btnUltimate);

        Button btnFlee = new Button(this);
        btnFlee.setText("【御风撤退】");
        btnFlee.setOnClickListener(v -> showMapExplorationUI());
        skillRow2.addView(btnFlee);

        panelContainer.addView(skillRow2);

        TextView tvLogTitle = new TextView(this);
        tvLogTitle.setText("【战局实时博弈推演】");
        tvLogTitle.setTextSize(13);
        tvLogTitle.setTextColor(Color.parseColor("#BDC3C7"));
        tvLogTitle.setPadding(0, 10, 0, 5);
        panelContainer.addView(tvLogTitle);

        ScrollView sv = new ScrollView(this);
        tvCombatFeed = new TextView(this);
        tvCombatFeed.setTextColor(Color.parseColor("#E0E0E0"));
        tvCombatFeed.setTextSize(12);
        tvCombatFeed.setText("乾坤战阵已开！请利用五行相克与破韧机制压制凶煞！");
        sv.addView(tvCombatFeed);
        panelContainer.addView(sv);
    }

    private void executePlayerAction(String action) {
        if (enemyCurHp <= 0) return;

        int dmg = 0;
        int tenDmg = 0;
        String logText = "";

        // 破韧状态倍率
        double breakMult = isEnemyBroken ? 1.6 : 1.0;

        if ("NORMAL".equals(action)) {
            dmg = (int)(selectedHero.baseAtk * 1.0 * breakMult);
            tenDmg = 15;
            logText = "【普攻】" + selectedHero.name + " 祭起剑芒，对首领造成 " + dmg + " 点真气伤害！";
        } else if ("SKILL_BREAK".equals(action)) {
            dmg = (int)(selectedHero.baseAtk * 1.8 * breakMult);
            tenDmg = 45;
            logText = "【大神通】五行相克引动天地雷罚，重击首领造成 " + dmg + " 伤害，削减其定力韧性 45 点！";
        } else if ("ULTIMATE".equals(action)) {
            dmg = (int)(selectedHero.baseAtk * 3.5 * breakMult);
            tenDmg = 80;
            logText = "【本命神威】万剑齐发！虚空轰鸣对首领造成毁天灭地 " + dmg + " 斩杀暴击！";
        }

        // 扣除敌方气血与韧性
        enemyCurHp = Math.max(0, enemyCurHp - dmg);
        if (!isEnemyBroken) {
            enemyCurTenacity = Math.max(0, enemyCurTenacity - tenDmg);
            if (enemyCurTenacity <= 0) {
                isEnemyBroken = true;
                breakCooldownTicks = 3; // 虚弱3回合
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
                logText += "\n【警惕】首领重聚煞气，定力护甲重新复原！";
            }
        }

        // 首领反击
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
            // 敌方战殁
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
            logText += "\n【身陨道消】肉身气血竭尽，神魂遁回终南山重聚！";
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
            Toast.makeText(this, "修为尚欠缺 " + (required - playerCultivation) + "，请先进入秘境挑战诛煞！", Toast.LENGTH_SHORT).show();
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
