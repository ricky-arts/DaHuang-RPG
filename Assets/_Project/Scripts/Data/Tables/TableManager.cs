using System;
using System.Collections.Generic;
using System.Text.Json;

namespace HongHuang.Data.Tables
{
    public class CharacterTemplateConfig
    {
        public string id { get; set; }
        public string name { get; set; }
        public string realm { get; set; }
        public string element { get; set; }
        public double baseHp { get; set; }
        public double baseAtk { get; set; }
        public double baseDef { get; set; }
        public double critRate { get; set; }
    }

    public class SkillTemplateConfig
    {
        public string id { get; set; }
        public string name { get; set; }
        public string element { get; set; }
        public double multiplier { get; set; }
        public double cooldownSec { get; set; }
        public double tenacityDamage { get; set; }
    }

    /// <summary>
    /// 静态配置表总管理器 (只读字典缓存，零运行时GC)
    /// </summary>
    public static class TableManager
    {
        private static readonly Dictionary<string, CharacterTemplateConfig> _characterMap = new Dictionary<string, CharacterTemplateConfig>();
        private static readonly Dictionary<string, SkillTemplateConfig> _skillMap = new Dictionary<string, SkillTemplateConfig>();

        public static void LoadCharactersFromJson(string json)
        {
            _characterMap.Clear();
            var list = JsonSerializer.Deserialize<List<CharacterTemplateConfig>>(json);
            if (list != null)
            {
                foreach (var item in list)
                {
                    _characterMap[item.id] = item;
                }
            }
        }

        public static void LoadSkillsFromJson(string json)
        {
            _skillMap.Clear();
            var list = JsonSerializer.Deserialize<List<SkillTemplateConfig>>(json);
            if (list != null)
            {
                foreach (var item in list)
                {
                    _skillMap[item.id] = item;
                }
            }
        }

        public static CharacterTemplateConfig GetCharacter(string id)
        {
            return _characterMap.TryGetValue(id, out var cfg) ? cfg : null;
        }

        public static SkillTemplateConfig GetSkill(string id)
        {
            return _skillMap.TryGetValue(id, out var cfg) ? cfg : null;
        }

        public static int CharacterCount => _characterMap.Count;
        public static int SkillCount => _skillMap.Count;
    }
}
