using System;

namespace HongHuang.Core.Audio
{
    public enum SoundFxType
    {
        SwordSlash,        // 飞剑破空斩击
        ThunderStrike,     // 天劫紫霄雷鸣
        FireSpell,         // 三昧真火爆鸣
        HeavyImpact,       // 翻天印碎岳重击
        BreakStun,         // 破韧虚弱脆鸣
        BreakthroughFanfare// 破境登仙古钟长鸣
    }

    /// <summary>
    /// 国风音频与音效管理器 (支持民乐循环、打击感音频分层播放与音量调节)
    /// </summary>
    public class AudioManager
    {
        private static AudioManager _instance;
        public static AudioManager Instance => _instance ??= new AudioManager();

        public float BgmVolume { get; set; } = 0.8f;
        public float SfxVolume { get; set; } = 1.0f;
        public bool IsMuted { get; set; } = false;

        public string CurrentBgmTrack { get; private set; } = "None";
        public SoundFxType LastPlayedSfx { get; private set; }

        public void PlayBgm(string trackName)
        {
            CurrentBgmTrack = trackName;
        }

        public void PlaySfx(SoundFxType sfx)
        {
            if (IsMuted) return;
            LastPlayedSfx = sfx;
        }

        public void StopBgm()
        {
            CurrentBgmTrack = "None";
        }
    }
}
