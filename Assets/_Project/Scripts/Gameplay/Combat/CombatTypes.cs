namespace HongHuang.Gameplay.Combat
{
    /// <summary>
    /// 五行灵根属性
    /// </summary>
    public enum ElementType
    {
        None = 0,
        Metal = 1, // 金
        Wood = 2,  // 木
        Water = 3, // 水
        Fire = 4,  // 火
        Earth = 5  // 土
    }

    /// <summary>
    /// 修真境界层级
    /// </summary>
    public enum RealmTier
    {
        LianQi = 1,        // 练气
        ZhuJi = 2,         // 筑基
        JinDan = 3,        // 金丹
        YuanYing = 4,      // 元婴
        HuaShen = 5,       // 化神
        LianXu = 6,        // 炼虚
        HeTi = 7,          // 合体
        DaCheng = 8,       // 大乘
        DuJie = 9,         // 渡劫
        HunYuanDaLuo = 10, // 混元大罗金仙 (圣人)
        TianDao = 11       // 天道境
    }

    /// <summary>
    /// 战斗受击实体属性模型
    /// </summary>
    public class CombatEntity
    {
        public string Name { get; set; }
        public RealmTier Realm { get; set; }
        public ElementType Element { get; set; }

        public double MaxHp { get; set; }
        public double CurrentHp { get; set; }

        public double Attack { get; set; }
        public double Defense { get; set; }

        public double PenetrationFlat { get; set; }     // 固定穿透
        public double PenetrationPercent { get; set; }  // 百分比穿透 (0.0 ~ 1.0)

        public double CritRate { get; set; }            // 暴击率 (0.0 ~ 1.0)
        public double CritMultiplier { get; set; }      // 暴击伤害倍率 (默认 1.5)

        public double Tenacity { get; set; }            // 当前定力/韧性值
        public double MaxTenacity { get; set; }         // 最大定力值
        public bool IsBroken { get; set; }              // 是否处于破韧虚弱状态
    }

    /// <summary>
    /// 伤害结算结果明细
    /// </summary>
    public struct DamageResult
    {
        public double FinalDamage;
        public bool IsCrit;
        public bool IsElementAdvantage;
        public bool IsElementDisadvantage;
        public double RealmModifier;
        public bool TriggeredBreak;
    }
}
