using System;
using HongHuang.UI.Framework;

namespace HongHuang.UI.Panels
{
    /// <summary>
    /// 主界面 HUD 面板 (展示角色气血、灵气、境界与小地图)
    /// </summary>
    public class MainHudPanel : IUIPanel
    {
        public string PanelName => "MainHudPanel";
        public bool IsVisible { get; private set; }

        public void OnOpen(object args = null) => IsVisible = true;
        public void OnClose() => IsVisible = false;
        public void OnPause() { }
        public void OnResume() { }
    }

    /// <summary>
    /// 境界渡劫面板 (展示因果业障、引动天劫与心魔试炼)
    /// </summary>
    public class RealmBreakthroughPanel : IUIPanel
    {
        public string PanelName => "RealmBreakthroughPanel";
        public bool IsVisible { get; private set; }
        public string TribulationTitle { get; private set; }

        public void OnOpen(object args = null)
        {
            IsVisible = true;
            TribulationTitle = args as string ?? "五行普通天劫";
        }

        public void OnClose() => IsVisible = false;
        public void OnPause() { }
        public void OnResume() { }
    }

    /// <summary>
    /// 本命法宝祭炼与装备背包面板
    /// </summary>
    public class ArtifactBagPanel : IUIPanel
    {
        public string PanelName => "ArtifactBagPanel";
        public bool IsVisible { get; private set; }

        public void OnOpen(object args = null) => IsVisible = true;
        public void OnClose() => IsVisible = false;
        public void OnPause() { }
        public void OnResume() { }
    }
}
