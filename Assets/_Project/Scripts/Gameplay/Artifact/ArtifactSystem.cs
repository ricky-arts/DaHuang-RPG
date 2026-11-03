using System;
using System.Collections.Generic;

namespace HongHuang.Gameplay.Artifact
{
    /// <summary>
    /// 装备与法宝品质阶位 (八大阶位)
    /// </summary>
    public enum ItemQuality
    {
        FanQi = 1,       // 凡器 (白)
        BaoQi = 2,       // 宝器 (绿)
        LingQi = 3,      // 灵器 (蓝)
        BaoLing = 4,     // 宝灵 (紫)
        XianQi = 5,      // 仙器 (橙)
        GuBao = 6,       // 古宝 (红)
        HouTianZhiBao = 7,// 后天至宝 (金)
        HunDunLingBao = 8 // 混沌灵宝 (彩)
    }

    /// <summary>
    /// 装备八大槽位
    /// </summary>
    public enum EquipSlot
    {
        Weapon = 0,      // 武器 (飞剑/宝幡/长枪)
        Crown = 1,       // 法冠
        Robe = 2,        // 道袍
        Boots = 3,       // 云履
        Pendant = 4,     // 玉佩
        Ring = 5,        // 法戒
        Mirror = 6,      // 护心镜
        ArrayPlate = 7   // 阵盘
    }

    /// <summary>
    /// 装备属性词缀
    /// </summary>
    public class ItemAffix
    {
        public string Name { get; set; }
        public string Description { get; set; }
        public string StatType { get; set; } // "Atk", "Def", "CritRate", "PenFlat", "Hp"
        public double Value { get; set; }
    }

    /// <summary>
    /// 装备实体实例
    /// </summary>
    public class EquipmentItem
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public EquipSlot Slot { get; set; }
        public ItemQuality Quality { get; set; }
        public double BaseAttack { get; set; }
        public double BaseDefense { get; set; }
        public double BaseHp { get; set; }

        public List<ItemAffix> Affixes { get; set; } = new List<ItemAffix>();
        public string LegendaryInscription { get; set; } // 洪荒传奇铭文
    }

    /// <summary>
    /// 本命法宝器胚类型
    /// </summary>
    public enum NatalVesselType
    {
        FlyingSword = 1, // 诛仙飞剑 (高爆发/攻伐穿透)
        HeavenlySeal = 2,// 翻天宝印 (高破韧/钝击碎岳)
        NineCauldron = 3,// 山河宝鼎 (高护体/防御反弹)
        ArrayBanner = 4, // 诸天阵旗 (五行增伤/范围迟滞)
        AncientZither = 5// 太古遗音 (灵力吸取/心神惑乱)
    }

    /// <summary>
    /// 本命法宝系统 (核心永久陪伴与成长养成)
    /// 9阶10星强化成长曲线
    /// </summary>
    public class NatalArtifact
    {
        public string CustomName { get; set; }
        public NatalVesselType VesselType { get; set; }
        public int Tier { get; private set; } = 1;       // 1 ~ 9 阶 (随玩家大境界解封)
        public int StarLevel { get; private set; } = 0;  // 0 ~ 10 星 (消耗天材地宝升星)

        public double BloodBondDegree { get; private set; } = 0.0; // 精血契合度 (0.0 ~ 1.0)
        public bool IsAwakened { get; private set; } = false;      // 器灵是否苏醒

        public double BonusAttack => (100 * Tier * (1 + StarLevel * 0.15)) * (1.0 + BloodBondDegree * 0.5);
        public double BonusDefense => (60 * Tier * (1 + StarLevel * 0.12)) * (1.0 + BloodBondDegree * 0.5);

        public NatalArtifact(string name, NatalVesselType vesselType)
        {
            CustomName = name;
            VesselType = vesselType;
        }

        /// <summary>
        /// 精血祭炼 (提升契合度)
        /// </summary>
        public bool CultivateWithBlood(double bloodEssence)
        {
            BloodBondDegree = Math.Min(1.0, BloodBondDegree + bloodEssence);
            if (BloodBondDegree >= 0.8 && !IsAwakened)
            {
                IsAwakened = true; // 器灵觉醒
                return true; // 触发觉醒事件
            }
            return false;
        }

        /// <summary>
        /// 异火淬炼升星 (0星 -> 10星)
        /// </summary>
        public bool TryUpgradeStar()
        {
            if (StarLevel >= 10) return false;
            StarLevel++;
            return true;
        }

        /// <summary>
        /// 渡劫破阶 (突破至下一阶，需满10星)
        /// </summary>
        public bool TryAdvanceTier(Combat.RealmTier playerRealm)
        {
            if (StarLevel < 10) return false;
            if (Tier >= 9) return false;
            // 阶位不能超过玩家境界
            if (Tier >= (int)playerRealm) return false;

            Tier++;
            StarLevel = 0;
            return true;
        }

        /// <summary>
        /// 获取专属神威大招名称
        /// </summary>
        public string GetDivineSkillName()
        {
            return VesselType switch
            {
                NatalVesselType.FlyingSword => "【万剑归宗·一剑破万法】",
                NatalVesselType.HeavenlySeal => "【翻天覆地·不周残岳镇】",
                NatalVesselType.NineCauldron => "【九州结界·乾坤鼎镇压】",
                NatalVesselType.ArrayBanner => "【十绝阵变·两仪颠倒杀】",
                NatalVesselType.AncientZither => "【太古天音·广陵止杀伐】",
                _ => "【本命神威】"
            };
        }
    }
}
