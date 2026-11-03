using System;
using System.Collections.Generic;

namespace HongHuang.UI.Framework
{
    public interface IUIPanel
    {
        string PanelName { get; }
        bool IsVisible { get; }
        void OnOpen(object args = null);
        void OnClose();
        void OnPause();
        void OnResume();
    }

    /// <summary>
    /// UI 导航栈与层级管理器 (支持返回上级栈帧、入栈暂停、出栈恢复)
    /// </summary>
    public static class UIManager
    {
        private static readonly Stack<IUIPanel> _panelStack = new Stack<IUIPanel>();
        private static readonly Dictionary<string, IUIPanel> _registeredPanels = new Dictionary<string, IUIPanel>();

        public static void Register(IUIPanel panel)
        {
            if (panel != null)
            {
                _registeredPanels[panel.PanelName] = panel;
            }
        }

        public static void PushPanel(string panelName, object args = null)
        {
            if (!_registeredPanels.TryGetValue(panelName, out var panel))
            {
                throw new KeyNotFoundException($"未找到已注册的 UI 面板: {panelName}");
            }

            if (_panelStack.Count > 0)
            {
                var top = _panelStack.Peek();
                top.OnPause();
            }

            _panelStack.Push(panel);
            panel.OnOpen(args);
        }

        public static void PopPanel()
        {
            if (_panelStack.Count == 0) return;

            var top = _panelStack.Pop();
            top.OnClose();

            if (_panelStack.Count > 0)
            {
                var next = _panelStack.Peek();
                next.OnResume();
            }
        }

        public static IUIPanel CurrentPanel => _panelStack.Count > 0 ? _panelStack.Peek() : null;
        public static int StackDepth => _panelStack.Count;

        public static void Clear()
        {
            while (_panelStack.Count > 0)
            {
                PopPanel();
            }
            _registeredPanels.Clear();
        }
    }
}
