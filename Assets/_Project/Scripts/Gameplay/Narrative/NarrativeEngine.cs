using System;
using System.Collections.Generic;

namespace HongHuang.Gameplay.Narrative
{
    /// <summary>
    /// 洪荒四大史诗终局类型
    /// </summary>
    public enum GameEndingType
    {
        None = 0,
        FengShenYongChang = 1, // 封神永昌 (顺天道·成无上执法天帝)
        ChongLiDiXian = 2,     // 重立地仙 (立地道·人人如龙散仙界)
        TaiChuPoXu = 3,        // 太初破虚 (独断万古·破碎虚空飞升混沌)
        WanJieJuMie = 4        // 万劫俱灭 (化身魔尊·业火焚世重启洪荒)
    }

    /// <summary>
    /// 对话选项节点
    /// </summary>
    public class DialogueOption
    {
        public string Text { get; set; }
        public int KarmaDelta { get; set; } // 改变的因果善恶值
        public string NextNodeId { get; set; }
        public Action OnSelected { get; set; }
    }

    /// <summary>
    /// 剧情对话树节点
    /// </summary>
    public class DialogueNode
    {
        public string NodeId { get; set; }
        public string Speaker { get; set; }
        public string Content { get; set; }
        public List<DialogueOption> Options { get; set; } = new List<DialogueOption>();
    }

    /// <summary>
    /// 山海图志（碎片化博物志收录实体）
    /// </summary>
    public class ShanHaiRecord
    {
        public string Id { get; set; }
        public string Title { get; set; }
        public string AncientQuote { get; set; } // 古文志异记载
        public string LoreNotes { get; set; }    // 秘辛解读
        public bool IsUnlocked { get; set; }
    }

    /// <summary>
    /// 剧情决策状态树与因果引擎
    /// </summary>
    public class NarrativeEngine
    {
        public int Chapter { get; private set; } = 1; // 1 ~ 5 卷
        public int Karma { get; private set; } = 0;   // 因果值 (-10000 ~ +10000)
        private readonly HashSet<string> _storyFlags = new HashSet<string>();
        private readonly Dictionary<string, ShanHaiRecord> _shanHaiRecords = new Dictionary<string, ShanHaiRecord>();

        public void SetFlag(string flag) => _storyFlags.Add(flag);
        public bool HasFlag(string flag) => _storyFlags.Contains(flag);

        public void ApplyKarmaChange(int delta)
        {
            Karma = Math.Clamp(Karma + delta, -10000, 10000);
        }

        public void UnlockShanHaiRecord(string id, string title, string quote, string lore)
        {
            if (!_shanHaiRecords.ContainsKey(id))
            {
                _shanHaiRecords[id] = new ShanHaiRecord
                {
                    Id = id,
                    Title = title,
                    AncientQuote = quote,
                    LoreNotes = lore,
                    IsUnlocked = true
                };
            }
        }

        public int UnlockedRecordsCount => _shanHaiRecords.Count;

        /// <summary>
        /// 判定游戏大结局走向
        /// 根据玩家积累的因果值与关键剧情抉择 Flag
        /// </summary>
        public GameEndingType EvaluateEnding()
        {
            if (HasFlag("DESTROY_HEAVEN_AND_EARTH") || Karma <= -7000)
            {
                return GameEndingType.WanJieJuMie; // 万劫俱灭
            }

            if (HasFlag("ASCEND_TO_VOID_CHAOS") && Karma > -2000 && Karma < 2000)
            {
                return GameEndingType.TaiChuPoXu; // 太初破虚
            }

            if (HasFlag("RESTORE_EARTH_REINCARNATION") || (Karma >= 3000 && HasFlag("DEFEND_DI_DAO")))
            {
                return GameEndingType.ChongLiDiXian; // 重立地仙
            }

            if (Karma >= 5000 && HasFlag("UPHOLD_HEAVEN_ORDER"))
            {
                return GameEndingType.FengShenYongChang; // 封神永昌
            }

            // 默认结局分支：依善恶偏向
            return Karma >= 0 ? GameEndingType.ChongLiDiXian : GameEndingType.WanJieJuMie;
        }
    }
}
