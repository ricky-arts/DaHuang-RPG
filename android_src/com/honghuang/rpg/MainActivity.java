package com.honghuang.rpg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.view.Gravity;
import java.util.Map;

public class MainActivity extends Activity implements ChessBoardView.OnTileClickListener {
    private LinearLayout rootContainer;
    private ChessBoardView boardView;
    private LinearLayout controlPanel;

    private ChessEngine engine;

    // 选中状态
    private int selectedBenchIdx = -1;
    private int selectedBoardR = -1;
    private int selectedBoardC = -1;
    private int selectedEquipIdx = -1;

    // 界面控件
    private TextView tvPlayerInfo;
    private TextView tvSynergies;
    private LinearLayout shopRowLayout;
    private LinearLayout equipRowLayout;
    private TextView tvDetailFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        engine = new ChessEngine();

        // 横屏左右分栏：左侧 65% 战棋棋盘，右侧 35% 金铲铲运营操作台
        rootContainer = new LinearLayout(this);
        rootContainer.setOrientation(LinearLayout.HORIZONTAL);
        rootContainer.setBackgroundColor(Color.parseColor("#080D14"));

        boardView = new ChessBoardView(this);
        boardView.setEngine(engine);
        boardView.setOnTileClickListener(this);

        LinearLayout.LayoutParams leftLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 6.2f
        );
        rootContainer.addView(boardView, leftLp);

        controlPanel = new LinearLayout(this);
        controlPanel.setOrientation(LinearLayout.VERTICAL);
        controlPanel.setBackgroundColor(Color.parseColor("#0F1722"));
        controlPanel.setPadding(16, 12, 16, 12);

        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 3.8f
        );
        rootContainer.addView(controlPanel, rightLp);

        setContentView(rootContainer);
        setupControlPanel();
        refreshAllViews();
    }

    private void setupControlPanel() {
        controlPanel.removeAllViews();

        // 1. 顶部玩家基础状态 (回合 / 灵石 / 人口 / 血量)
        tvPlayerInfo = new TextView(this);
        tvPlayerInfo.setTextSize(13);
        tvPlayerInfo.setTextColor(Color.parseColor("#F1C40F"));
        controlPanel.addView(tvPlayerInfo);

        // 2. 羁绊激活信息栏 (Synergies)
        tvSynergies = new TextView(this);
        tvSynergies.setTextSize(11);
        tvSynergies.setTextColor(Color.parseColor("#2ECC71"));
        tvSynergies.setPadding(0, 4, 0, 4);
        controlPanel.addView(tvSynergies);

        // 3. 运营操作按钮组 (升人口 D牌 开战)
        LinearLayout opLayout = new LinearLayout(this);
        opLayout.setOrientation(LinearLayout.HORIZONTAL);
        opLayout.setGravity(Gravity.CENTER);

        Button btnBuyExp = new Button(this);
        btnBuyExp.setText("【研习道法 4灵石】");
        btnBuyExp.setTextSize(10);
        btnBuyExp.setOnClickListener(v -> {
            if (engine.buyExp()) {
                feedLog("【升级人口】研习道法获得 4 点经验！当前人口等级：" + engine.level);
                refreshAllViews();
            } else {
                Toast.makeText(this, "灵石不足 4 块或已达九阶人口！", Toast.LENGTH_SHORT).show();
            }
        });
        opLayout.addView(btnBuyExp);

        Button btnReroll = new Button(this);
        btnReroll.setText("【引动天机 2灵石】");
        btnReroll.setTextSize(10);
        btnReroll.setOnClickListener(v -> {
            if (engine.refreshShop(false)) {
                feedLog("【天机刷新】消耗 2 灵石洗练天机卡池！");
                refreshAllViews();
            } else {
                Toast.makeText(this, "灵石不足 2 块无法刷新！", Toast.LENGTH_SHORT).show();
            }
        });
        opLayout.addView(btnReroll);

        Button btnFight = new Button(this);
        btnFight.setText("⚔️【开战】");
        btnFight.setTextSize(11);
        btnFight.setBackgroundColor(Color.parseColor("#C0392B"));
        btnFight.setTextColor(Color.WHITE);
        btnFight.setOnClickListener(v -> executeCombatRound());
        opLayout.addView(btnFight);

        controlPanel.addView(opLayout);

        // 4. 卡池商店横向卡牌区 (5 张牌)
        TextView tvShopTitle = new TextView(this);
        tvShopTitle.setText("【 天机道坊 · 仙魔招募 (点击购买) 】");
        tvShopTitle.setTextColor(Color.parseColor("#E67E22"));
        tvShopTitle.setTextSize(11);
        tvShopTitle.setPadding(0, 4, 0, 2);
        controlPanel.addView(tvShopTitle);

        shopRowLayout = new LinearLayout(this);
        shopRowLayout.setOrientation(LinearLayout.VERTICAL);
        controlPanel.addView(shopRowLayout);

        // 5. 装备法宝栏 (可穿戴至选中棋子)
        TextView tvEquipTitle = new TextView(this);
        tvEquipTitle.setText("【 法宝玉匣 (点击装备赋能给选中棋子) 】");
        tvEquipTitle.setTextColor(Color.parseColor("#9B59B6"));
        tvEquipTitle.setTextSize(11);
        tvEquipTitle.setPadding(0, 4, 0, 2);
        controlPanel.addView(tvEquipTitle);

        equipRowLayout = new LinearLayout(this);
        equipRowLayout.setOrientation(LinearLayout.HORIZONTAL);
        controlPanel.addView(equipRowLayout);

        // 6. 详细战报与人物生平
        ScrollView sv = new ScrollView(this);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f
        );
        sv.setLayoutParams(svLp);

        tvDetailFeed = new TextView(this);
        tvDetailFeed.setTextColor(Color.parseColor("#BDC3C7"));
        tvDetailFeed.setTextSize(10);
        tvDetailFeed.setText("欢迎踏入《大荒：洪荒弈道》！\n三张同名棋子自动升星（二星/三星）！两件基础散件装备到同一棋子自动融合成神品至宝！");
        sv.addView(tvDetailFeed);
        controlPanel.addView(sv);
    }

    private void refreshAllViews() {
        // 1. 刷新玩家顶部状态
        int boardCount = engine.getBoardCount();
        tvPlayerInfo.setText("第 " + engine.round + " 回合 ｜ 道尊气血:" + engine.playerHealth + " ｜ 灵石:" + engine.gold + " ｜ 人口:" + boardCount + "/" + engine.level + " (升级进度:" + engine.exp + "/" + engine.getExpForNextLevel() + ")");

        // 2. 刷新羁绊信息
        Map<String, Integer> syn = engine.calculateSynergies();
        StringBuilder synSb = new StringBuilder("【活跃羁绊】：");
        if (syn.isEmpty()) {
            synSb.append("暂无");
        } else {
            for (Map.Entry<String, Integer> e : syn.entrySet()) {
                synSb.append(e.getKey()).append(" x").append(e.getValue()).append("  ");
            }
        }
        tvSynergies.setText(synSb.toString());

        // 3. 刷新商店 5 张卡
        shopRowLayout.removeAllViews();
        for (int i = 0; i < engine.shopCards.size(); i++) {
            final int cardIdx = i;
            ChessChampion card = engine.shopCards.get(i);
            Button btnCard = new Button(this);
            if (card != null) {
                btnCard.setText(card.cost + "灵石 [" + card.origin + "·" + card.roleClass + "] " + card.name);
                btnCard.setTextSize(10);
                btnCard.setBackgroundColor(getCostColor(card.cost));
                btnCard.setTextColor(Color.WHITE);
                btnCard.setOnClickListener(v -> {
                    if (engine.buyChampion(cardIdx)) {
                        feedLog("【招揽道友】花费 " + card.cost + " 灵石请出【" + card.name + "】入座备战席！");
                        refreshAllViews();
                    } else {
                        Toast.makeText(this, "备战席已满或灵石不足！", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnCard.setText("已售罄");
                btnCard.setTextSize(10);
                btnCard.setEnabled(false);
                btnCard.setBackgroundColor(Color.parseColor("#2C3E50"));
            }
            shopRowLayout.addView(btnCard);
        }

        // 4. 刷新装备法宝栏
        equipRowLayout.removeAllViews();
        for (int i = 0; i < engine.equipmentInventory.size(); i++) {
            final int eqIdx = i;
            ChessEquipment eq = engine.equipmentInventory.get(i);
            Button btnEq = new Button(this);
            btnEq.setText(eq.type.name);
            btnEq.setTextSize(9);
            btnEq.setBackgroundColor(eqIdx == selectedEquipIdx ? Color.parseColor("#F1C40F") : Color.parseColor("#8E44AD"));
            btnEq.setTextColor(Color.WHITE);
            btnEq.setOnClickListener(v -> {
                selectedEquipIdx = (selectedEquipIdx == eqIdx) ? -1 : eqIdx;
                feedLog("【选中法宝】" + eq.type.name + " - " + eq.type.description + "\n(点击棋盘上的棋子即可穿戴或融合至尊法宝！)");
                refreshAllViews();
            });
            equipRowLayout.addView(btnEq);
        }

        boardView.selectedBenchIdx = this.selectedBenchIdx;
        boardView.selectedBoardR = this.selectedBoardR;
        boardView.selectedBoardC = this.selectedBoardC;
        boardView.invalidate();
    }

    private int getCostColor(int cost) {
        switch (cost) {
            case 1: return Color.parseColor("#576574");
            case 2: return Color.parseColor("#27AE60");
            case 3: return Color.parseColor("#2980B9");
            case 4: return Color.parseColor("#8E44AD");
            case 5: return Color.parseColor("#D35400");
            default: return Color.parseColor("#2C3E50");
        }
    }

    /**
     * 点击备战席棋子
     */
    @Override
    public void onBenchClicked(int idx) {
        if (selectedEquipIdx >= 0 && engine.bench[idx] != null) {
            // 穿戴装备
            if (engine.equipItemToChampion(selectedEquipIdx, engine.bench[idx])) {
                feedLog("【祭炼穿戴】成功将法宝赐予【" + engine.bench[idx].name + "】！");
                selectedEquipIdx = -1;
                refreshAllViews();
                return;
            }
        }

        if (selectedBenchIdx == idx) {
            selectedBenchIdx = -1; // 取消选中
        } else {
            selectedBenchIdx = idx;
            selectedBoardR = -1;
            selectedBoardC = -1;
            ChessChampion c = engine.bench[idx];
            if (c != null) {
                showChampionLore(c);
            }
        }
        refreshAllViews();
    }

    /**
     * 点击战棋盘格子 (布阵或穿戴装备)
     */
    @Override
    public void onBoardTileClicked(int r, int c) {
        if (r < 2) {
            // 点击敌方格子，查看敌方属性
            if (engine.enemyBoard[r][c] != null) {
                showChampionLore(engine.enemyBoard[r][c]);
            }
            return;
        }

        // 我方半场格子
        if (selectedEquipIdx >= 0 && engine.board[r][c] != null) {
            if (engine.equipItemToChampion(selectedEquipIdx, engine.board[r][c])) {
                feedLog("【祭炼穿戴】成功将法宝赐予【" + engine.board[r][c].name + "】！");
                selectedEquipIdx = -1;
                refreshAllViews();
                return;
            }
        }

        if (selectedBenchIdx >= 0) {
            // 从备战席上阵到该格子
            if (engine.deployChampion(selectedBenchIdx, r, c)) {
                feedLog("【登坛布阵】棋子入阵战棋格 (" + r + "," + c + ")！");
                selectedBenchIdx = -1;
            } else {
                Toast.makeText(this, "上阵人口已达当前等级上限！请先研习道法提升人口！", Toast.LENGTH_SHORT).show();
            }
        } else if (selectedBoardR >= 0 && selectedBoardC >= 0) {
            // 棋盘内换位
            ChessChampion temp = engine.board[r][c];
            engine.board[r][c] = engine.board[selectedBoardR][selectedBoardC];
            engine.board[selectedBoardR][selectedBoardC] = temp;
            selectedBoardR = -1;
            selectedBoardC = -1;
        } else {
            if (engine.board[r][c] != null) {
                selectedBoardR = r;
                selectedBoardC = c;
                showChampionLore(engine.board[r][c]);
            }
        }
        refreshAllViews();
    }

    private void showChampionLore(ChessChampion c) {
        StringBuilder sb = new StringBuilder();
        sb.append("★【").append(c.name).append("】").append(c.star).append("星 (").append(c.origin).append("·").append(c.roleClass).append(")\n")
          .append("属性：气血 ").append(c.getMaxHp()).append(" ｜ 攻伐 ").append(c.getAttack()).append(" ｜ 护体 ").append(c.getDefense()).append("\n")
          .append("大招神通：").append(c.skillName).append("\n效果：").append(c.skillDesc).append("\n")
          .append("背景生平：").append(c.background);
        if (!c.equipments.isEmpty()) {
            sb.append("\n已佩戴法宝：");
            for (ChessEquipment eq : c.equipments) {
                sb.append("[").append(eq.type.name).append("] ");
            }
        }
        feedLog(sb.toString());
    }

    /**
     * 执行自走棋自动对战回合模拟
     */
    private void executeCombatRound() {
        if (engine.getBoardCount() == 0) {
            Toast.makeText(this, "棋盘上尚未布置任何棋子！请先从备战席上阵棋子！", Toast.LENGTH_SHORT).show();
            return;
        }

        boardView.triggerCombatTurn();

        // 计算双方战斗力与羁绊
        int myPower = 0;
        for (int r = 2; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                if (engine.board[r][c] != null) {
                    myPower += engine.board[r][c].getAttack() + engine.board[r][c].getMaxHp() / 10;
                }
            }
        }

        int enemyPower = 0;
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 8; c++) {
                if (engine.enemyBoard[r][c] != null) {
                    enemyPower += engine.enemyBoard[r][c].getAttack() + engine.enemyBoard[r][c].getMaxHp() / 10;
                }
            }
        }

        // 胜负判定与血量扣减
        boolean victory = myPower >= (enemyPower * 0.85);
        if (victory) {
            feedLog("【★ 回合大捷 ★】我方仙魔各显神通，万剑与雷霆倾泻，大获全胜！获得回合胜利灵石加成！");
        } else {
            int damage = Math.max(5, (enemyPower - myPower) / 50);
            engine.playerHealth = Math.max(0, engine.playerHealth - damage);
            feedLog("【回合失利】敌方大阵煞气滔天，我方道尊受到 " + damage + " 点神魂真伤！剩余血量：" + engine.playerHealth);
        }

        if (engine.playerHealth <= 0) {
            Toast.makeText(this, "道尊气血竭尽，重聚洪荒灵池！", Toast.LENGTH_LONG).show();
            engine.playerHealth = 100;
        }

        // 回合结算，发牌发钱
        engine.startNewRound();
        refreshAllViews();
    }

    private void feedLog(String msg) {
        if (tvDetailFeed != null) {
            String old = tvDetailFeed.getText().toString();
            tvDetailFeed.setText(msg + "\n---------------------\n" + old);
        }
    }
}
