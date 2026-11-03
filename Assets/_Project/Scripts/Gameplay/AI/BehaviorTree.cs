using System;
using System.Collections.Generic;

namespace HongHuang.Gameplay.AI
{
    /// <summary>
    /// 行为树节点执行状态
    /// </summary>
    public enum BTStatus
    {
        Success,
        Failure,
        Running
    }

    /// <summary>
    /// 行为树黑板上下文 (共享战斗态势与环境感知)
    /// </summary>
    public class AIBlackboard
    {
        public double SelfHpPercent { get; set; } = 1.0;
        public double DistanceToPlayer { get; set; } = 10.0;
        public bool IsPlayerBroken { get; set; } = false;
        public bool HasEmergencyTalismans { get; set; } = true;
        public double QiConcentration { get; set; } = 1.0;
        public string LastExecutedSkill { get; set; } = "";
    }

    /// <summary>
    /// 行为树基类节点
    /// </summary>
    public abstract class BTNode
    {
        public abstract BTStatus Tick(AIBlackboard bb);
    }

    /// <summary>
    /// 选择节点 (Selector): 类似逻辑 OR，只要一个子节点返回 Success/Running 就立即返回
    /// </summary>
    public class BTSelector : BTNode
    {
        private readonly List<BTNode> _children = new List<BTNode>();

        public BTSelector(params BTNode[] children)
        {
            _children.AddRange(children);
        }

        public override BTStatus Tick(AIBlackboard bb)
        {
            foreach (var child in _children)
            {
                var status = child.Tick(bb);
                if (status != BTStatus.Failure)
                {
                    return status;
                }
            }
            return BTStatus.Failure;
        }
    }

    /// <summary>
    /// 顺序节点 (Sequence): 类似逻辑 AND，所有子节点 Success 才算 Success
    /// </summary>
    public class BTSequence : BTNode
    {
        private readonly List<BTNode> _children = new List<BTNode>();

        public BTSequence(params BTNode[] children)
        {
            _children.AddRange(children);
        }

        public override BTStatus Tick(AIBlackboard bb)
        {
            foreach (var child in _children)
            {
                var status = child.Tick(bb);
                if (status != BTStatus.Success)
                {
                    return status;
                }
            }
            return BTStatus.Success;
        }
    }

    /// <summary>
    /// 条件判定叶子节点
    /// </summary>
    public class BTCondition : BTNode
    {
        private readonly Func<AIBlackboard, bool> _predicate;

        public BTCondition(Func<AIBlackboard, bool> predicate)
        {
            _predicate = predicate ?? throw new ArgumentNullException(nameof(predicate));
        }

        public override BTStatus Tick(AIBlackboard bb)
        {
            return _predicate(bb) ? BTStatus.Success : BTStatus.Failure;
        }
    }

    /// <summary>
    /// 动作执行叶子节点
    /// </summary>
    public class BTAction : BTNode
    {
        private readonly Func<AIBlackboard, BTStatus> _action;

        public BTAction(Func<AIBlackboard, BTStatus> action)
        {
            _action = action ?? throw new ArgumentNullException(nameof(action));
        }

        public override BTStatus Tick(AIBlackboard bb)
        {
            return _action(bb);
        }
    }

    /// <summary>
    /// 洪荒大能/BOSS行为树工厂
    /// </summary>
    public static class BossAITreeFactory
    {
        public static BTNode CreateGrandmasterTree()
        {
            // 顶层 Selector:
            // 1. 保命序列: 血量 < 25% 且有保命符箓 -> 祭起保命法宝护体 + 御风拉开距离
            // 2. 斩杀序列: 玩家处于破韧虚弱状态 -> 释放大神通【三昧真火+飞剑连诛】
            // 3. 追击序列: 距离 > 12米 -> 御空突进拉近
            // 4. 常规战术: 普攻施法压制
            return new BTSelector(
                // 1. 保命序列
                new BTSequence(
                    new BTCondition(bb => bb.SelfHpPercent < 0.25 && bb.HasEmergencyTalismans),
                    new BTAction(bb =>
                    {
                        bb.HasEmergencyTalismans = false;
                        bb.LastExecutedSkill = "【九曲遁甲符·保命金光罩】";
                        return BTStatus.Success;
                    })
                ),
                // 2. 斩杀破韧序列
                new BTSequence(
                    new BTCondition(bb => bb.IsPlayerBroken),
                    new BTAction(bb =>
                    {
                        bb.LastExecutedSkill = "【三昧真火·太乙玄门剑连携斩杀】";
                        return BTStatus.Success;
                    })
                ),
                // 3. 远距离追击
                new BTSequence(
                    new BTCondition(bb => bb.DistanceToPlayer > 12.0),
                    new BTAction(bb =>
                    {
                        bb.DistanceToPlayer = 5.0; // 突进拉近
                        bb.LastExecutedSkill = "【咫尺天涯·缩地成寸突进】";
                        return BTStatus.Success;
                    })
                ),
                // 4. 默认压制
                new BTAction(bb =>
                {
                    bb.LastExecutedSkill = "【五行天雷印·普通法术压制】";
                    return BTStatus.Success;
                })
            );
        }
    }
}
