---
item_ids:
  - ae2thing:part_universal_import
  - ae2thing:part_universal_export
  - ae2thing:part_universal_storage
  - ae2thing:ex_io_port
  - ae2thing:fluid_packet_encoder
  - ae2thing:wireless_distributor
navigation:
  title: 总线与自动化
  parent: guide.md
  position: 70
  icon: ae2thing:ex_io_port
categories:
  - ae2things
author: vcwdfca
date: 2026-11-27
updated: 2026-11-27
---

# 总线与自动化

本模块介绍多类型总线、批量传输，以及搬运流体和连接 GT 机器的方块。

<Column>
  <Row>
    <ItemImage id="ae2thing:part_universal_import" scale="4" />
    <ItemImage id="ae2thing:part_universal_export" scale="4" />
    <ItemImage id="ae2thing:part_universal_storage" scale="4" />
  </Row>
  <Row>
    <ItemImage id="ae2thing:ex_io_port" scale="4" />
    <ItemImage id="ae2thing:fluid_packet_encoder" scale="4" />
    <ItemImage id="ae2thing:wireless_distributor" scale="4" />
  </Row>
</Column>

## 通用总线

<ItemGrid>
  <ItemIcon id="ae2thing:part_universal_import" />
  <ItemIcon id="ae2thing:part_universal_export" />
  <ItemIcon id="ae2thing:part_universal_storage" />
</ItemGrid>

通用总线使用 AE2 的外部存储处理器，因此一条总线就能处理整合包中所有可用的 AE 堆栈类型。

| 总线 | 功能 |
| --- | --- |
| 通用输入总线 | 把连接库存中的匹配资源拉进网络。 |
| 通用输出总线 | 把网络中的指定资源推到连接库存。 |
| 通用存储总线 | 把连接库存作为存储暴露给网络。 |

### 存储总线

- 默认 18 个配置槽，每张容量卡增加 9 个，最多 63 个。
- 5 个升级槽。
- 支持优先级、访问模式、提取模式、模糊模式、矿物词典过滤和粘性模式。
- 配置槽接受任意 AE 堆栈，一条总线就能暴露混合库存。

### 输入和输出总线

- 输入总线配置为空时输入所有支持的类型；填入配置后只输入指定资源。
- 输出总线只输出配置的资源。
- 两者都支持加速卡、超级加速卡和超光速加速卡，以及常规的矿物词典过滤和红石控制。

### 填写配置

从 NEI 拖入物品、流体或书签。拖入整个书签组时，总线会用当前可用槽位尽量填充；放不下的条目会在聊天栏提示。安装容量卡可以解锁更多槽位。

流体需要 AE2FC，源质需要神秘能源学，魔力需要应用植物学。没有注册提供者的类型会被跳过。

<RecipeFor id="ae2thing:part_universal_storage" fallbackText="当前整合包没有注册该部件。" />

> [!NOTE]
> 如果整合包用其他模组提供了通用总线的替代品，AE2Things 可能跳过注册自己的通用总线以避免重复物品。找不到这些物品时，请先检查整合包里的替代模组。

## ME扩展IO端口

<ItemImage id="ae2thing:ex_io_port" label="right" />

<ItemLink id="appliedenergistics2:tile.BlockIOPort" showIcon="left" /> 的批量版本。它先对元件内容做快照，然后按可配置的预算批量传输，而不是每次只搬一个物品。

- `exIOPortTransferContentsRate` 控制传输速率，默认 256；加速卡和超级加速卡会继续提高。
- 保留普通 IO 端口的模式：填充或清空，以及目标“满”或“半满”的行为。
- 放入 ME无限通用存储单元后，一次可以同时搬运物品、流体、源质和魔力。
- 占用一个频道，并显示供电状态。

<RecipeFor id="ae2thing:ex_io_port" />

## ME流体封包编码器

<ItemImage id="ae2thing:fluid_packet_encoder" label="right" />

把网络中的流体编码成 AE2FC 流体封包。在界面中选择流体和“每个封包代表的量”，然后用管道或其他物品运输方式抽出封包。每抽出一个封包，就会从 ME 流体存储中扣除对应数量。

- 必须连接 ME 网络，且网络中存有该流体。
- 占用一个频道，并有一定空闲耗电。
- 需要 AE2FC。
- 适用于只接受 AE2FC 封包、而不接受流体处理器的机器。

<RecipeFor id="ae2thing:fluid_packet_encoder" fallbackText="需要 AE2FC。" />

## ME无线网络分配器

<ItemImage id="ae2thing:wireless_distributor" label="right" />

让 GT ME 仓室无需线缆即可接入网络。它是密集频道方块，最多维持 40 条无线连接。

- 右击在 1×1 和 3×3 区块扫描范围之间切换。
- 潜行右击开始扫描附近的 GT ME 仓室。
- 只能连接 AE 颜色匹配的仓室，或一端为未染色的情况。
- WAILA 会显示当前已使用的频道数。
- 安装 GT5U 或旧版 GT5 时可用。
