---
item_ids:
  - ae2thing_ab:mana_cell_housing
  - ae2thing_ab:mana_cell_1k
  - ae2thing_ab:mana_cell_4k
  - ae2thing_ab:mana_cell_16k
  - ae2thing_ab:mana_cell_64k
  - ae2thing_ab:mana_cell_256k
  - ae2thing_ab:creative_mana_cell
  - ae2thing_ab:fluix_mana_pool
navigation:
  title: 应用植物学
  parent: guide.md
  position: 50
  recommend: 5
  icon: ae2thing_ab:fluix_mana_pool
  required_mod: Botania
categories:
  - applied-botanics
author: vcwdfca
date: 2026-11-27
updated: 2026-11-27
---

# 应用植物学

**应用植物学（Applied Botanics，AppBot）** 是 AE2Things 内置的植物魔法模块。它把植物魔法的魔力注册为 AE 的原生堆栈类型，使魔力可以存放在元件和 ME 网络中，而不必用液体管道搬运。

<Column>
  <Row>
    <ItemImage id="ae2thing_ab:mana_cell_1k" scale="4" />
    <ItemImage id="ae2thing_ab:mana_cell_4k" scale="4" />
    <ItemImage id="ae2thing_ab:mana_cell_16k" scale="4" />
    <ItemImage id="ae2thing_ab:mana_cell_64k" scale="4" />
    <ItemImage id="ae2thing_ab:mana_cell_256k" scale="4" />
  </Row>
  <Row>
    <ItemImage id="ae2thing_ab:mana_cell_housing" scale="4" />
    <ItemImage id="ae2thing_ab:creative_mana_cell" scale="4" />
    <ItemImage id="ae2thing_ab:fluix_mana_pool" scale="4" />
  </Row>
</Column>

- 模组 id：`ae2thing_ab`
- 需要 **植物魔法（Botania）**
- 要真正存住魔力，网络中至少要有一个魔力存储元件或创造魔力元件

> [!IMPORTANT]
> 魔力**不是 Forge 流体**。流体管道、储罐和流体存储总线都无法搬运它。旧版应用植物学曾用液体表示魔力，29x 模块不再如此。

## 工作方式

```text
植物魔法魔力池 / 收集器
        |
        v
[ME网络 + 魔力存储元件] <--> [福鲁伊克斯魔力池] <--> 植物魔法设备
```

魔力存储元件负责真正保存魔力。福鲁伊克斯魔力池是桥梁：植物魔法设备可以往它里面注入或抽取魔力，而每一次读写都会落到网络上。负责在植物魔法接收器和网络之间搬运魔力的输入/输出总线在[下一页](appbot-automation.md)。

## 魔力存储元件

<ItemGrid>
  <ItemIcon id="ae2thing_ab:mana_cell_1k" />
  <ItemIcon id="ae2thing_ab:mana_cell_4k" />
  <ItemIcon id="ae2thing_ab:mana_cell_16k" />
  <ItemIcon id="ae2thing_ab:mana_cell_64k" />
  <ItemIcon id="ae2thing_ab:mana_cell_256k" />
</ItemGrid>

魔力存储元件可以放进 <ItemLink id="appliedenergistics2:tile.BlockDrive" showIcon="left" /> 或 <ItemLink id="appliedenergistics2:tile.BlockChest" showIcon="left" />，只存储一种资源：魔力。每个元件有 5 个升级槽。

| 元件 | 魔力容量 | 折合魔力池 | 空闲耗能 |
| --- | ---: | ---: | ---: |
| 1k 魔力存储元件 | 500,000 | 0.5 | 0.5 AE/t |
| 4k 魔力存储元件 | 2,000,000 | 2 | 1.0 AE/t |
| 16k 魔力存储元件 | 8,000,000 | 8 | 1.5 AE/t |
| 64k 魔力存储元件 | 32,000,000 | 32 | 2.0 AE/t |
| 256k 魔力存储元件 | 128,000,000 | 128 | 2.5 AE/t |

一个植物魔法魔力池的容量是 1,000,000 魔力，所以提示框和终端会用**池**作为单位。每个等级的容量固定，容量卡不会增加它。

### 外壳与配方

<ItemImage id="ae2thing_ab:mana_cell_housing" label="right" />

魔力元件外壳是五个等级共用的主体。把它与对应的 AE2 元件核心组合。

<Column>
  <Row>
    <RecipeFor id="ae2thing_ab:mana_cell_housing" />
    <RecipeFor id="ae2thing_ab:mana_cell_1k" />
  </Row>
</Column>

## 创造魔力元件

<ItemImage id="ae2thing_ab:creative_mana_cell" label="right" />

无限的魔力来源，没有空闲耗能，从中提取魔力总是成功。它用于测试和创造模式游玩；当前源码没有为它注册生存配方，但整合包可能会自行添加。

## 福鲁伊克斯魔力池

<BlockImage id="ae2thing_ab:fluix_mana_pool" scale="2" />

一个内部魔力直接来自 ME 网络魔力存储的植物魔法魔力池。对植物魔法设备来说，它的行为与普通魔力池一致：

- 魔力发射器可以往它里面注入魔力；
- 魔力火花、收集器和消耗魔力的设备都能与之交互；
- 法杖 HUD 会显示当前存储量；
- 比较器可以读取它的填充程度。

它需要占用一个频道，并消耗 1 AE/t。它的容量等于整个网络的魔力总容量，而不是固定的 1,000,000。接入 256k 魔力存储元件后，它可以显示 128 池的容量。

> [!WARNING]
> 魔力池本身不提供存储。如果网络里没有魔力存储元件或创造魔力元件，注进魔力池的魔力就无处可去。把魔力池接到发射器或收集器之前，请先放好至少一个魔力存储元件。

<RecipeFor id="ae2thing_ab:fluix_mana_pool" fallbackText="当前整合包没有注册福鲁伊克斯魔力池的配方。" />

## 继续阅读

- [应用植物学：总线与自动化](appbot-automation.md)——魔力输入/输出总线、魔力标记和常见布局。
- [AE2Things 总览](guide.md)
