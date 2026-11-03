using System;

namespace HongHuang.Core.Input
{
    /// <summary>
    /// 虚拟输入状态快照 (适合每帧无GC传递给战斗系统)
    /// </summary>
    public struct InputStateSnapshot
    {
        public double MoveHorizontal;  // -1.0 ~ +1.0
        public double MoveVertical;    // -1.0 ~ +1.0
        public bool IsMoving => Math.Abs(MoveHorizontal) > 0.05 || Math.Abs(MoveVertical) > 0.05;

        public bool AttackPressed;     // 普通攻伐
        public bool DodgePressed;      // 极限闪避 (翻滚/御风)
        public int SkillSlotPressed;   // 0: 无, 1: 技能一, 2: 技能二, 3: 技能三
        public bool ArtifactPressed;   // 激发本命法宝专属神威
    }

    /// <summary>
    /// 移动端触控输入管理器 (适配安卓双摇杆与触控按键)
    /// </summary>
    public class TouchInputController
    {
        private InputStateSnapshot _currentSnapshot;

        public InputStateSnapshot CurrentSnapshot => _currentSnapshot;

        /// <summary>
        /// 模拟/触控摇杆方向向量更新
        /// </summary>
        public void SetJoystickVector(double x, double y)
        {
            // 归一化限制在单位圆内
            double length = Math.Sqrt(x * x + y * y);
            if (length > 1.0)
            {
                _currentSnapshot.MoveHorizontal = x / length;
                _currentSnapshot.MoveVertical = y / length;
            }
            else
            {
                _currentSnapshot.MoveHorizontal = x;
                _currentSnapshot.MoveVertical = y;
            }
        }

        public void TriggerAttack() => _currentSnapshot.AttackPressed = true;
        public void TriggerDodge() => _currentSnapshot.DodgePressed = true;
        public void TriggerSkill(int slot) => _currentSnapshot.SkillSlotPressed = slot;
        public void TriggerArtifact() => _currentSnapshot.ArtifactPressed = true;

        /// <summary>
        /// 每帧物理或逻辑循环结束重置瞬时触发量
        /// </summary>
        public void ResetFrameTriggers()
        {
            _currentSnapshot.AttackPressed = false;
            _currentSnapshot.DodgePressed = false;
            _currentSnapshot.SkillSlotPressed = 0;
            _currentSnapshot.ArtifactPressed = false;
        }
    }
}
