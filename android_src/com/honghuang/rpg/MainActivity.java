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

    // 玩家修真境界与升级模型 (练气一阶 ~ 渡劫真仙九阶)
    private CultivationRealm realm = new CultivationRealm(0, 1); // 初始：练气一阶
    private CharacterProfile selectedHero;
    private int playerCurHp = 1000;
    private int playerMaxHp = 1000;
    private int playerAttack = 180;
    private int playerDefense = 90;

    // 怪物与秘境
    private MapLocation currentDungeon;
    private MonsterEntity[] areaMonsters;
    private int curMonsterIdx = 0;
    private MonsterEntity curMonster;

    private int breakCooldownTicks = 0;
    private TextView tvCombatFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profiles = CharacterProfile.getProfiles();
        locations = MapLocation.getDefaultLocations();

        rootContainer = new LinearLayout(this);
        rootContainer.setOrientation(LinearLayout.HORIZONTAL);
        rootContainer.setBackgroundColor(Color.parseColor("#080D14"));

        visualView = new GameVisualView(this);
        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f
        );
        rootContainer.addView(visualView, leftLp);

        controlPanel = new LinearLayout(this);
        controlPanel.setOrientation(LinearLayout.VERTICAL);
        controlPanel.setBackgroundColor(Color.parseColor("#0F1722"));
        controlPanel.setPadding(24, 18, 24, 18);

        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 4.5f
        );
        rootContainer.addView(controlPanel, rightLp);

        setContentView(rootContainer);
        showCharacterSelectUI();
    }

    // ==========================================
    // 界面 1: 开局角色道骨形象自选
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
        tvHeroStats.setText("属性：" + hero.element + " ｜ 本命：" + hero.weapon + "\n初始身魄：气血 " + hero.baseHp + " ｜ 攻伐 " + hero.baseAtk + " ｜ 护体 " + hero.baseDef);
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

        Button btnConfirm = new Button(this);
        btnConfirm.setText("【 锁定道果 · 踏入大荒大世界 】");
        btnConfirm.setBackgroundColor(Color.parseColor("#B71540"));
        btnConfirm.setTextColor(Color.WHITE);
        btnConfirm.setTextSize(14);
        btnConfirm.setPadding(0, 14, 0, 14);
        btnConfirm.setOnClickListener(v -> {
            selectedHero = profiles[curCharIdx];
            applyRealmStats();
            showMapExplorationUI();
        });
        controlPanel.addView(btnConfirm);
    }

    private void applyRealmStats() {
        double mult = realm.getStatMultiplier();
        playerMaxHp = (int)(selectedHero.baseHp * mult);
        playerCurHp = playerMaxHp;
        playerAttack = (int)(selectedHero.baseAtk * mult);
        playerDefense = (int)(selectedHero.baseDef * mult);
    }

    // ==========================================
    // 界面 2: 四海八荒可探索大地图与怪物梯度
    // ==========================================
    private void showMapExplorationUI() {
        visualView.setMode("MAP");
        visualView.setSelectedMap(curMapIdx);
        controlPanel.removeAllViews();

        MapLocation loc = locations[curMapIdx];
        areaMonsters = MonsterEntity.getAreaMonsters(loc.id);

        TextView tvMapTitle = new TextView(this);
        tvMapTitle.setText("【 四海八荒图志 · 当前神域节点 】");
        tvMapTitle.setTextSize(16);
        tvMapTitle.setTextColor(Color.parseColor("#D4AF37"));
        controlPanel.addView(tvMapTitle);

        TextView tvLocName = new TextView(this);
        tvLocName.setText(loc.name + " (" + loc.zone + ")");
        tvLocName.setTextSize(18);
        tvLocName.setTextColor(Color.parseColor("#2ECC71"));
        tvLocName.setPadding(0, 6, 0, 2);
        controlPanel.addView(tvLocName);

        TextView tvPlayerInfo = new TextView(this);
        tvPlayerInfo.setText("道主：" + selectedHero.name + " ｜ 境界：" + realm.getFullRealmTitle() + " ｜ 气血:" + playerCurHp + "/" + playerMaxHp);
        tvPlayerInfo.setTextColor(Color.parseColor("#F1C40F"));
        tvPlayerInfo.setTextSize(12);
        controlPanel.addView(tvPlayerInfo);

        TextView tvExpInfo = new TextView(this);
        tvExpInfo.setText("当前修为进度：" + realm.getCurrentExp() + " / " + realm.getExpNeededForNextStage());
        tvExpInfo.setTextColor(Color.parseColor("#3498DB"));
        tvExpInfo.setTextSize(12);
        controlPanel.addView(tvExpInfo);

        // 怪物梯度选择列表 (普通 / 精英 / BOSS)
        TextView tvMonsterHeader = new TextView(this);
        tvMonsterHeader.setText("【 区域异兽凶险梯度 (点击挑选对手) 】");
        tvMonsterHeader.setTextColor(Color.parseColor("#E74C3C"));
        tvMonsterHeader.setTextSize(12);
        tvMonsterHeader.setPadding(0, 8, 0, 4);
        controlPanel.addView(tvMonsterHeader);

        LinearLayout monsterList = new LinearLayout(this);
        monsterList.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < areaMonsters.length; i++) {
            final int idx = i;
            MonsterEntity m = areaMonsters[i];
            Button btnM = new Button(this);
            btnM.setText("【" + m.tier.title + "】" + m.name + " (" + m.element + "系 | 气血:" + m.maxHp + ")");
            btnM.setTextSize(11);
            btnM.setBackgroundColor(idx == curMonsterIdx ? Color.parseColor("#B71540") : Color.parseColor("#1C2A38"));
            btnM.setTextColor(Color.WHITE);
            btnM.setOnClickListener(v -> {
                curMonsterIdx = idx;
                showMapExplorationUI();
            });
            monsterList.addView(btnM);
        }
        controlPanel.addView(monsterList);

        // 地图切换
        LinearLayout mapNav = new LinearLayout(this);
        mapNav.setOrientation(LinearLayout.HORIZONTAL);
        mapNav.setGravity(Gravity.CENTER);
        mapNav.setPadding(0, 6, 0, 6);

        Button btnPrevMap = new Button(this);
        btnPrevMap.setText("◀ 上一域");
        btnPrevMap.setTextSize(11);
        btnPrevMap.setOnClickListener(v -> {
            curMapIdx = (curMapIdx - 1 + locations.length) % locations.length;
            curMonsterIdx = 0;
            showMapExplorationUI();
        });
        mapNav.addView(btnPrevMap);

        Button btnNextMap = new Button(this);
        btnNextMap.setText("下一域 ▶");
        btnNextMap.setTextSize(11);
        btnNextMap.setOnClickListener(v -> {
            curMapIdx = (curMapIdx + 1) % locations.length;
            curMonsterIdx = 0;
            showMapExplorationUI();
        });
        mapNav.addView(btnNextMap);
        controlPanel.addView(mapNav);

        // 出战按钮
        Button btnEnterCombat = new Button(this);
        btnEnterCombat.setText("【 祭出法宝 · 迎战所选异兽 】");
        btnEnterCombat.setBackgroundColor(Color.parseColor("#C0392B"));
        btnEnterCombat.setTextColor(Color.WHITE);
        btnEnterCombat.setTextSize(13);
        btnEnterCombat.setPadding(0, 10, 0, 10);
        btnEnterCombat.setOnClickListener(v -> {
            currentDungeon = locations[curMapIdx];
            MonsterEntity template = areaMonsters[curMonsterIdx];
            // 实例化新怪物
            curMonster = new MonsterEntity(
                template.name, template.tier, template.element,
                template.maxHp, template.attack, template.defense,
                template.expReward, template.specialAffix
            );
            showCombatUI();
        });
        controlPanel.addView(btnEnterCombat);

        // 渡劫按钮
        if (realm.canTriggerTribulation()) {
            Button btnTribulation = new Button(this);
            btnTribulation.setText("⚡【九阶大圆满 · 引动天劫大突破】⚡");
            btnTribulation.setBackgroundColor(Color.parseColor("#8E44AD"));
            btnTribulation.setTextColor(Color.parseColor("#F1C40F"));
            btnTribulation.setTextSize(13);
            btnTribulation.setOnClickListener(v -> triggerMajorTribulation());
            controlPanel.addView(btnTribulation);
        }
    }

    // ==========================================
    // 界面 3: 高燃动作技能、真实属性扣减战斗
    // ==========================================
    private void showCombatUI() {
        visualView.setMode("COMBAT");
        visualView.setMonsterInfo(curMonster.tier, curMonster.name);
        updateVisualCombat();
        controlPanel.removeAllViews();

        TextView tvBattleInfo = new TextView(this);
        tvBattleInfo.setText("对阵：" + curMonster.name + " (" + curMonster.element + "系·" + curMonster.tier.title + ")\n道主：" + selectedHero.name + " (" + realm.getFullRealmTitle() + ")");
        tvBattleInfo.setTextColor(Color.parseColor("#F1C40F"));
        tvBattleInfo.setTextSize(13);
        controlPanel.addView(tvBattleInfo);

        // 技能槽按钮组 (攻击、破韧技能、本命大招、撤退)
        LinearLayout skillRow1 = new LinearLayout(this);
        skillRow1.setOrientation(LinearLayout.HORIZONTAL);
        skillRow1.setGravity(Gravity.CENTER);

        Button btnAttack = new Button(this);
        btnAttack.setText("【普攻·流光斩】");
        btnAttack.setTextSize(11);
        btnAttack.setOnClickListener(v -> executeCombatRound("NORMAL"));
        skillRow1.addView(btnAttack);

        Button btnSkillBreak = new Button(this);
        btnSkillBreak.setText("【五行·灭神印】");
        btnSkillBreak.setTextSize(11);
        btnSkillBreak.setOnClickListener(v -> executeCombatRound("SKILL_BREAK"));
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
        btnUltimate.setOnClickListener(v -> executeCombatRound("ULTIMATE"));
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
        tvCombatFeed.setText("战阵已启！属性克制增伤35%，请全力破韧！");
        sv.addView(tvCombatFeed);
        controlPanel.addView(sv);
    }

    /**
     * 执行真实属性与五行生克伤害扣减逻辑
     */
    private void executeCombatRound(String skillType) {
        if (curMonster.curHp <= 0) return;

        double skillRate = 1.0;
        String skillElem = selectedHero.element.contains("木") ? "木" : (selectedHero.element.contains("火") ? "火" : "土");
        String animAction = "SLASH";

        if ("NORMAL".equals(skillType)) {
            skillRate = 1.1;
            animAction = "SLASH";
        } else if ("SKILL_BREAK".equals(skillType)) {
            skillRate = 1.9;
            animAction = "CAST";
        } else if ("ULTIMATE".equals(skillType)) {
            skillRate = 3.6;
            animAction = "ULTIMATE";
        }

        // 1. 严格双曲公式结算角色对怪物伤害
        BattleCalculator.HitResult hit = BattleCalculator.calculateDamage(
            playerAttack,
            skillRate,
            skillElem,
            curMonster.defense,
            curMonster.element,
            curMonster.isBroken,
            0.15
        );

        // 触发角色动作与技能飞天弹道特效
        visualView.triggerCombatSkillAnimation(animAction, "-" + hit.finalDamage + (hit.isCrit ? " 暴击!" : ""));

        // 扣减怪物血量与定力
        curMonster.curHp = Math.max(0, curMonster.curHp - hit.finalDamage);
        if (!curMonster.isBroken && curMonster.maxTenacity > 0) {
            curMonster.curTenacity = Math.max(0, curMonster.curTenacity - hit.tenacityDamage);
            if (curMonster.curTenacity <= 0) {
                curMonster.isBroken = true;
                breakCooldownTicks = 3;
            }
        } else if (curMonster.isBroken) {
            breakCooldownTicks--;
            if (breakCooldownTicks <= 0) {
                curMonster.isBroken = false;
                curMonster.curTenacity = curMonster.maxTenacity;
            }
        }

        StringBuilder log = new StringBuilder();
        log.append("【攻伐】").append(selectedHero.name).append(" 施展招式，造成 ")
           .append(hit.finalDamage).append(" 点真实伤害！").append(hit.elemDescription)
           .append(hit.isCrit ? "【暴击】" : "");

        if (curMonster.isBroken) {
            log.append("\n【破韧！】首领韧性护盾瓦解，陷入 BREAK 虚弱（增伤50%）！");
        }

        // 2. 检查怪物死亡并触发自动结算！
        if (curMonster.curHp <= 0) {
            updateVisualCombat();
            feedLog(log.toString());
            // 自动触发战捷胜利结算弹窗！
            showBattleVictoryOverlay();
            return;
        }

        // 3. 怪物对角色反击（同样按双曲减伤逻辑真实扣减）
        BattleCalculator.HitResult monsterHit = BattleCalculator.calculateDamage(
            curMonster.attack,
            curMonster.isBroken ? 0.6 : 1.0,
            curMonster.element,
            playerDefense,
            skillElem,
            false,
            0.05
        );

        playerCurHp = Math.max(0, playerCurHp - monsterHit.finalDamage);
        log.append("\n【反噬】").append(curMonster.name).append(" 狂暴反扑，对道主造成 ")
           .append(monsterHit.finalDamage).append(" 点伤害！");

        if (playerCurHp <= 0) {
            log.append("\n【身陨道消】肉身气血耗尽，残魂遁回终南山重塑！");
            playerCurHp = playerMaxHp;
        }

        updateVisualCombat();
        feedLog(log.toString());
    }

    // ==========================================
    // 界面 4: 战斗自动胜利结算与经验连升弹窗！
    // ==========================================
    private void showBattleVictoryOverlay() {
        controlPanel.removeAllViews();

        TextView tvVictory = new TextView(this);
        tvVictory.setText("★【 大 荒 战 捷 · 斩 妖 证 道 】★");
        tvVictory.setTextSize(17);
        tvVictory.setTextColor(Color.parseColor("#F1C40F"));
        tvVictory.setGravity(Gravity.CENTER);
        tvVictory.setPadding(0, 10, 0, 10);
        controlPanel.addView(tvVictory);

        TextView tvKillInfo = new TextView(this);
        tvKillInfo.setText("斩杀强敌：" + curMonster.name + " (" + curMonster.tier.title + ")\n获取鸿蒙修为：+" + curMonster.expReward + " 经验值！");
        tvKillInfo.setTextColor(Color.parseColor("#2ECC71"));
        tvKillInfo.setTextSize(14);
        controlPanel.addView(tvKillInfo);

        // 增加修为经验并触发小境界自动升级
        String oldRealmTitle = realm.getFullRealmTitle();
        String upgradeMessage = realm.addCultivationExp(curMonster.expReward);
        applyRealmStats(); // 自动提升攻防血量

        TextView tvUpgradeResult = new TextView(this);
        if (!upgradeMessage.isEmpty()) {
            tvUpgradeResult.setText(upgradeMessage + "\n【体魄蜕变】气血上限提升至: " + playerMaxHp + "，攻伐提升至: " + playerAttack);
            tvUpgradeResult.setTextColor(Color.parseColor("#E74C3C"));
        } else {
            tvUpgradeResult.setText("修真境界：" + realm.getFullRealmTitle() + "\n下阶修为进度：" + realm.getCurrentExp() + " / " + realm.getExpNeededForNextStage());
            tvUpgradeResult.setTextColor(Color.parseColor("#3498DB"));
        }
        tvUpgradeResult.setTextSize(13);
        tvUpgradeResult.setPadding(0, 10, 0, 15);
        controlPanel.addView(tvUpgradeResult);

        // 操作按钮：继续探索或大境界渡劫
        if (realm.canTriggerTribulation()) {
            Button btnTrib = new Button(this);
            btnTrib.setText("⚡【立即引动九霄天劫 · 破阶渡劫】⚡");
            btnTrib.setBackgroundColor(Color.parseColor("#8E44AD"));
            btnTrib.setTextColor(Color.parseColor("#F1C40F"));
            btnTrib.setTextSize(13);
            btnTrib.setOnClickListener(v -> triggerMajorTribulation());
            controlPanel.addView(btnTrib);
        }

        Button btnContinue = new Button(this);
        btnContinue.setText("【 收取战利品 · 返回大荒神域 】");
        btnContinue.setBackgroundColor(Color.parseColor("#27AE60"));
        btnContinue.setTextColor(Color.WHITE);
        btnContinue.setTextSize(14);
        btnContinue.setPadding(0, 14, 0, 14);
        btnContinue.setOnClickListener(v -> showMapExplorationUI());
        controlPanel.addView(btnContinue);
    }

    private void triggerMajorTribulation() {
        if (realm.advanceMajorRealm()) {
            applyRealmStats();
            Toast.makeText(this, "雷劫轰顶！以雷淬体成功！晋升无上大境界至：" + realm.getFullRealmTitle() + "！气血翻倍！", Toast.LENGTH_LONG).show();
            showMapExplorationUI();
        }
    }

    private void updateVisualCombat() {
        visualView.updateCombatState(
            (float)playerCurHp / playerMaxHp,
            (float)curMonster.curHp / curMonster.maxHp,
            curMonster.maxTenacity > 0 ? (float)curMonster.curTenacity / curMonster.maxTenacity : 0f,
            curMonster.isBroken
        );
    }

    private void feedLog(String msg) {
        if (tvCombatFeed != null) {
            String old = tvCombatFeed.getText().toString();
            tvCombatFeed.setText(msg + "\n---\n" + old);
        }
    }
}
