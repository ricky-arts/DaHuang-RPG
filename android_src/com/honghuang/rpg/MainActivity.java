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
    private CultivationRealm realm = new CultivationRealm(0, 1);
    private CharacterProfile selectedHero;
    private int playerCurHp = 1000;
    private int playerMaxHp = 1000;
    private int playerAttack = 180;
    private int playerDefense = 90;
    private double playerCritRate = 0.15;
    private double playerDodgeRate = 0.10;

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
    // 界面 1: 开局角色道骨形象自选 (差异化专属)
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
        tvHeroStats.setText("属性：" + hero.element + "系 ｜ 本命神兵：" + hero.weapon +
                "\n初始属性：气血 " + hero.baseHp + " ｜ 攻伐 " + hero.baseAtk + " ｜ 护体 " + hero.baseDef +
                "\n专属倾向：暴击率 " + (int)(hero.baseCritChance * 100) + "% ｜ 闪避率 " + (int)(hero.baseDodgeChance * 100) + "%");
        tvHeroStats.setTextColor(Color.parseColor("#A4B0BE"));
        tvHeroStats.setTextSize(12);
        controlPanel.addView(tvHeroStats);

        ScrollView sv = new ScrollView(this);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
        );
        sv.setLayoutParams(svLp);

        LinearLayout loreLayout = new LinearLayout(this);
        loreLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tvPassive = new TextView(this);
        tvPassive.setText("★ 专属天道真元被动：\n" + hero.passiveSkill);
        tvPassive.setTextColor(Color.parseColor("#E67E22"));
        tvPassive.setTextSize(12);
        tvPassive.setPadding(0, 4, 0, 4);
        loreLayout.addView(tvPassive);

        TextView tvHeroLore = new TextView(this);
        tvHeroLore.setText("命格宿命：" + hero.lore + "\n\n★ 专属三大修真神通：\n1. " + hero.skillNames[0] + ": " + hero.skillDescs[0] +
                "\n2. " + hero.skillNames[1] + ": " + hero.skillDescs[1] +
                "\n3. " + hero.skillNames[2] + ": " + hero.skillDescs[2]);
        tvHeroLore.setTextColor(Color.parseColor("#CED6E0"));
        tvHeroLore.setTextSize(11);
        tvHeroLore.setPadding(0, 4, 0, 8);
        loreLayout.addView(tvHeroLore);

        sv.addView(loreLayout);
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
        btnConfirm.setText("【 锁定此身道骨 · 踏入大荒大世界 】");
        btnConfirm.setBackgroundColor(Color.parseColor("#B71540"));
        btnConfirm.setTextColor(Color.WHITE);
        btnConfirm.setTextSize(14);
        btnConfirm.setPadding(0, 14, 0, 14);
        btnConfirm.setOnClickListener(v -> {
            selectedHero = profiles[curCharIdx];
            visualView.setActiveHeroTheme(selectedHero.visualTheme);
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
        playerCritRate = selectedHero.baseCritChance;
        playerDodgeRate = selectedHero.baseDodgeChance;
    }

    // ==========================================
    // 界面 2: 四海八荒大地图与动态随机Roguelike奇遇
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
        tvLocName.setPadding(0, 4, 0, 2);
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

        // 随机奇遇探索按钮 (Roguelike 机缘)
        Button btnRandomAdventure = new Button(this);
        btnRandomAdventure.setText("🎲【神域探幽 · 触发未知大荒随机机缘】🎲");
        btnRandomAdventure.setBackgroundColor(Color.parseColor("#E67E22"));
        btnRandomAdventure.setTextColor(Color.WHITE);
        btnRandomAdventure.setTextSize(12);
        btnRandomAdventure.setOnClickListener(v -> triggerMapRandomAdventure(loc.name));
        controlPanel.addView(btnRandomAdventure);

        // 怪物梯度选择列表
        TextView tvMonsterHeader = new TextView(this);
        tvMonsterHeader.setText("【 区域异兽凶险梯度 (点击挑选对手) 】");
        tvMonsterHeader.setTextColor(Color.parseColor("#E74C3C"));
        tvMonsterHeader.setTextSize(12);
        tvMonsterHeader.setPadding(0, 6, 0, 2);
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
        mapNav.setPadding(0, 4, 0, 4);

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

    /**
     * 触发大荒 Roguelike 随机奇遇事件
     */
    private void triggerMapRandomAdventure(String mapName) {
        AdventureEvent ev = AdventureEvent.triggerRandomEvent(mapName);
        controlPanel.removeAllViews();

        TextView tvTitle = new TextView(this);
        tvTitle.setText(ev.title);
        tvTitle.setTextSize(17);
        tvTitle.setTextColor(Color.parseColor(ev.type.colorHex));
        controlPanel.addView(tvTitle);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(ev.description);
        tvDesc.setTextSize(13);
        tvDesc.setTextColor(Color.parseColor("#E0E0E0"));
        tvDesc.setPadding(0, 10, 0, 15);
        controlPanel.addView(tvDesc);

        // 选项 A
        Button btnA = new Button(this);
        btnA.setText(ev.optionA);
        btnA.setBackgroundColor(Color.parseColor("#2C3E50"));
        btnA.setTextColor(Color.WHITE);
        btnA.setTextSize(12);
        btnA.setOnClickListener(v -> handleAdventureResult(ev.resultTextA, ev.expDeltaA, ev.hpDeltaA));
        controlPanel.addView(btnA);

        // 选项 B
        Button btnB = new Button(this);
        btnB.setText(ev.optionB);
        btnB.setBackgroundColor(Color.parseColor("#34495E"));
        btnB.setTextColor(Color.WHITE);
        btnB.setTextSize(12);
        btnB.setOnClickListener(v -> handleAdventureResult(ev.resultTextB, ev.expDeltaB, ev.hpDeltaB));
        controlPanel.addView(btnB);
    }

    private void handleAdventureResult(String resultText, int expDelta, int hpDelta) {
        controlPanel.removeAllViews();

        TextView tvResult = new TextView(this);
        tvResult.setText("【 奇遇机缘造化结语 】");
        tvResult.setTextSize(16);
        tvResult.setTextColor(Color.parseColor("#F1C40F"));
        controlPanel.addView(tvResult);

        TextView tvContent = new TextView(this);
        tvContent.setText(resultText);
        tvContent.setTextSize(13);
        tvContent.setTextColor(Color.parseColor("#E0E0E0"));
        tvContent.setPadding(0, 10, 0, 15);
        controlPanel.addView(tvContent);

        // 结算数值变动
        playerCurHp = Math.max(1, Math.min(playerMaxHp, playerCurHp + hpDelta));
        if (expDelta > 0) {
            String up = realm.addCultivationExp(expDelta);
            applyRealmStats();
            if (!up.isEmpty()) {
                TextView tvUp = new TextView(this);
                tvUp.setText(up);
                tvUp.setTextColor(Color.parseColor("#2ECC71"));
                tvUp.setTextSize(13);
                controlPanel.addView(tvUp);
            }
        }

        Button btnBack = new Button(this);
        btnBack.setText("【 获益启程 · 返回大荒神域 】");
        btnBack.setBackgroundColor(Color.parseColor("#27AE60"));
        btnBack.setTextColor(Color.WHITE);
        btnBack.setOnClickListener(v -> showMapExplorationUI());
        controlPanel.addView(btnBack);
    }

    // ==========================================
    // 界面 3: 具备真随机概率波动的战斗系统
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

        // 差异化专属技能按键
        LinearLayout skillRow1 = new LinearLayout(this);
        skillRow1.setOrientation(LinearLayout.HORIZONTAL);
        skillRow1.setGravity(Gravity.CENTER);

        Button btnAttack = new Button(this);
        btnAttack.setText(selectedHero.skillNames[0]);
        btnAttack.setTextSize(10);
        btnAttack.setOnClickListener(v -> executeCombatRound("NORMAL"));
        skillRow1.addView(btnAttack);

        Button btnSkillBreak = new Button(this);
        btnSkillBreak.setText(selectedHero.skillNames[1]);
        btnSkillBreak.setTextSize(10);
        btnSkillBreak.setOnClickListener(v -> executeCombatRound("SKILL_BREAK"));
        skillRow1.addView(btnSkillBreak);
        controlPanel.addView(skillRow1);

        LinearLayout skillRow2 = new LinearLayout(this);
        skillRow2.setOrientation(LinearLayout.HORIZONTAL);
        skillRow2.setGravity(Gravity.CENTER);

        Button btnUltimate = new Button(this);
        btnUltimate.setText(selectedHero.skillNames[2]);
        btnUltimate.setTextSize(10);
        btnUltimate.setBackgroundColor(Color.parseColor("#8E44AD"));
        btnUltimate.setTextColor(Color.WHITE);
        btnUltimate.setOnClickListener(v -> executeCombatRound("ULTIMATE"));
        skillRow2.addView(btnUltimate);

        Button btnFlee = new Button(this);
        btnFlee.setText("【御风撤退】");
        btnFlee.setTextSize(10);
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
        tvCombatFeed.setText("战阵已启！角色初始暴击 " + (int)(playerCritRate*100) + "%，闪避率 " + (int)(playerDodgeRate*100) + "%！伤害带 ±15% 真随机浮动！");
        sv.addView(tvCombatFeed);
        controlPanel.addView(sv);
    }

    private void executeCombatRound(String skillType) {
        if (curMonster.curHp <= 0) return;

        double skillRate = 1.0;
        String skillElem = selectedHero.element;
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

        // 角色专属暴伤加成判定 (太微 200% 暴伤)
        double critMult = "char_sword".equals(selectedHero.id) ? 2.0 : 1.65;

        // 1. 真实属性与真随机浮动伤害计算
        BattleCalculator.HitResult hit = BattleCalculator.calculateDamageWithRng(
            playerAttack,
            skillRate,
            skillElem,
            curMonster.defense,
            curMonster.element,
            curMonster.isBroken,
            playerCritRate,
            critMult,
            0.0 // 怪物闪避
        );

        visualView.triggerCombatSkillAnimation(animAction, "-" + hit.finalDamage + (hit.isCrit ? " 暴击!" : ""));

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
        log.append("【").append(selectedHero.name).append(" 出手】造成 ")
           .append(hit.finalDamage).append(" 点伤害！").append(hit.elemDescription)
           .append(hit.isCrit ? "★【暴击】" : "")
           .append(hit.statusEffect.isEmpty() ? "" : (" 附加" + hit.statusEffect));

        if (curMonster.isBroken) {
            log.append("\n【破韧！】首领韧性护盾瓦解，陷入 BREAK 虚弱状态！");
        }

        // 怪物死灭自动结算
        if (curMonster.curHp <= 0) {
            updateVisualCombat();
            feedLog(log.toString());
            showBattleVictoryOverlay();
            return;
        }

        // 2. 怪物对角色反攻 (带角色真实闪避率判定与断岳反伤被动)
        BattleCalculator.HitResult monsterHit = BattleCalculator.calculateDamageWithRng(
            curMonster.attack,
            curMonster.isBroken ? 0.6 : 1.0,
            curMonster.element,
            playerDefense,
            skillElem,
            false,
            0.05,
            1.5,
            playerDodgeRate // 传入角色专属闪避率！
        );

        if (monsterHit.isDodged) {
            log.append("\n【道骨无双】").append(selectedHero.name).append(" 展开身法残影，完美闪避了首领的反扑攻击！(MISSED)");
        } else {
            // 望舒被动：九尾天狐障概率免疫
            if ("char_fox".equals(selectedHero.id) && Math.random() < 0.25) {
                log.append("\n【天狐替身】望舒触发【九尾天狐障】，幻象代受，免疫了此次伤害！");
            } else {
                playerCurHp = Math.max(0, playerCurHp - monsterHit.finalDamage);
                log.append("\n【首领反噬】受到 ").append(monsterHit.finalDamage).append(" 点伤害！");

                // 断岳被动：反震 20% 伤害
                if ("char_titan".equals(selectedHero.id)) {
                    int reflect = (int)(monsterHit.finalDamage * 0.2);
                    curMonster.curHp = Math.max(0, curMonster.curHp - reflect);
                    log.append("【巫祖反震】断岳震荡煞体，反伤首领 ").append(reflect).append(" 点气血！");
                }
            }
        }

        if (playerCurHp <= 0) {
            log.append("\n【身陨道消】肉身气血耗尽，残魂遁回重塑！");
            playerCurHp = playerMaxHp;
        }

        updateVisualCombat();
        feedLog(log.toString());
    }

    // ==========================================
    // 界面 4: 战斗自动战捷与经验连升结算
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

        // 随机战利品词缀掉落 (概率掉落宝物)
        if (Math.random() < 0.50) {
            String[] loots = {
                "【古宝】混元太虚佩 (暴击率永久+2%)",
                "【神兵】庚金洗髓精铁 (攻伐永久+25)",
                "【灵丹】九叶还阳草 (气血上限+150)",
                "【秘箓】遁天破妄符箓 (身法闪避率+2%)"
            };
            int lootIdx = (int)(Math.random() * loots.length);
            TextView tvLoot = new TextView(this);
            tvLoot.setText("🎁【战利品偶得】" + loots[lootIdx]);
            tvLoot.setTextColor(Color.parseColor("#E67E22"));
            tvLoot.setTextSize(13);
            tvLoot.setPadding(0, 4, 0, 8);
            controlPanel.addView(tvLoot);
        }

        // 增加修为经验并触发小境界自动升级
        String upgradeMessage = realm.addCultivationExp(curMonster.expReward);
        applyRealmStats();

        TextView tvUpgradeResult = new TextView(this);
        if (!upgradeMessage.isEmpty()) {
            tvUpgradeResult.setText(upgradeMessage + "\n【体魄蜕变】气血上限提升至: " + playerMaxHp + "，攻伐提升至: " + playerAttack);
            tvUpgradeResult.setTextColor(Color.parseColor("#E74C3C"));
        } else {
            tvUpgradeResult.setText("修真境界：" + realm.getFullRealmTitle() + "\n下阶修为进度：" + realm.getCurrentExp() + " / " + realm.getExpNeededForNextStage());
            tvUpgradeResult.setTextColor(Color.parseColor("#3498DB"));
        }
        tvUpgradeResult.setTextSize(13);
        tvUpgradeResult.setPadding(0, 8, 0, 12);
        controlPanel.addView(tvUpgradeResult);

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
        btnContinue.setPadding(0, 12, 0, 12);
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
