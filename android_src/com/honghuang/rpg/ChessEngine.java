package com.honghuang.rpg;

import java.util.*;

public class ChessEngine {
    public int gold = 15; // 初始灵石
    public int level = 3; // 初始人口 (等级)
    public int exp = 0;   // 人口经验
    public int round = 1; // 当前回合
    public int playerHealth = 100; // 玩家小小道尊血量

    // 备战席 (最多 8 格)
    public ChessChampion[] bench = new ChessChampion[8];

    // 战斗棋盘 (8 列 x 4 行)
    public ChessChampion[][] board = new ChessChampion[4][8];

    // 敌方棋盘镜像 (4 行 x 8 列)
    public ChessChampion[][] enemyBoard = new ChessChampion[4][8];

    // 商店卡槽 (5 张)
    public List<ChessChampion> shopCards = new ArrayList<>(5);

    // 装备库备选池 (最多 6 件)
    public List<ChessEquipment> equipmentInventory = new ArrayList<>(6);

    private final List<ChessChampion> pool = ChessChampion.getAllPoolChampions();
    private final Random rng = new Random();

    public ChessEngine() {
        // 初始附赠 2 件基础法宝散件
        equipmentInventory.add(new ChessEquipment(ChessEquipment.EquipType.SHEN_TIAN_JIAN));
        equipmentInventory.add(new ChessEquipment(ChessEquipment.EquipType.XUAN_WU_JIA));
        refreshShop(true);
        setupDefaultEnemyBoard();
    }

    /**
     * 计算利息与回合收入
     * 基础 5 金币 + 利息 (每 10 金币多得 1，最多 5 金币)
     */
    public int calculateRoundIncome() {
        int interest = Math.min(5, gold / 10);
        return 5 + interest;
    }

    public void startNewRound() {
        round++;
        gold += calculateRoundIncome();
        exp += 2; // 每回合自动获得 2 点人口经验
        checkLevelUp();
        refreshShop(true);
        setupEnemyBoardForRound();
    }

    public void checkLevelUp() {
        int need = getExpForNextLevel();
        while (exp >= need && level < 9) {
            exp -= need;
            level++;
            need = getExpForNextLevel();
        }
    }

    public int getExpForNextLevel() {
        return level * 4 + (level > 5 ? 12 : 4);
    }

    public boolean buyExp() {
        if (gold >= 4 && level < 9) {
            gold -= 4;
            exp += 4;
            checkLevelUp();
            return true;
        }
        return false;
    }

    /**
     * 刷新卡池商店
     */
    public boolean refreshShop(boolean free) {
        if (!free) {
            if (gold < 2) return false;
            gold -= 2;
        }
        shopCards.clear();
        for (int i = 0; i < 5; i++) {
            int cost = rollCardCost(level);
            List<ChessChampion> filtered = new ArrayList<>();
            for (ChessChampion c : pool) {
                if (c.cost == cost) filtered.add(c);
            }
            if (filtered.isEmpty()) filtered = pool;
            ChessChampion picked = filtered.get(rng.nextInt(filtered.size())).copy();
            shopCards.add(picked);
        }
        return true;
    }

    private int rollCardCost(int lvl) {
        double r = rng.nextDouble();
        if (lvl <= 3) {
            return r < 0.75 ? 1 : 2;
        } else if (lvl <= 5) {
            if (r < 0.45) return 1;
            if (r < 0.80) return 2;
            return 3;
        } else if (lvl <= 7) {
            if (r < 0.25) return 1;
            if (r < 0.55) return 2;
            if (r < 0.85) return 3;
            return 4;
        } else {
            if (r < 0.15) return 1;
            if (r < 0.35) return 2;
            if (r < 0.65) return 3;
            if (r < 0.90) return 4;
            return 5; // 5费大能
        }
    }

    /**
     * 购买棋子并自动触发“3合1升星”
     */
    public boolean buyChampion(int shopIndex) {
        if (shopIndex < 0 || shopIndex >= shopCards.size()) return false;
        ChessChampion card = shopCards.get(shopIndex);
        if (card == null || gold < card.cost) return false;

        // 寻找备战席空位
        int emptyIdx = -1;
        for (int i = 0; i < bench.length; i++) {
            if (bench[i] == null) {
                emptyIdx = i;
                break;
            }
        }
        if (emptyIdx == -1) return false; // 备战席已满

        gold -= card.cost;
        bench[emptyIdx] = card;
        shopCards.set(shopIndex, null); // 扣除该牌

        // 自动三连升星检查！
        triggerTripleStarUpgrade(card.name);
        return true;
    }

