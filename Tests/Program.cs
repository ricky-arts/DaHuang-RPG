using System;
using System.IO;
using System.Security.Cryptography;
using HongHuang.Core.EventBus;
using HongHuang.Core.ObjectPool;
using HongHuang.Core.Security;
using HongHuang.Core.Storage;
using HongHuang.Gameplay.Combat;
using HongHuang.Gameplay.Realm;

namespace HongHuang.Tests
{
    // 测试用简单事件
    public struct TestBreakthroughEvent : IGameEvent
    {
        public RealmTier NewRealm;
        public int MinorStage;
    }

    class Program
    {
        static int Main(string[] args)
        {
            Console.WriteLine("==================================================");
            Console.WriteLine("=== 洪荒 RPG 核心引擎自动化单元测试与自验证 ===");
            Console.WriteLine("==================================================\n");

            bool allPassed = true;

            try
            {
                TestEventBus();
                TestSecurityAndSaveEngine();
                TestObjectPool();
                TestElementMatrixAndDamageEngine();
                TestRealmAndTribulationSystem();
            }
            catch (Exception ex)
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine($"[CRITICAL FAILURE] 测试执行崩溃: {ex.Message}\n{ex.StackTrace}");
                Console.ResetColor();
                allPassed = false;
            }

            Console.WriteLine();
            if (allPassed)
            {
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine(">>> 所有核心测试全部通过 (100% PASS)！游戏架构与计算底座稳定无误！<<<");
                Console.ResetColor();
                return 0;
            }
            else
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine(">>> 测试存在失败用例，请修复！<<<");
                Console.ResetColor();
                return 1;
            }
        }

        static void Assert(bool condition, string testName)
        {
            if (condition)
            {
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"[PASS] {testName}");
                Console.ResetColor();
            }
            else
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine($"[FAIL] {testName}");
                Console.ResetColor();
                throw new Exception($"Assertion failed: {testName}");
            }
        }

        static void TestEventBus()
        {
            Console.WriteLine("--- 测试 1: GameEventBus 类型安全解耦通信 ---");
            bool received = false;
            RealmTier receivedRealm = RealmTier.LianQi;

            Action<TestBreakthroughEvent> handler = e =>
            {
                received = true;
                receivedRealm = e.NewRealm;
            };

            GameEventBus.Subscribe(handler);
            GameEventBus.Publish(new TestBreakthroughEvent { NewRealm = RealmTier.JinDan, MinorStage = 1 });

            Assert(received && receivedRealm == RealmTier.JinDan, "事件成功发布并被订阅者准确捕获");

            received = false;
            GameEventBus.Unsubscribe(handler);
            GameEventBus.Publish(new TestBreakthroughEvent { NewRealm = RealmTier.YuanYing, MinorStage = 1 });
            Assert(!received, "取消订阅后事件不再被触发");
        }

        static void TestSecurityAndSaveEngine()
        {
            Console.WriteLine("\n--- 测试 2: 防修改数值与安全加密存档 ---");
            // 1. ObfuscatedInt 掩码测试
            ObfuscatedInt safeInt = 9999;
            Assert(safeInt.Value == 9999, "ObfuscatedInt 正确存储并解密原始值");
            safeInt.Value += 1;
            Assert(safeInt.Value == 10000, "ObfuscatedInt 正确响应自增操作");

            // 2. 加密存档 + HMAC 签名
            var (key, iv) = SecureSaveEngine.DeriveKey("android_device_id_demo_888", "com.honghuang.rpg");
            string testJson = "{\"player\":\"遁去的一\",\"realm\":\"JinDan\",\"stone\":888888}";

            byte[] encrypted = SecureSaveEngine.EncryptWithHmac(testJson, key, iv);
            string decrypted = SecureSaveEngine.DecryptWithHmac(encrypted, key, iv);
            Assert(decrypted == testJson, "加密并解密后的数据与明文完全一致");

            // 3. 模拟外部修改器篡改数据
            encrypted[encrypted.Length - 1] ^= 0xFF; // 翻转最后一个字节
            bool caughtTamper = false;
            try
            {
                SecureSaveEngine.DecryptWithHmac(encrypted, key, iv);
            }
            catch (CryptographicException)
            {
                caughtTamper = true;
            }
            Assert(caughtTamper, "外部非法 Hex 篡改存档被 HMAC 签名机制成功拦截并报错");
        }

        static void TestObjectPool()
        {
            Console.WriteLine("\n--- 测试 3: 通用对象池 Zero-GC 性能 ---");
            int createCount = 0;
            var pool = new SimpleObjectPool<CombatEntity>(
                factory: () => { createCount++; return new CombatEntity(); },
                initialCapacity: 4
            );

            Assert(createCount == 4 && pool.CountInactive == 4, "对象池预热初始化容量正确");

            var entity = pool.Get();
            Assert(pool.CountInactive == 3, "成功取出对象并减少闲置计数");

            pool.Release(entity);
            Assert(pool.CountInactive == 4, "对象回收回池成功，无多余创建");
        }

        static void TestElementMatrixAndDamageEngine()
        {
            Console.WriteLine("\n--- 测试 4: 五行生克矩阵与双曲伤害计算 ---");
            // 金克木
            var (mulMetalWood, adv1, _) = CombatEngine.CalculateElementModifier(ElementType.Metal, ElementType.Wood);
            Assert(mulMetalWood == 1.35 && adv1, "五行克制：金克木正确增伤 35%");

            // 木被金克
            var (mulWoodMetal, _, dis1) = CombatEngine.CalculateElementModifier(ElementType.Wood, ElementType.Metal);
            Assert(mulWoodMetal == 0.75 && dis1, "五行被克：木被金克正确减伤 25%");

            // 水克火
            var (mulWaterFire, adv2, _) = CombatEngine.CalculateElementModifier(ElementType.Water, ElementType.Fire);
            Assert(mulWaterFire == 1.35 && adv2, "五行克制：水克火正确增伤 35%");

            // 伤害计算实体
            var attacker = new CombatEntity
            {
                Name = "主角",
                Realm = RealmTier.JinDan,
                Element = ElementType.Fire,
                Attack = 1000,
                CritRate = 0.0, // 关暴击便于精算
                PenetrationFlat = 0,
                PenetrationPercent = 0
            };

            var defender = new CombatEntity
            {
                Name = "木灵树妖",
                Realm = RealmTier.JinDan,
                Element = ElementType.Metal, // 火克金
                MaxHp = 10000,
                CurrentHp = 10000,
                Defense = 800, // DefConstant = 800，减伤正好 50%
                Tenacity = 100,
                MaxTenacity = 100,
                IsBroken = false
            };

            var res = CombatEngine.CalculateDamage(attacker, defender, 1.0);
            // 理论基础伤害: 1000 * (1 - 800/(800+800)) = 500
            // 火克金修正: 500 * 1.35 = 675
            // 同境界压制: 1.0 -> 最终伤害应为 675
            Assert(Math.Abs(res.FinalDamage - 675.0) < 0.01, $"双曲防御与五行相克计算精准 (实际结算伤害: {res.FinalDamage})");
            Assert(defender.CurrentHp == 10000 - 675, "防御实体正确扣除相应气血");
        }

        static void TestRealmAndTribulationSystem()
        {
            Console.WriteLine("\n--- 测试 5: 修真九境、因果善恶与天劫机制 ---");
            var rm = new RealmManager(RealmTier.LianQi);
            Assert(rm.CurrentRealm == RealmTier.LianQi && rm.MinorStage == 1, "初始境界为练气一层");

            // 境界压制函数验证
            double highToLow = CombatEngine.CalculateRealmModifier(2); // 金丹打练气 (+2)
            // 1.0 + 0.25*2 + 0.05*4 = 1.70
            Assert(Math.Abs(highToLow - 1.70) < 0.001, $"顺境高打低非线性增伤正确: {highToLow}");

            double lowToHigh = CombatEngine.CalculateRealmModifier(-2); // 练气打金丹 (-2)
            // 1 / (1 + 0.35*2 + 0.08*4) = 1 / (1 + 0.70 + 0.32) = 1 / 2.02 ≈ 0.495
            Assert(lowToHigh < 0.50 && lowToHigh > 0.48, $"低打高逆伐衰减平滑: {lowToHigh}");

            // 因果影响天劫
            rm.KarmaValue = -5000; // 极深业障
            var (_, dmgEvil, nameEvil) = rm.GetTribulationSpec();
            Assert(nameEvil.Contains("九幽寂灭紫霄黑雷"), "极恶业障成功触发紫霄黑雷天劫惩戒");

            rm.KarmaValue = 5000; // 极高功德
            var (_, dmgGood, nameGood) = rm.GetTribulationSpec();
            Assert(nameGood.Contains("功德金莲护体") && dmgGood < dmgEvil, "福缘功德大幅削减雷劫威能，祥云护体");
        }
    }
}
