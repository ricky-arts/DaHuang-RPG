using UnityEngine;
using HongHuang.Core.Architecture;
using HongHuang.Core.Audio;
using HongHuang.UI.Framework;
using HongHuang.UI.Panels;

namespace HongHuang.Bootstrap
{
    /// <summary>
    /// 游戏首发场景入口挂载脚本 (单例引导)
    /// </summary>
    public class GameBootstrapper : MonoBehaviour
    {
        private void Awake()
        {
            DontDestroyOnLoad(gameObject);

            // 1. 注册核心 UI 面板
            UIManager.Register(new MainHudPanel());
            UIManager.Register(new RealmBreakthroughPanel());
            UIManager.Register(new ArtifactBagPanel());

            // 2. 启动游戏总控
            string deviceId = SystemInfo.deviceUniqueIdentifier ?? "Android_Test_Device_888";
            GameManager.Instance.InitializeGame(deviceId, "com.honghuang.rpg");

            // 3. 启动首发古琴民乐 BGM
            AudioManager.Instance.PlayBgm("BGM_KunLun_GuQin_MainTheme");

            Debug.Log("[DaHuang] 《大荒》首发装机引导完成，游戏进入运行就绪态！");
        }

        private void Start()
        {
            // 自动开启第一卷试炼
            GameManager.Instance.StartNewAdventure();
        }

        private void Update()
        {
            // 驱动主游戏循环心跳
            GameManager.Instance.UpdateGameLoop(Time.deltaTime);
        }

        private void OnApplicationPause(bool pauseStatus)
        {
            if (pauseStatus)
            {
                // 切后台触发安全原子存档
                GameManager.Instance.SaveGameData();
                GameManager.Instance.PauseGame();
            }
            else
            {
                GameManager.Instance.ResumeGame();
            }
        }
    }
}
