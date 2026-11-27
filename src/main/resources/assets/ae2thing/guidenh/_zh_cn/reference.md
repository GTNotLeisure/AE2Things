---
navigation:
  title: 旧版与参考
  parent: guide.md
  position: 30
  icon: ae2thing:crafting_debug_card
categories:
  - ae2things
author: vcwdfca
date: 2026-11-27
updated: 2026-11-27
---

# 旧版与参考

本页收集不属于主要功能模块的内容：旧版神秘时代、配置选项和常见问题。

<Column>
  <Row>
    <ItemImage id="ae2thing:part_infusion_pattern_terminal" scale="4" />
    <ItemImage id="ae2thing:infusion_interface" scale="4" />
    <ItemImage id="ae2thing:part_thaumatorium_interface" scale="4" />
    <ItemImage id="ae2thing:essentia_discretizer" scale="4" />
    <ItemImage id="ae2thing:phial" scale="4" />
  </Row>
</Column>

## 旧版神秘时代内容

> [!WARNING]
> 在 AE2 1080 及更新版本（包括 GTNH 2.9）中，这些方块已弃用，配方也已禁用。物品可能仍存在于旧存档中，但不要围绕它们搭建新的自动化。

| 物品 | 原本用途 |
| --- | --- |
| <ItemImage id="ae2thing:part_infusion_pattern_terminal" label="right" /> | 编码注魔、坩埚和蒸馏样板，并可下单源质。 |
| <ItemImage id="ae2thing:infusion_interface" label="right" /> | 注魔侧接口。 |
| <ItemImage id="ae2thing:part_thaumatorium_interface" label="right" /> | 炼金炉接口。 |
| <ItemImage id="ae2thing:essentia_discretizer" label="right" /> | 把源质处理转换成 ME 网络可用的形式。 |
| <ItemImage id="ae2thing:phial" label="right" /> | 旧版配方使用的源质瓶。 |

在较旧的 AE2 版本中，这些方块可以自动化注魔、坩埚和炼金炉流程。当前 GTNH 请使用现代神秘能源学或 GTNH 原生源质自动化。

## 配置

主配置文件位于 `config/ae2thing/ae2thing.cfg`。

| 选项 | 默认值 | 作用 |
| --- | ---: | --- |
| `cellLink` | true | 启用无限存储单元的链接界面和 UUID 共享。 |
| `magnetRange` | 32 | 背包终端磁铁范围，8–64 格。 |
| `exIOPortTransferContentsRate` | 256 | ME扩展IO端口的传输速率。 |
| `multiPagesSupport` | 2 | 二合一接口终端的加工样板页数，2–64。 |
| `modernInteraction` | true | 启用终端中的点击填充/抽空容器交互。 |
| `defaultContainers` | `fluid=IC2:itemCellEmpty@0` | 按堆栈类型指定首选空容器。 |
| `craftingHistorySize` | 200 | 保留的合成历史条数，100–300。 |
| `terminalBaubles` | true | 允许受支持的终端放入任意饰品槽。 |
| `backpackTerminalAddTicSupport` | false | 允许背包终端加工匠魂工具。 |
| `enhancedPins` | true | 启用原生终端增强置顶。 |
| `scrollPlayerPins` | false | 玩家置顶随库存滚动，而不是固定区域。 |
| `wirelessConnectorTerminalInfinityConnectionRange` | true | 取消无线连接终端的距离限制。 |
| `replaceHotIngot` | true | 转移加工配方时，把炽热锭产物替换为冷却后的锭。 |

## NEI 选项

相关选项位于 **AE2 Thing** 和 **AE2 Thing → 二合一接口终端** 下。

- 自动上传样板、增强配方搜索和搜索调试
- 配方名填充，包括电路和不消耗原料选项
- 终端历史、合成固定栏和库存状态
- NEI 快速下单和 AE 终端内的 NEI 自动合成
- 通用终端模式

## 常见问题

**无限存储单元为什么耗电这么高？**
物品型和流体型单元每个固定消耗 2,000 AE/t。检查驱动器里装了多少个，并把长期不用的单元取出来；通用单元请直接查看驱动器的实际空闲耗电。

**为什么两个元件无法链接？**
链接槽只接受同类型的单元。目标必须是空单元、没有 UUID，或已经带有相同 UUID。`cellLink` 也必须启用。

**为什么自动上传没有放入样板？**
自动上传只存在于 ME无线二合一接口终端。请在 NEI 设置中开启，确保恰好只有一个接口能运行该配方，并准备空白样板。重复或槽位已满会在聊天栏提示。

**为什么 `@` 增强搜索没有结果？**
先在 NEI 设置中开启 **增强配方搜索**，再确认配方池名称。调试选项会把解析到的配方池和候选接口打印到客户端日志。

**为什么神秘时代的方块无法合成？**
在 AE2 1080 及以上版本中，这些方块已弃用且配方被禁用，只作为旧版本遗留内容保留；物品可能仍存在于旧存档中。

**应用植物学在哪里？**
魔力是独立模块，请继续阅读 [应用植物学](appbot.md)。

## 相关页面

- [AE2Things 总览](guide.md)
- [存储](storage.md)
- [终端](terminals.md)
- [总线与自动化](automation.md)
- [应用植物学](appbot.md)
