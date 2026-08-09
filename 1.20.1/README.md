# BF煎饼猫 - 地狱门预览

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-blue.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.15.11-9d4edd.svg)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-%3E%3D17-F89820.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![GitHub](https://img.shields.io/badge/GitHub-BFTYSB%2Fportal--preview-181717.svg)](https://github.com/BFTYSB/portal-preview)

> 在地狱扫描传送门门框，返回主世界后实时预览对应坐标的搭建位置，告别手动计算坐标！

**BF煎饼猫** 是一款面向 Fabric 客户端的辅助 Mod。它能在地狱自动识别传送门门框并计算主世界对应坐标，回到主世界后以半透明幽灵方块 + 轮廓线的方式，实时标出你该在哪里搭建传送门。

---

## ✨ 特性

- 🧭 在地狱扫描门框，HUD 显示地狱坐标及对应主世界坐标
- 👻 主世界实时预览传送门搭建位置（半透明幽灵方块 + 轮廓线）
- ⌨️ 可自定义的按键绑定（基于 Cloth Config）
- 🖥️ 集成 ModMenu，图形化配置界面

---

## 📥 安装

### 环境要求

- **Minecraft**：1.20.1
- **Java**：JDK 17+
- **Fabric Loader**：0.15.11+

### 依赖 Mod

| Mod | 版本 | 说明 |
|-----|------|------|
| Fabric API | 0.92.2+1.20.1 | 必需 |
| Cloth Config | 11.1.118 | 配置界面必需 |
| ModMenu | 7.2.2 | 可选（推荐，可在模组菜单中配置） |

### 安装步骤

1. 安装 [Fabric Loader](https://fabricmc.net/use/) 0.15.11+
2. 将 `portal-preview-2.0.0.jar` 放入 `.minecraft/mods/` 文件夹
3. 安装 Fabric API 0.92.2+1.20.1
4. 安装 Cloth Config 11.1.118（配置界面必需）
5. （可选）安装 ModMenu 7.2.2
6. 启动游戏，享受地狱门预览功能！

---

## 🎮 使用指南

### 按键绑定

| 按键                | 功能 | 使用场景 |
|---------------------|------|----------|
| **P**               | 开启/关闭 HUD 显示 | 在地狱中使用 |
| **[**（左方括号）   | 切换传送门朝向 | 地狱或主世界 |
| **R**               | 开启/关闭预览渲染 | 主世界中查看搭建位置 |
| **K**（默认未绑定） | 打开配置界面 | 任意场景 |

### 使用流程

1. **进入地狱**，靠近地狱门框
2. 按 **P 键** 扫描门框，HUD 会显示地狱坐标和对应的主世界坐标
3. **返回主世界**，按 **R 键** 开启预览渲染
4. 根据幽灵方块提示搭建传送门
5. 全部 6 个内部方块搭建完成后，预览自动关闭

---

## 🔧 配置

安装 ModMenu 后，可在游戏内 **模组菜单 → BF煎饼猫 → 配置** 中调整按键绑定、预览颜色等选项（基于 Cloth Config）。

---

## 📮 反馈

如遇到 Bug 或有功能建议，欢迎通过 [GitHub Issues](https://github.com/BFTYSB/portal-preview/issues) 反馈。

---

## 📜 许可

本项目基于 [MIT License](LICENSE) 开源。

---

## 🙏 致谢

- 感谢 Fabric 团队提供的优秀 Mod 加载器和 API
- 感谢 Cloth Config 和 ModMenu 的开发者
- 感谢所有使用本 Mod 的玩家！

---

**BF煎饼猫 - 让地狱门搭建更轻松！**
