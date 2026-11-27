---
item_ids:
  - ae2thing:part_universal_import
  - ae2thing:part_universal_export
  - ae2thing:part_universal_storage
  - ae2thing:ex_io_port
  - ae2thing:fluid_packet_encoder
  - ae2thing:wireless_distributor
navigation:
  title: Buses & Automation
  parent: guide.md
  position: 70
  icon: ae2thing:ex_io_port
categories:
  - ae2things
author: vcwdfca
date: 2026-11-27
updated: 2026-11-27
---

# Buses and Automation

This module covers multi-type buses, bulk transfer and the blocks that move fluids or connect GT machines.

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

## Universal buses

<ItemGrid>
  <ItemIcon id="ae2thing:part_universal_import" />
  <ItemIcon id="ae2thing:part_universal_export" />
  <ItemIcon id="ae2thing:part_universal_storage" />
</ItemGrid>

The Universal buses use AE2's external storage handlers, so one bus can move every stack type available in the pack.

| Bus | Function |
| --- | --- |
| Universal Import | Pulls matching resources from the connected inventory into the network. |
| Universal Export | Pushes configured resources from the network into the connected inventory. |
| Universal Storage | Exposes the connected inventory to the network as storage. |

### Storage bus

- 18 config slots by default, plus 9 per Capacity Card, up to 63.
- Five upgrade slots.
- Supports priority, access mode, extraction mode, fuzzy mode, ore filter and sticky mode.
- Config slots accept any AE stack, so one bus can expose a mixed inventory.

### Import and export buses

- An empty import config imports all supported types; filled slots restrict it.
- The export bus only pushes what is configured.
- Both support Speed, Super Speed and Superluminal Speed Cards, plus the usual ore filter and redstone controls.

### Configuring

Drag items, fluids or bookmarks from NEI into the bus config. Dropping a whole bookmark group fills as many slots as the bus currently has; skipped entries are reported in chat. Capacity Cards unlock more slots.

Fluids need AE2FC, essentia needs Thaumic Energistics, and mana needs Applied Botanics. A type with no registered provider is skipped.

<RecipeFor id="ae2thing:part_universal_storage" fallbackText="This part is not registered in the current pack." />

> [!NOTE]
> If your pack supplies its own universal bus replacement, AE2Things may skip registering these parts to avoid duplicates. Check the pack's replacement mod if the items are missing.

## ME Extended IO Port

<ItemImage id="ae2thing:ex_io_port" label="right" />

A bulk version of the <ItemLink id="appliedenergistics2:tile.BlockIOPort" showIcon="left" />. It snapshots the cell contents and transfers a configurable budget per operation instead of one item at a time.

- `exIOPortTransferContentsRate` controls the transfer rate (default 256); Speed and Super Speed Cards increase it.
- Keeps the normal IO Port modes: fill or empty, and full or half destination behaviour.
- A Universal Infinity Cell in the port can move items, fluids, essentia and mana in one pass.
- Uses a channel and shows an active power state.

<RecipeFor id="ae2thing:ex_io_port" />

## ME Fluid Packet Encoder

<ItemImage id="ae2thing:fluid_packet_encoder" label="right" />

Turns network fluid into AE2FC fluid packets. Select the fluid and the amount represented by one packet in the GUI, then extract the packet item with a pipe or item transport. Each extracted packet consumes that amount from ME fluid storage.

- Must be connected to an ME network that contains the fluid.
- Uses a channel and a small amount of idle power.
- Requires AE2FC.
- Useful when a machine accepts AE2FC packets instead of a fluid handler.

<RecipeFor id="ae2thing:fluid_packet_encoder" fallbackText="Requires AE2FC." />

## ME Wireless Distributor

<ItemImage id="ae2thing:wireless_distributor" label="right" />

Connects GT ME hatches without cables. It is a dense-channel block and keeps up to 40 wireless connections.

- Right-click toggles the scan area between 1×1 and 3×3 chunks.
- Sneak and right-click starts a scan for nearby GT ME hatches.
- It only connects to a hatch with a matching AE color, or to an uncolored side.
- WAILA shows the number of channels in use.
- Available with GT5U or legacy GT5.
