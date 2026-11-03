using System;
using HongHuang.Core.Security;

namespace HongHuang.Gameplay.Realm
{
    /// <summary>
    /// 玩家修真境界管理与渡劫算法
    /// </summary>
    public class RealmManager
    {
        public Combat.RealmTier CurrentRealm { get; private set; }
        public int MinorStage { get; private set; } // 1~9 重天 (前期/中期/后期/大圆满)

        public ObfuscatedLong CurrentCultivation { get; set; } // 当前修为 (防修改)
        public ObfuscatedLong RequiredCultivation { get; private set; } // 突破所需修为
        public ObfuscatedInt KarmaValue { get; set; } // 因果善恶值 (-10000 ~ +10000)

        public RealmManager(Combat.RealmTier initialRealm = Combat.RealmTier.LianQi)
        {
            CurrentRealm = initialRealm;
            MinorStage = 1;
            CurrentCultivation = 0L;
            KarmaValue = 0;
            UpdateRequiredCultivation();
        }

        private void UpdateRequiredCultivation()
        {
            // 随境界与小境界呈指数级提升
            long baseValue = 1000L * (long)Math.Pow(3.5, (int)CurrentRealm - 1);
            RequiredCultivation = baseValue * MinorStage;
        }

        /// <summary>
        /// 检查是否可以进行大境界突破/渡劫
        /// </summary>
        public bool CanAttemptBreakthrough(out string reason)
        {
            if (CurrentCultivation.Value < RequiredCultivation.Value)
            {
                reason = $"修为未臻圆满，尚需 {RequiredCultivation.Value - CurrentCultivation.Value} 点修为";
                return false;
            }

            reason = "灵气充盈，可引动天地气机突破！";
            return true;
        }

        /// <summary>
        /// 计算天劫雷击威力 (受因果业障影响)
        /// 善果深厚降下祥云瑞气，业障过重降下九天寂灭黑雷
        /// </summary>
        public (int LightningWaves, double BaseStrikeDamage, string TribulationName) GetTribulationSpec()
        {
            int baseWaves = CurrentRealm switch
            {
                Combat.RealmTier.JinDan => 36,  // 四九小天劫
                Combat.RealmTier.HuaShen => 54, // 六九玄天劫
                Combat.RealmTier.DaCheng => 81, // 九九灭世天劫
                _ => 9                          // 普通筑基或小天劫
            };

            double baseDmg = 500.0 * Math.Pow(2.2, (int)CurrentRealm);

            // 因果业障影响：Karma 为负数时大幅加重雷劫威力
            int karma = KarmaValue.Value;
            if (karma < -1000)
            {
                double penalty = Math.Min(3.0, 1.0 + Math.Abs(karma) / 2500.0);
                baseDmg *= penalty;
                return (baseWaves, baseDmg, "九幽寂灭紫霄黑雷（业障缠身，威力倍增）");
            }
            else if (karma > 1000)
            {
                double blessing = Math.Max(0.7, 1.0 - karma / 5000.0);
                baseDmg *= blessing;
                return (baseWaves, baseDmg, "九霄太乙度厄青雷（功德金莲护体，劫雷温和）");
            }

            return (baseWaves, baseDmg, "乾坤正气五行神雷");
        }

        /// <summary>
        /// 成功突破境界
        /// </summary>
        public void ConfirmBreakthroughSuccess()
        {
            if (MinorStage < 9)
            {
                MinorStage++;
            }
            else
            {
                if (CurrentRealm < Combat.RealmTier.TianDao)
                {
                    CurrentRealm++;
                    MinorStage = 1;
                }
            }
            CurrentCultivation = 0L;
            UpdateRequiredCultivation();
        }
    }
}
