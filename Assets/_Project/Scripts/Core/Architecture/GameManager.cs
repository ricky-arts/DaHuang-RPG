using System;
using HongHuang.Core.EventBus;
using HongHuang.Core.Input;
using HongHuang.Core.Storage;
using HongHuang.Gameplay.Combat;
using HongHuang.Gameplay.Realm;
using HongHuang.Gameplay.Artifact;
using HongHuang.Gameplay.Narrative;
using HongHuang.UI.Framework;

namespace HongHuang.Core.Architecture
{
    public enum GameState
    {
        Bootstrapping,   // 引擎自检与配置初始化
        TitleScreen,     // 标题主选单
        InCombat,        // 战斗演练中
        InDialogue,      // 剧情对话中
        Breakthrough,    // 境界天劫结算
        Paused           // 暂停中
    }

    /// <summary>
    /// 游戏总控状态机与全局生命周期中枢
    /// 串联输入、战斗、修真、法宝、剧情、UI与安全存盘
    /// </summary>
    public class GameManager
    {
        private static GameManager _instance;
        public static GameManager Instance => _instance ??= new GameManager();

        public GameState CurrentState { get; private set; } = GameState.Bootstrapping;

        // 全局核心系统引用
        public TouchInputController InputCtrl { get; } = new TouchInputController();
        public RealmManager PlayerRealm { get; private set; }
        public NatalArtifact PlayerArtifact { get; private set; }
        public NarrativeEngine Narrative { get; private set; }

        public CombatEntity PlayerCombatEntity { get; private set; }
        public CombatEntity CurrentTargetEnemy { get; private set; }

        private byte[] _saveKey;
        private byte[] _saveIv;

        public void InitializeGame(string deviceId, string packageName)
        {
            // 1. 初始化派生防篡改密钥
            var (k, iv) = SecureSaveEngine.DeriveKey(deviceId, packageName);
            _saveKey = k;
            _saveIv = iv;

            // 2. 初始化核心系统
            PlayerRealm = new RealmManager(RealmTier.LianQi);
            PlayerArtifact = new NatalArtifact("太初辟易剑", NatalVesselType.FlyingSword);
            Narrative = new NarrativeEngine();

            PlayerCombatEntity = new CombatEntity
            {
                Name = "青莲道人 (主角)",
                Realm = RealmTier.LianQi,
                Element = ElementType.Wood,
                MaxHp = 1500,
                CurrentHp = 1500,
                Attack = 220,
                Defense = 120,
                CritRate = 0.12,
                CritMultiplier = 1.6,
                MaxTenacity = 100,
                Tenacity = 100
            };

            CurrentState = GameState.TitleScreen;
        }

        public void StartNewAdventure()
        {
            CurrentState = GameState.InCombat;
            UIManager.PushPanel("MainHudPanel");

            // 刷出新手引导敌人
            SpawnEnemy("九幽阴煞残魂", RealmTier.LianQi, ElementType.Earth, 800, 90, 60);
        }

        public void SpawnEnemy(string name, RealmTier realm, ElementType element, double hp, double atk, double def)
        {
            CurrentTargetEnemy = new CombatEntity
            {
                Name = name,
                Realm = realm,
                Element = element,
                MaxHp = hp,
                CurrentHp = hp,
                Attack = atk,
                Defense = def,
                CritRate = 0.05,
                CritMultiplier = 1.5,
                MaxTenacity = 60,
                Tenacity = 60
            };
        }

        /// <summary>
        /// 每帧主循环心跳驱动 (对应 Unity Update / FixedUpdate)
        /// </summary>
        public void UpdateGameLoop(double deltaTime)
        {
            if (CurrentState != GameState.InCombat || CurrentTargetEnemy == null)
            {
                InputCtrl.ResetFrameTriggers();
                return;
            }

            var input = InputCtrl.CurrentSnapshot;

            // 响应攻击输入
            if (input.AttackPressed)
            {
                CombatEngine.CalculateDamage(PlayerCombatEntity, CurrentTargetEnemy, 1.0);
            }
            // 响应技能输入
            else if (input.SkillSlotPressed > 0)
            {
                double skillMult = input.SkillSlotPressed == 1 ? 1.5 : 2.2;
                CombatEngine.CalculateDamage(PlayerCombatEntity, CurrentTargetEnemy, skillMult);
            }
            // 响应本命法宝大招
            else if (input.ArtifactPressed)
            {
                CombatEngine.CalculateDamage(PlayerCombatEntity, CurrentTargetEnemy, 3.5);
            }

            // 检查敌人死灭
            if (CurrentTargetEnemy.CurrentHp <= 0)
            {
                OnEnemyDefeated();
            }

            InputCtrl.ResetFrameTriggers();
        }

        private void OnEnemyDefeated()
        {
            // 结算修为奖励并吸收精血
            PlayerRealm.CurrentCultivation = PlayerRealm.CurrentCultivation.Value + 500L;
            PlayerArtifact.CultivateWithBlood(0.15);

            CurrentTargetEnemy = null;

            // 检查是否可引动突破
            if (PlayerRealm.CanAttemptBreakthrough(out _))
            {
                CurrentState = GameState.Breakthrough;
                var spec = PlayerRealm.GetTribulationSpec();
                UIManager.PushPanel("RealmBreakthroughPanel", spec.TribulationName);
            }
        }

        /// <summary>
        /// 切后台或手动存档
        /// </summary>
        public byte[] SaveGameData()
        {
            string payload = $"{{\"realm\":\"{PlayerRealm.CurrentRealm}\",\"cultivation\":{PlayerRealm.CurrentCultivation.Value},\"artifact\":\"{PlayerArtifact.CustomName}\"}}";
            return SecureSaveEngine.EncryptWithHmac(payload, _saveKey, _saveIv);
        }

        public void PauseGame()
        {
            if (CurrentState == GameState.InCombat)
                CurrentState = GameState.Paused;
        }

        public void ResumeGame()
        {
            if (CurrentState == GameState.Paused)
                CurrentState = GameState.InCombat;
        }
    }
}
