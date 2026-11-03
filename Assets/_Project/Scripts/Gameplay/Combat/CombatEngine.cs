using System;

namespace HongHuang.Gameplay.Combat
{
    /// <summary>
    /// 洪荒战斗核心计算引擎：五行相克、双曲减伤、非线性境界压制、破韧硬直
    /// </summary>
    public static class CombatEngine
    {
        public const double DefConstant = 800.0; // 双曲减伤基准常数

        /// <summary>
        /// 判定五行相克关系
        /// 金克木 -> 木克土 -> 土克水 -> 水克火 -> 火克金
        /// </summary>
        public static (double Multiplier, bool Advantage, bool Disadvantage) CalculateElementModifier(ElementType attacker, ElementType defender)
        {
            if (attacker == ElementType.None || defender == ElementType.None)
                return (1.0, false, false);

            if (attacker == defender)
                return (1.0, false, false);

            // 克制逻辑
            bool isAdvantage = (attacker == ElementType.Metal && defender == ElementType.Wood) ||
                               (attacker == ElementType.Wood && defender == ElementType.Earth) ||
                               (attacker == ElementType.Earth && defender == ElementType.Water) ||
                               (attacker == ElementType.Water && defender == ElementType.Fire) ||
                               (attacker == ElementType.Fire && defender == ElementType.Metal);

            if (isAdvantage)
                return (1.35, true, false); // 克制增伤 35%

            // 被克逻辑
            bool isDisadvantage = (defender == ElementType.Metal && attacker == ElementType.Wood) ||
                                  (defender == ElementType.Wood && attacker == ElementType.Earth) ||
                                  (defender == ElementType.Earth && attacker == ElementType.Water) ||
                                  (defender == ElementType.Water && attacker == ElementType.Fire) ||
                                  (defender == ElementType.Fire && attacker == ElementType.Metal);

            if (isDisadvantage)
                return (0.75, false, true); // 被克减伤 25%

            return (1.0, false, false);
        }

        /// <summary>
        /// 非线性境界压制计算
        /// deltaRealm = 攻击方境界 - 防御方境界
        /// </summary>
        public static double CalculateRealmModifier(int deltaRealm)
        {
            if (deltaRealm == 0) return 1.0;

            if (deltaRealm > 0)
            {
                // 高境界碾压低境界：平滑抛物线上涨
                return 1.0 + 0.25 * deltaRealm + 0.05 * Math.Pow(deltaRealm, 2);
            }
            else
            {
                // 低境界挑战高境界（逆伐）：阻尼平滑衰减，保底 0.15 防被彻底打穿
                int absDelta = Math.Abs(deltaRealm);
                double mod = 1.0 / (1.0 + 0.35 * absDelta + 0.08 * Math.Pow(absDelta, 2));
                return Math.Max(0.15, mod);
            }
        }

        /// <summary>
        /// 综合伤害结算
        /// </summary>
        public static DamageResult CalculateDamage(CombatEntity attacker, CombatEntity defender, double skillRate = 1.0, bool forceCrit = false)
        {
            // 1. 有效防御力计算 (考虑百分比与固定穿透)
            double effectiveDef = defender.Defense * (1.0 - Math.Clamp(attacker.PenetrationPercent, 0.0, 1.0)) - attacker.PenetrationFlat;
            effectiveDef = Math.Max(0.0, effectiveDef);

            // 2. 双曲防御减伤率 (防御永不溢出爆表)
            double defReduction = effectiveDef / (DefConstant + effectiveDef);
            double baseDamage = attacker.Attack * skillRate * (1.0 - defReduction);

            // 3. 五行生克修正
            var elemRes = CalculateElementModifier(attacker.Element, defender.Element);
            double elemMod = elemRes.Multiplier;

            // 4. 境界压制修正
            int deltaRealm = (int)attacker.Realm - (int)defender.Realm;
            double realmMod = CalculateRealmModifier(deltaRealm);

            // 5. 暴击判定
            bool isCrit = forceCrit || (attacker.CritRate > 0 && new Random().NextDouble() < attacker.CritRate);
            double critMod = isCrit ? Math.Max(1.5, attacker.CritMultiplier) : 1.0;

            // 6. 破韧虚弱增伤 (处于 Break 状态增伤 50%)
            double breakMod = defender.IsBroken ? 1.5 : 1.0;

            // 最终伤害
            double finalDamage = Math.Max(1.0, baseDamage * elemMod * realmMod * critMod * breakMod);

            // 7. 削减定力韧性
            bool breakTriggered = false;
            if (!defender.IsBroken && defender.MaxTenacity > 0)
            {
                double tenacityDamage = attacker.Attack * 0.2 * skillRate; // 韧性伤害
                defender.Tenacity -= tenacityDamage;
                if (defender.Tenacity <= 0)
                {
                    defender.Tenacity = 0;
                    defender.IsBroken = true;
                    breakTriggered = true;
                }
            }

            // 扣除气血
            defender.CurrentHp = Math.Max(0, defender.CurrentHp - finalDamage);

            return new DamageResult
            {
                FinalDamage = finalDamage,
                IsCrit = isCrit,
                IsElementAdvantage = elemRes.Advantage,
                IsElementDisadvantage = elemRes.Disadvantage,
                RealmModifier = realmMod,
                TriggeredBreak = breakTriggered
            };
        }
    }
}
