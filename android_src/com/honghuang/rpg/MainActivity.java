package com.honghuang.rpg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.view.Gravity;
import java.util.HashMap;
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
    private int selectedItemIdx = -1;

    // 界面控件
    private TextView tvPlayerInfo;
    private TextView tvSynergies;
    private LinearLayout shopCardContainer;
    private LinearLayout itemBenchLayout;
    private LinearLayout detailCardLayout;
    private ImageView ivDetailPortrait;
    private TextView tvDetailFeed;

    // 图片缓存
    private final Map<String, Bitmap> cardBitmaps = new HashMap<>();
    private final Map<String, Bitmap> itemBitmaps = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        engine = new ChessEngine();
        loadUiBitmaps();

        // 横屏金铲铲竞技场布局：左侧 65% 战棋主棋盘与备战席，右侧 35% 运营操作控制台
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
        controlPanel.setPadding(14, 10, 14, 10);

        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 3.8f
        );
        rootContainer.addView(controlPanel, rightLp);

        setContentView(rootContainer);
        setupControlPanel();
        refreshAllViews();

        // 默认激活展示第一位代表性英雄的高清卡牌立绘！
        if (!engine.shopCards.isEmpty() && engine.shopCards.get(0) != null) {
            showChampionDetailCard(engine.shopCards.get(0));
        }
    }

    private void loadUiBitmaps() {
        String[] heroes = {"nezha", "yangjian", "tongtian", "baihe", "linghu", "wubing", "leizhenzi", "zhaogongming", "kongxuan", "houtu", "donghuang"};
        for (String h : heroes) {
            Bitmap b = AssetLoader.loadDrawableOrAsset(this, "tft_card_" + h);
            if (b != null) cardBitmaps.put(h, b);
        }

        String[] items = {"sword", "bow", "tear", "armor", "ie", "guinsoo"};
        for (String it : items) {
            Bitmap b = AssetLoader.loadDrawableOrAsset(this, "tft_item_" + it);
            if (b != null) itemBitmaps.put(it, b);
        }
    }

    private void setupControlPanel() {
        controlPanel.removeAllViews();

        // 1. 金铲铲顶部状态 (回合/气血/金币/人口)
        tvPlayerInfo = new TextView(this);
        tvPlayerInfo.setTextSize(12);
        tvPlayerInfo.setTextColor(Color.parseColor("#F1C40F"));
        controlPanel.addView(tvPlayerInfo);

        // 2. 羁绊
        tvSynergies = new TextView(this);
        tvSynergies.setTextSize(11);
        tvSynergies.setTextColor(Color.parseColor("#2ECC71"));
        tvSynergies.setPadding(0, 2, 0, 4);
        controlPanel.addView(tvSynergies);

        // 3. 运营操作按钮组 (升人口 4金 / 刷新 2金 / 开战)
        LinearLayout opLayout = new LinearLayout(this);
        opLayout.setOrientation(LinearLayout.HORIZONTAL);
        opLayout.setGravity(Gravity.CENTER);

        Button btnBuyExp = new Button(this);
        btnBuyExp.setText("【研习道法 4金】");
        btnBuyExp.setTextSize(10);
        btnBuyExp.setOnClickListener(v -> {
            if (engine.buyExp()) {
                feedLog("【升人口】花费 4 金币购买 4 点经验！当前等级：" + engine.level + "级");
                refreshAllViews();
            } else {
                Toast.makeText(this, "金币不足 4 块或已达满级人口！", Toast.LENGTH_SHORT).show();
            }
        });
        opLayout.addView(btnBuyExp);

        Button btnReroll = new Button(this);
        btnReroll.setText("【刷新 2金】");
        btnReroll.setTextSize(10);
        btnReroll.setOnClickListener(v -> {
            if (engine.refreshShop(false)) {
                feedLog("【D牌刷新】消耗 2 金币洗练卡池！");
                refreshAllViews();
            } else {
                Toast.makeText(this, "金币不足 2 块无法刷新！", Toast.LENGTH_SHORT).show();
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

        // 4. 金铲铲标准卡牌商店 (水平横向排列的卡牌槽位)
        TextView tvShopTitle = new TextView(this);
        tvShopTitle.setText("【 天机卡池 · 仙魔招募 (点击购入) 】");
        tvShopTitle.setTextColor(Color.parseColor("#E67E22"));
        tvShopTitle.setTextSize(11);
        tvShopTitle.setPadding(0, 4, 0, 2);
        controlPanel.addView(tvShopTitle);

        HorizontalScrollView hsvShop = new HorizontalScrollView(this);
        shopCardContainer = new LinearLayout(this);
        shopCardContainer.setOrientation(LinearLayout.HORIZONTAL);
        hsvShop.addView(shopCardContainer);
        controlPanel.addView(hsvShop);

        // 5. 金铲铲装备库 (真实图标)
        TextView tvItemTitle = new TextView(this);
        tvItemTitle.setText("【 装备库 (点击选中后赋能英雄，双散件自动合成) 】");
        tvItemTitle.setTextColor(Color.parseColor("#9B59B6"));
        tvItemTitle.setTextSize(11);
        tvItemTitle.setPadding(0, 4, 0, 2);
        controlPanel.addView(tvItemTitle);

        HorizontalScrollView hsvItem = new HorizontalScrollView(this);
        itemBenchLayout = new LinearLayout(this);
        itemBenchLayout.setOrientation(LinearLayout.HORIZONTAL);
        hsvItem.addView(itemBenchLayout);
        controlPanel.addView(hsvItem);

        // 6. 英雄全幅高精原画立绘展示卡 (金铲铲卡牌视窗)
        detailCardLayout = new LinearLayout(this);
        detailCardLayout.setOrientation(LinearLayout.HORIZONTAL);
        detailCardLayout.setPadding(0, 4, 0, 4);
        detailCardLayout.setGravity(Gravity.CENTER_VERTICAL);

        ivDetailPortrait = new ImageView(this);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(110, 140);
        ivLp.rightMargin = 10;
        ivDetailPortrait.setLayoutParams(ivLp);
        ivDetailPortrait.setScaleType(ImageView.ScaleType.FIT_CENTER);
        detailCardLayout.addView(ivDetailPortrait);

        ScrollView sv = new ScrollView(this);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 140
        );
        sv.setLayoutParams(svLp);

        tvDetailFeed = new TextView(this);
        tvDetailFeed.setTextColor(Color.parseColor("#CED6E0"));
        tvDetailFeed.setTextSize(11);
        tvDetailFeed.setText("欢迎踏入《大荒：洪荒弈道》！\n三张同名棋子自动升星！两件基础散件装备到同一棋子身上自动熔铸成金铲铲神级大件！");
        sv.addView(tvDetailFeed);
        detailCardLayout.addView(sv);

        controlPanel.addView(detailCardLayout);
    }

    private void refreshAllViews() {
        int boardCount = engine.getBoardCount();
        tvPlayerInfo.setText("第 " + engine.round + " 回合 ｜ 小小道尊气血:" + engine.playerHealth + " ｜ 金币:" + engine.gold + " ｜ 人口:" + boardCount + "/" + engine.level + " (经验:" + engine.exp + "/" + engine.getExpForNextLevel() + ")");

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

        // 刷新金铲铲图文卡牌商店
        shopCardContainer.removeAllViews();
        for (int i = 0; i < engine.shopCards.size(); i++) {
            final int cardIdx = i;
            ChessChampion card = engine.shopCards.get(i);

            LinearLayout cardBox = new LinearLayout(this);
            cardBox.setOrientation(LinearLayout.VERTICAL);
            cardBox.setGravity(Gravity.CENTER);
            cardBox.setPadding(6, 4, 6, 4);

            LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(95, 115);
            boxLp.rightMargin = 6;
            cardBox.setLayoutParams(boxLp);

            if (card != null) {
                cardBox.setBackgroundColor(getCostColor(card.cost));

                ImageView ivCardThumb = new ImageView(this);
                LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(65, 75);
                ivCardThumb.setLayoutParams(ivLp);
                ivCardThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);

                String key = card.id.replace("c_", "");
                Bitmap thumb = cardBitmaps.get(key);
                if (thumb != null) {
                    ivCardThumb.setImageBitmap(thumb);
                }

                TextView tvName = new TextView(this);
                tvName.setText(card.name + " (" + card.cost + "金)");
                tvName.setTextColor(Color.WHITE);
                tvName.setTextSize(9);
                tvName.setGravity(Gravity.CENTER);

                cardBox.addView(ivCardThumb);
                cardBox.addView(tvName);

                cardBox.setOnClickListener(v -> {
                    if (engine.buyChampion(cardIdx)) {
                        feedLog("【招募英雄】花费 " + card.cost + " 金币招募【" + card.name + "】！");
                        showChampionDetailCard(card);
                        refreshAllViews();
                    } else {
                        Toast.makeText(this, "备战席已满或金币不足！", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                cardBox.setBackgroundColor(Color.parseColor("#1C2430"));
                TextView tvSold = new TextView(this);
                tvSold.setText("已售罄");
                tvSold.setTextColor(Color.GRAY);
                tvSold.setTextSize(10);
                cardBox.addView(tvSold);
            }
            shopCardContainer.addView(cardBox);
        }

        // 刷新金铲铲装备库 (图标)
        itemBenchLayout.removeAllViews();
        for (int i = 0; i < engine.itemBench.size(); i++) {
            final int itIdx = i;
            TFTItem item = engine.itemBench.get(i);

            LinearLayout itemCol = new LinearLayout(this);
            itemCol.setOrientation(LinearLayout.VERTICAL);
            itemCol.setGravity(Gravity.CENTER);
            itemCol.setPadding(6, 2, 6, 2);

            ImageView ivItem = new ImageView(this);
            LinearLayout.LayoutParams ivItemLp = new LinearLayout.LayoutParams(42, 42);
            ivItem.setLayoutParams(ivItemLp);
            ivItem.setScaleType(ImageView.ScaleType.FIT_CENTER);

            String itemKey = mapItemToKey(item.id);
            Bitmap bmp = itemBitmaps.get(itemKey);
            if (bmp != null) {
                ivItem.setImageBitmap(bmp);
            } else {
                ivItem.setBackgroundColor(item.id.isCompleted ? Color.parseColor("#D35400") : Color.parseColor("#2980B9"));
            }

            Button btnItem = new Button(this);
            btnItem.setText(item.id.name.substring(0, Math.min(3, item.id.name.length())));
            btnItem.setTextSize(8);
            btnItem.setBackgroundColor(itIdx == selectedItemIdx ? Color.parseColor("#F1C40F") : Color.parseColor("#2C3E50"));
            btnItem.setTextColor(Color.WHITE);
            btnItem.setOnClickListener(v -> {
                selectedItemIdx = (selectedItemIdx == itIdx) ? -1 : itIdx;
                feedLog("【选中装备】" + item.id.name + "\n效果：" + item.id.description + "\n(点击棋盘上的英雄棋子即可穿戴或合成金铲铲大件！)");
                refreshAllViews();
            });

            itemCol.addView(ivItem);
            itemCol.addView(btnItem);
            itemBenchLayout.addView(itemCol);
        }

        boardView.selectedBenchIdx = this.selectedBenchIdx;
        boardView.selectedBoardR = this.selectedBoardR;
        boardView.selectedBoardC = this.selectedBoardC;
        boardView.invalidate();
    }

    private String mapItemToKey(TFTItem.ItemID id) {
        switch (id) {
            case BF_SWORD: return "sword";
            case RECURVE_BOW: return "bow";
            case TEAR: return "tear";
            case CHAIN_VEST: return "armor";
            case INFINITY_EDGE: return "ie";
            case GUINSOO_RAGEBLADE: return "guinsoo";
            default: return "sword";
        }
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

    @Override
    public void onBenchClicked(int idx) {
        if (selectedItemIdx >= 0 && engine.bench[idx] != null) {
            if (engine.equipItemToChampion(selectedItemIdx, engine.bench[idx])) {
                feedLog("【穿戴成功】成功将装备给予【" + engine.bench[idx].name + "】！");
                selectedItemIdx = -1;
                showChampionDetailCard(engine.bench[idx]);
                refreshAllViews();
                return;
            }
        }

        if (selectedBenchIdx == idx) {
            selectedBenchIdx = -1;
        } else {
            selectedBenchIdx = idx;
            selectedBoardR = -1;
            selectedBoardC = -1;
            ChessChampion c = engine.bench[idx];
            if (c != null) {
                showChampionDetailCard(c);
            }
        }
        refreshAllViews();
    }

    @Override
    public void onBoardTileClicked(int r, int c) {
        if (r < 2) {
            if (engine.enemyBoard[r][c] != null) {
                showChampionDetailCard(engine.enemyBoard[r][c]);
            }
            return;
        }

        if (selectedItemIdx >= 0 && engine.board[r][c] != null) {
            if (engine.equipItemToChampion(selectedItemIdx, engine.board[r][c])) {
                feedLog("【穿戴成功】成功将装备给予【" + engine.board[r][c].name + "】！");
                selectedItemIdx = -1;
                showChampionDetailCard(engine.board[r][c]);
                refreshAllViews();
                return;
            }
        }

        if (selectedBenchIdx >= 0) {
            if (engine.deployChampion(selectedBenchIdx, r, c)) {
                feedLog("【登坛布阵】英雄入阵战棋格 (" + r + "," + c + ")！");
                selectedBenchIdx = -1;
            } else {
                Toast.makeText(this, "上阵人口已达当前等级上限！请先研习道法提升人口！", Toast.LENGTH_SHORT).show();
            }
        } else if (selectedBoardR >= 0 && selectedBoardC >= 0) {
            ChessChampion temp = engine.board[r][c];
            engine.board[r][c] = engine.board[selectedBoardR][selectedBoardC];
            engine.board[selectedBoardR][selectedBoardC] = temp;
            selectedBoardR = -1;
            selectedBoardC = -1;
        } else {
            if (engine.board[r][c] != null) {
                selectedBoardR = r;
                selectedBoardC = c;
                showChampionDetailCard(engine.board[r][c]);
            }
        }
        refreshAllViews();
    }

    private void showChampionDetailCard(ChessChampion c) {
        String key = c.id.replace("c_", "");
        Bitmap portrait = cardBitmaps.get(key);
        if (portrait != null) {
            ivDetailPortrait.setVisibility(View.VISIBLE);
            ivDetailPortrait.setImageBitmap(portrait);
        } else {
            ivDetailPortrait.setVisibility(View.GONE);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("★【").append(c.name).append("】").append(c.star).append("星 (").append(c.origin).append("·").append(c.roleClass).append(")\n")
          .append("属性：气血 ").append(c.getMaxHp()).append(" ｜ 攻击 ").append(c.getAttack())
          .append(" ｜ 护甲 ").append(c.getArmor()).append(" ｜ 魔抗 ").append(c.getMagicResist())
          .append("\n攻速 ").append(String.format("%.2f", c.getAttackSpeed())).append("次/秒 ｜ 法强 ").append(c.getApPercent()).append("% ｜ 射程 ").append(c.attackRange).append("格\n")
          .append("大招神通：").append(c.skillName).append("\n效果：").append(c.skillDesc).append("\n")
          .append("背景生平：").append(c.background);
        if (!c.items.isEmpty()) {
            sb.append("\n已装备物品：");
            for (TFTItem it : c.items) {
                sb.append("[").append(it.id.name).append("] ");
            }
        }
        feedLog(sb.toString());
    }

    private void executeCombatRound() {
        if (engine.getBoardCount() == 0) {
            Toast.makeText(this, "棋盘上尚未布置任何英雄！请先从备战席上阵英雄！", Toast.LENGTH_SHORT).show();
            return;
        }

        feedLog("【⚔️ 全场开战】英雄面向目标索敌走砍，双抗真实减伤，满蓝自动引动大招！");
        boardView.startLiveBattle();
    }

    @Override
    public void onCombatFinished(boolean isVictory) {
        runOnUiThread(() -> {
            if (isVictory) {
                feedLog("【★ 回合大捷 ★】我方阵线告捷，万剑与雷霆倾泻，大获全胜！获得回合胜利金币奖励！");
            } else {
                int damage = Math.max(6, 12 - engine.getBoardCount() * 2);
                engine.playerHealth = Math.max(0, engine.playerHealth - damage);
                feedLog("【回合失利】敌方阵线撕裂我方防线，小小道尊受到 " + damage + " 点真伤！剩余血量：" + engine.playerHealth);
            }

            if (engine.playerHealth <= 0) {
                Toast.makeText(this, "小小道尊血量竭尽，重整旗鼓重新开局！", Toast.LENGTH_LONG).show();
                engine.playerHealth = 100;
            }

            engine.startNewRound();
            refreshAllViews();
        });
    }

    private void feedLog(String msg) {
        if (tvDetailFeed != null) {
            String old = tvDetailFeed.getText().toString();
            tvDetailFeed.setText(msg + "\n---------------------\n" + old);
        }
    }
}
