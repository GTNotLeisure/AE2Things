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
  title: Applied Botanics
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

# Applied Botanics

**Applied Botanics** (also called **AppBot**) is the Botania module bundled with AE2Things. It stores Botania mana as a native AE stack type, so mana can live in cells and the ME network instead of being piped as a liquid.

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

- Mod id: `ae2thing_ab`
- Requires **Botania**
- Requires at least one mana storage cell, or a creative mana cell, before mana can actually be stored

> [!IMPORTANT]
> Mana is **not a Forge fluid**. Fluid pipes, tanks and fluid storage buses cannot move it. Older Applied Botanics versions used a liquid-mana representation; the 29x module does not.

## How it works

```text
Botania pool / collector
        |
        v
[ME network + Mana Cells] <--> [Fluix Mana Pool] <--> Botania devices
```

A mana cell stores the actual mana. A Fluix Mana Pool is a bridge: Botania devices can fill or drain it, and every unit is read from or written to the network. The import and export buses that move mana between a Botania receiver and the network are covered on the [next page](appbot-automation.md).

## Mana cells

<ItemGrid>
  <ItemIcon id="ae2thing_ab:mana_cell_1k" />
  <ItemIcon id="ae2thing_ab:mana_cell_4k" />
  <ItemIcon id="ae2thing_ab:mana_cell_16k" />
  <ItemIcon id="ae2thing_ab:mana_cell_64k" />
  <ItemIcon id="ae2thing_ab:mana_cell_256k" />
</ItemGrid>

Mana cells work in an <ItemLink id="appliedenergistics2:tile.BlockDrive" showIcon="left" /> or <ItemLink id="appliedenergistics2:tile.BlockChest" showIcon="left" /> and store exactly one resource: mana. Each has five upgrade slots.

| Cell | Capacity in mana | Equivalent pools | Idle drain |
| --- | ---: | ---: | ---: |
| 1k Mana Storage Cell | 500,000 | 0.5 | 0.5 AE/t |
| 4k Mana Storage Cell | 2,000,000 | 2 | 1.0 AE/t |
| 16k Mana Storage Cell | 8,000,000 | 8 | 1.5 AE/t |
| 64k Mana Storage Cell | 32,000,000 | 32 | 2.0 AE/t |
| 256k Mana Storage Cell | 128,000,000 | 128 | 2.5 AE/t |

One Botania mana pool holds 1,000,000 mana, which is why the tooltip and terminal use **pools** as the unit. Capacity is fixed per tier; Capacity Cards do not increase it.

### Housing and recipes

<ItemImage id="ae2thing_ab:mana_cell_housing" label="right" />

The housing is the common body for all five tiers. Combine it with the matching AE2 cell component.

<Column>
  <Row>
    <RecipeFor id="ae2thing_ab:mana_cell_housing" />
    <RecipeFor id="ae2thing_ab:mana_cell_1k" />
  </Row>
</Column>

## Creative Mana Cell

<ItemImage id="ae2thing_ab:creative_mana_cell" label="right" />

An infinite mana source with no idle drain. Extracting from it always succeeds. It is intended for testing and creative play; the current source tree does not register a survival recipe, though a pack may add one.

## Fluix Mana Pool

<BlockImage id="ae2thing_ab:fluix_mana_pool" scale="2" />

A Botania mana pool whose internal mana is the ME network's mana storage. For Botania devices it behaves like a normal pool:

- mana spreaders can fill it;
- sparks, collectors and mana consumers can interact with it;
- the wand HUD shows the stored amount;
- a comparator reads its fill level.

It needs a channel and consumes 1 AE/t. Its capacity is the network's total mana capacity, not a fixed 1,000,000. With a 256k cell attached, it can show 128 pools of capacity.

> [!WARNING]
> The pool does not create storage. If the network has no mana cell or creative mana cell, mana inserted into the pool has nowhere to go. Place at least one mana cell before connecting it to spreaders or collectors.

<RecipeFor id="ae2thing_ab:fluix_mana_pool" fallbackText="No Fluix Mana Pool recipe is registered in this pack." />

## Continue

- [Applied Botanics: Buses and Automation](appbot-automation.md) — import/export buses, the Mana Marker and example layouts.
- [AE2Things overview](guide.md)
