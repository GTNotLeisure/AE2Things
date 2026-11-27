---
item_ids:
  - ae2thing_ab:part_mana_import
  - ae2thing_ab:part_mana_export
  - ae2thing_ab:mana_marker
navigation:
  title: 应用植物学：自动化
  parent: appbot.md
  position: 40
  icon: ae2thing_ab:part_mana_import
  required_mod: Botania
categories:
  - applied-botanics
author: vcwdfca
date: 2026-11-27
updated: 2026-11-27
---

# 应用植物学：总线与自动化

应用植物学的第二页介绍魔力传输、过滤和工作布局。如果还没有配置魔力元件和福鲁伊克斯魔力池，请先阅读 [应用植物学](appbot.md)。

<Column>
  <Row>
    <ItemImage id="ae2thing_ab:part_mana_import" scale="4" />
    <ItemImage id="ae2thing_ab:part_mana_export" scale="4" />
    <ItemImage id="ae2thing_ab:mana_marker" scale="4" />
  </Row>
</Column>

## ME魔力输入总线与输出总线

<Row>
  <ItemImage id="ae2thing_ab:part_mana_import" label="right" />
  <ItemImage id="ae2thing_ab:part_mana_export" label="right" />
</Row>

两者都是线缆部件。贴在 ME 线缆上，并朝向一个植物魔法魔力接收器。

| 总线 | 功能 |
| --- | --- |
| ME魔力输入总线 | 把连接的接收器中的魔力抽进网络。 |
| ME魔力输出总线 | 把网络中的魔力推入连接的接收器。 |

它们接受任何植物魔法的 `IManaReceiver`，包括魔力池、收集器和其他能接收魔力的方块。总线只搬运魔力，没有物品或流体过滤槽。需要过滤时，请使用带魔力标记的通用存储总线。

### 传输量

未安装升级时，每次操作搬运 1,000 魔力。总线根据积压情况每 5–40 tick 执行一次，因此实际持续速率取决于单次搬运量和执行间隔。

| 安装数量 | 加速卡 | 超级加速卡 |
| ---: | ---: | ---: |
| 0 | ×1 | ×1 |
| 1 | ×8 | ×32 |
| 2 | ×32 | ×512 |
| 3 | ×64 | ×6,144 |
| 4 | ×96 | ×49,152 |

两种倍率可以叠加，但一条总线总共只有 4 个升级槽。红石卡会提供常规的红石控制模式。

<Column>
  <Row>
    <RecipeFor id="ae2thing_ab:part_mana_import" fallbackText="需要 AE2FC。" />
    <RecipeFor id="ae2thing_ab:part_mana_export" fallbackText="需要 AE2FC。" />
  </Row>
</Column>

## 魔力标记

<ItemImage id="ae2thing_ab:mana_marker" label="right" />

配置用“幽灵物品”，不是燃料。从 NEI 把它拖进兼容的 AE 配置槽——最重要的是**通用存储总线**——就能让那条总线识别并暴露魔力。它同时也是 NEI 和终端类型过滤中代表魔力的图标。

魔力输入总线和输出总线不需要魔力标记，因为它们本来就只处理魔力。

## 工作布局

### 用植物魔法给网络充能

1. 把 ME魔力输入总线贴在魔力池或魔力收集器上。
2. 在同一网络的 ME 驱动器里放至少一个魔力存储元件。
3. 总线会把魔力送进元件。安装多个元件时，注意每个元件的空闲耗能。

### 用网络给植物魔法供能

1. 把 ME魔力输出总线贴在目标魔力池或耗魔设备上。
2. 网络中先存有魔力。
3. 只要目标还有空间、网络还有电，总线就会持续输出魔力。

### 把福鲁伊克斯魔力池当作植物魔法接口

1. 用频道把魔力池接入 ME 网络。
2. 在网络中安装魔力存储元件。
3. 让发射器、收集器或魔力火花像对待普通魔力池一样对待它。

这样可以避免为每个魔力池单独拉一条总线，并把魔力集中在一个网络级缓冲区里。

### 把植物魔法魔力池暴露为存储

应用植物学把魔力注册为外部存储类型，因此 ME通用存储总线可以读取和写入普通植物魔法魔力接收器。需要过滤或优先级控制时，这种接法很有用，但它不能替代专用的魔力存储元件。

## 常见问题

**魔力放进魔力池后消失了。**
网络里没有魔力存储。先放一个魔力存储元件或创造魔力元件。

**魔力总线不搬运魔力。**
检查接收器、网络供电和频道，以及导入时网络是否还有剩余魔力容量、导出时网络里是否存有魔力。

**终端里看不到魔力。**
魔力是独立的 AE 堆栈类型。检查终端的类型过滤，并确认应用植物学已安装。

**能用流体管道或储罐吗？**
不能。请使用魔力存储元件、魔力总线或福鲁伊克斯魔力池。
