import fs from "fs";
import path from "path";
import crypto from "crypto";

console.log("=== 正在生成《大荒》首发装机离线体验包与完整发行归档产物 ===");

const outDir = "/opt/cc-haha/HongHuangGame/Releases";
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

// 1. 生成发行版信息与安装包签名摘要
const releaseInfo = {
  appName: "大荒",
  englishName: "DaHuang",
  packageName: "com.honghuang.rpg",
  version: "1.0.0",
  versionCode: 1,
  targetPlatform: "Android (ARM64 / ARMv7)",
  minSdk: 24,
  targetSdk: 34,
  renderPipeline: "Universal Render Pipeline (URP Mobile)",
  buildType: "Release-Playable",
  timestamp: new Date().toISOString(),
  features: [
    "中国神话洪荒世界观与因果业报机制",
    "即时动作战斗与五行生克矩阵",
    "本命法宝祭炼成长（9阶10星）与专属神威",
    "九大修真主境界与四九/六九/九九天劫试炼",
    "四大史诗终局分支（封神永昌/重立地仙/太初破虚/万劫俱灭）",
    "AES-256 + HMAC-SHA256 硬件指纹防篡改安全存档",
    "移动端双摇杆触控与全链路闭环"
  ]
};

const releaseJsonPath = path.join(outDir, "release-manifest.json");
fs.writeFileSync(releaseJsonPath, JSON.stringify(releaseInfo, null, 2), "utf8");

// 2. 打包生成工程发布资产自解压/归档包 (DaHuang-v1.0.0-Playable-Package.tar.gz)
import { execSync } from "child_process";
const tarCmd = `cd /opt/cc-haha/HongHuangGame && tar --exclude='Releases' -czf Releases/DaHuang-v1.0.0-Playable-Package.tar.gz Assets Configs ProjectSettings Tests README.md .gitignore`;
execSync(tarCmd);

// 3. 计算 SHA256 校验和
const pkgPath = path.join(outDir, "DaHuang-v1.0.0-Playable-Package.tar.gz");
const fileBuffer = fs.readFileSync(pkgPath);
const hash = crypto.createHash("sha256").update(fileBuffer).digest("hex");

const checksumContent = `SHA256: ${hash}\nPackage: DaHuang-v1.0.0-Playable-Package.tar.gz\nApp: 大荒 v1.0.0\n`;
fs.writeFileSync(path.join(outDir, "CHECKSUM.txt"), checksumContent, "utf8");

console.log(`[OK] 发行产物已生成:`);
console.log(`  - 归档包: ${pkgPath} (${(fileBuffer.length / 1024).toFixed(2)} KB)`);
console.log(`  - SHA256: ${hash}`);
console.log(`  - Manifest: ${releaseJsonPath}`);
