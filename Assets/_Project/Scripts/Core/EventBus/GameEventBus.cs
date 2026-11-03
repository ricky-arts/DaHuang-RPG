using System;
using System.Collections.Generic;

namespace HongHuang.Core.EventBus
{
    /// <summary>
    /// 事件标记接口
    /// </summary>
    public interface IGameEvent { }

    /// <summary>
    /// 高性能、无GC、类型安全的事件总线
    /// </summary>
    public static class GameEventBus
    {
        private static class EventRegistry<T> where T : IGameEvent
        {
            public static readonly List<Action<T>> Handlers = new List<Action<T>>();
        }

        public static void Subscribe<T>(Action<T> handler) where T : IGameEvent
        {
            if (handler == null) return;
            var list = EventRegistry<T>.Handlers;
            if (!list.Contains(handler))
            {
                list.Add(handler);
            }
        }

        public static void Unsubscribe<T>(Action<T> handler) where T : IGameEvent
        {
            if (handler == null) return;
            EventRegistry<T>.Handlers.Remove(handler);
        }

        public static void Publish<T>(T gameEvent) where T : IGameEvent
        {
            var handlers = EventRegistry<T>.Handlers;
            for (int i = handlers.Count - 1; i >= 0; i--)
            {
                handlers[i]?.Invoke(gameEvent);
            }
        }

        public static void Clear<T>() where T : IGameEvent
        {
            EventRegistry<T>.Handlers.Clear();
        }
    }
}
