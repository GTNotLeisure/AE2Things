---
navigation:
  title: AE2Things
  position: 100
  recommend: 10
  icon: ae2thing:infinity_cell
categories:
  - ae2things
author: vcwdfca
date: 2026-11-27
updated: 2026-11-27
---

# AE2Things

AE2Things 是 **AE2-GTNH** 的附属模组，为 ME 网络增加大容量存储、远程终端和多类型自动化。本指南对应面向 GTNH 2.9 的 29x 分支。

<Column>
  <Row>
    <ItemImage id="ae2thing:infinity_cell" scale="4" />
    <ItemImage id="ae2thing:backpack_terminal" scale="4" />
    <ItemImage id="ae2thing:ex_io_port" scale="4" />
    <ItemImage id="ae2thing:pattern_modifier" scale="4" />
    <ItemImage id="ae2thing:crafting_debug_card" scale="4" />
  </Row>
</Column>

为了避免单个页面过长，本指南按模块拆分成多个短页面。

## 模块

| 模块 | 内容 | 页面 |
| --- | --- | --- |
| 存储 | 无限存储单元、元件链接、创造元件 | [存储](storage.md) |
| 终端 | 背包终端、无线二合一接口终端、终端增强 | [终端](terminals.md) |
| 总线与自动化 | 通用总线、扩展IO端口、流体封包编码器、无线网络分配器 | [总线与自动化](automation.md) |
| 无线与工具 | 无线连接终端、样板修改器、合成DEBUG卡 | [无线与工具](wireless-tools.md) |
| 应用植物学 | 魔力元件、福鲁伊克斯魔力池 | [应用植物学](appbot.md) |
| 应用植物学自动化 | 魔力总线、魔力标记和自动化布局 | [应用植物学：自动化](appbot-automation.md) |
| 旧版与参考 | 旧版神秘时代内容、配置、NEI 选项、排错 | [旧版与参考](reference.md) |

## 建议阅读顺序

1. 先读 [存储](storage.md)，了解无限存储单元和链接机制。
2. 需要背包终端或二合一接口终端时读 [终端](terminals.md)。
3. 需要多类型总线和批量传输时读 [总线与自动化](automation.md)。
4. 只有安装植物魔法时才需要读 [应用植物学](appbot.md)。

## 29x 说明

> [!NOTE]
> 部分功能依赖可选模组：流体需要 AE2FC，源质需要神秘能源学，无线连接终端需要 AE2Stuff，无线网络分配器需要 GT5U，应用植物学需要植物魔法。各模块页面会分别列出依赖。

> [!WARNING]
> 在 AE2 1080 及更新版本（包括 GTNH 2.9）中，旧版神秘时代自动化方块已弃用，配方也已禁用。围绕它们搭建自动化前，请先阅读 [旧版与参考](reference.md)。
