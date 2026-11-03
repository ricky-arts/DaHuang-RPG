using System;
using System.Collections.Generic;

namespace HongHuang.Core.ObjectPool
{
    /// <summary>
    /// 高性能通用对象池，杜绝运行时频繁分配导致 GC
    /// </summary>
    public class SimpleObjectPool<T> where T : class
    {
        private readonly Stack<T> _pool;
        private readonly Func<T> _factory;
        private readonly Action<T> _onGet;
        private readonly Action<T> _onRelease;

        public SimpleObjectPool(Func<T> factory, Action<T> onGet = null, Action<T> onRelease = null, int initialCapacity = 16)
        {
            _factory = factory ?? throw new ArgumentNullException(nameof(factory));
            _onGet = onGet;
            _onRelease = onRelease;
            _pool = new Stack<T>(initialCapacity);

            for (int i = 0; i < initialCapacity; i++)
            {
                _pool.Push(_factory());
            }
        }

        public int CountInactive => _pool.Count;

        public T Get()
        {
            T item = _pool.Count > 0 ? _pool.Pop() : _factory();
            _onGet?.Invoke(item);
            return item;
        }

        public void Release(T item)
        {
            if (item == null) return;
            _onRelease?.Invoke(item);
            _pool.Push(item);
        }

        public void Clear()
        {
            _pool.Clear();
        }
    }
}