    /**
     * 3连自动合成 2星 / 3星 机制 (金铲铲精髓)
     */
    public void triggerTripleStarUpgrade(String champName) {
        // 1. 检查是否有 3 个 1 星同名棋子
        List<int[]> oneStarLocs = new ArrayList<>();
        for (int i = 0; i < bench.length; i++) {
            if (bench[i] != null && bench[i].name.equals(champName) && bench[i].star == 1) {
                oneStarLocs.add(new int[] { 0, i }); // 0 代表 bench
            }
        }
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].name.equals(champName) && board[r][c].star == 1) {
                    oneStarLocs.add(new int[] { 1, r, c }); // 1 代表 board
                }
            }
        }

        if (oneStarLocs.size() >= 3) {
            // 合成 2 星！保留第一个位置，销毁后两个
            int[] keep = oneStarLocs.get(0);
            ChessChampion target = (keep[0] == 0) ? bench[keep[1]] : board[keep[1]][keep[2]];
            target.star = 2;

            for (int k = 1; k < 3; k++) {
                int[] loc = oneStarLocs.get(k);
                if (loc[0] == 0) bench[loc[1]] = null;
                else board[loc[1]][loc[2]] = null;
            }

            // 递归检查是否能再合成 3 星
            checkTwoStarTripleUpgrade(champName);
        }
    }

    private void checkTwoStarTripleUpgrade(String champName) {
        List<int[]> twoStarLocs = new ArrayList<>();
        for (int i = 0; i < bench.length; i++) {
            if (bench[i] != null && bench[i].name.equals(champName) && bench[i].star == 2) {
                twoStarLocs.add(new int[] { 0, i });
            }
        }
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].name.equals(champName) && board[r][c].star == 2) {
                    twoStarLocs.add(new int[] { 1, r, c });
                }
            }
        }

        if (twoStarLocs.size() >= 3) {
            int[] keep = twoStarLocs.get(0);
            ChessChampion target = (keep[0] == 0) ? bench[keep[1]] : board[keep[1]][keep[2]];
            target.star = 3; // 升至金闪闪 3 星大能！

            for (int k = 1; k < 3; k++) {
                int[] loc = twoStarLocs.get(k);
                if (loc[0] == 0) bench[loc[1]] = null;
                else board[loc[1]][loc[2]] = null;
            }
        }
    }

    /**
     * 将装备穿戴到棋子上 (支持法宝合成)
     */
    public boolean equipItemToChampion(int equipInvIndex, ChessChampion target) {
        if (equipInvIndex < 0 || equipInvIndex >= equipmentInventory.size() || target == null) return false;
        ChessEquipment item = equipmentInventory.get(equipInvIndex);
        if (item == null) return false;

        // 如果棋子上已经有1件基础散件，并且新装也是基础散件 -> 自动融合成神品法宝！
        if (target.equipments.size() == 1 && !target.equipments.get(0).type.isCombined && !item.type.isCombined) {
            ChessEquipment first = target.equipments.get(0);
            ChessEquipment combined = tryCombineEquipment(first.type, item.type);
            if (combined != null) {
                target.equipments.clear();
                target.equipments.add(combined);
                equipmentInventory.remove(equipInvIndex);
                return true;
            }
        }

        if (target.addEquipment(item)) {
            equipmentInventory.remove(equipInvIndex);
            return true;
        }
        return false;
    }

    private ChessEquipment tryCombineEquipment(ChessEquipment.EquipType a, ChessEquipment.EquipType b) {
        if (a == ChessEquipment.EquipType.SHEN_TIAN_JIAN && b == ChessEquipment.EquipType.SHEN_TIAN_JIAN) {
            return new ChessEquipment(ChessEquipment.EquipType.ZHU_XIAN_JIAN);
        }
        if ((a == ChessEquipment.EquipType.SHEN_TIAN_JIAN && b == ChessEquipment.EquipType.XUAN_WU_JIA) ||
            (b == ChessEquipment.EquipType.SHEN_TIAN_JIAN && a == ChessEquipment.EquipType.XUAN_WU_JIA)) {
            return new ChessEquipment(ChessEquipment.EquipType.FAN_TIAN_YIN);
        }
        if ((a == ChessEquipment.EquipType.TAI_YI_FU && b == ChessEquipment.EquipType.SHEN_TIAN_JIAN) ||
            (b == ChessEquipment.EquipType.TAI_YI_FU && a == ChessEquipment.EquipType.SHEN_TIAN_JIAN)) {
            return new ChessEquipment(ChessEquipment.EquipType.DING_HAI_ZHU);
        }
        if ((a == ChessEquipment.EquipType.XUAN_WU_JIA && b == ChessEquipment.EquipType.HOU_TU_JING) ||
            (b == ChessEquipment.EquipType.XUAN_WU_JIA && a == ChessEquipment.EquipType.HOU_TU_JING)) {
            return new ChessEquipment(ChessEquipment.EquipType.TAI_JI_TU);
        }
        return null;
    }

    /**
     * 上阵棋子数量统计
     */
    public int getBoardCount() {
        int count = 0;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null) count++;
            }
        }
        return count;
    }

    /**
     * 上阵/下阵棋子
     */
    public boolean deployChampion(int benchIdx, int boardR, int boardC) {
        if (benchIdx < 0 || benchIdx >= bench.length) return false;
        if (boardR < 0 || boardR >= 4 || boardC < 0 || boardC >= 8) return false;

        ChessChampion toDeploy = bench[benchIdx];
        if (toDeploy == null) return false;

        // 如果目标格为空且上阵人数已达上限
        if (board[boardR][boardC] == null && getBoardCount() >= level) {
            return false;
        }

        // 交换或上阵
        ChessChampion old = board[boardR][boardC];
        board[boardR][boardC] = toDeploy;
        bench[benchIdx] = old;
        return true;
    }

    public boolean recallChampion(int boardR, int boardC) {
        if (boardR < 0 || boardR >= 4 || boardC < 0 || boardC >= 8) return false;
        ChessChampion champ = board[boardR][boardC];
        if (champ == null) return false;

        for (int i = 0; i < bench.length; i++) {
            if (bench[i] == null) {
                bench[i] = champ;
                board[boardR][boardC] = null;
                return true;
            }
        }
        return false;
    }

    /**
     * 计算活跃羁绊
     */
    public Map<String, Integer> calculateSynergies() {
        Map<String, Integer> counts = new HashMap<>();
        Set<String> uniqueChamps = new HashSet<>();

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                ChessChampion champ = board[r][c];
                if (champ != null && uniqueChamps.add(champ.name)) {
                    counts.put(champ.origin, counts.getOrDefault(champ.origin, 0) + 1);
                    counts.put(champ.roleClass, counts.getOrDefault(champ.roleClass, 0) + 1);
                }
            }
        }
        return counts;
    }

    private void setupDefaultEnemyBoard() {
        // 第一回合 2 个 1 费野怪/低阶对手
        enemyBoard[0][3] = pool.get(0).copy();
        enemyBoard[0][4] = pool.get(1).copy();
    }

    private void setupEnemyBoardForRound() {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 8; c++) {
                enemyBoard[r][c] = null;
            }
        }
        int enemyCount = Math.min(round + 1, 8);
        for (int i = 0; i < enemyCount; i++) {
            int col = (i * 2) % 8;
            int row = (i / 4) % 2;
            int poolIdx = Math.min(pool.size() - 1, (round - 1) + rng.nextInt(3));
            ChessChampion enemy = pool.get(poolIdx).copy();
            if (round >= 5) enemy.star = 2;
            enemyBoard[row][col] = enemy;
        }

        // 回合胜率奖品：概率掉落法宝散件
        if (rng.nextDouble() < 0.40 && equipmentInventory.size() < 6) {
            ChessEquipment.EquipType[] types = ChessEquipment.EquipType.values();
            ChessEquipment.EquipType drop = types[rng.nextInt(4)]; // 掉落4种基础件之一
            equipmentInventory.add(new ChessEquipment(drop));
        }
    }
}
