---
item_ids:
  - ae2thing_ab:part_mana_import
  - ae2thing_ab:part_mana_export
  - ae2thing_ab:mana_marker
navigation:
  title: Applied Botanics: Automation
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

# Applied Botanics: Buses and Automation

The second Applied Botanics page covers mana transfer, filtering and working layouts. Start with [Applied Botanics](appbot.md) if you have not set up mana cells and a Fluix Mana Pool yet.

<Column>
  <Row>
    <ItemImage id="ae2thing_ab:part_mana_import" scale="4" />
    <ItemImage id="ae2thing_ab:part_mana_export" scale="4" />
    <ItemImage id="ae2thing_ab:mana_marker" scale="4" />
  </Row>
</Column>

## ME Mana Import and Export Buses

<Row>
  <ItemImage id="ae2thing_ab:part_mana_import" label="right" />
  <ItemImage id="ae2thing_ab:part_mana_export" label="right" />
</Row>

Both are cable parts. Place one on an ME cable against a Botania mana receiver.

| Bus | Function |
| --- | --- |
| ME Mana Import Bus | Pulls mana from the connected receiver into the network. |
| ME Mana Export Bus | Pushes network mana into the connected receiver. |

They accept any Botania `IManaReceiver`, including mana pools, collectors and other mana-accepting blocks. They always move mana; there is no item or fluid filter. For filtered mana access, use a Universal Storage Bus with a Mana Marker.

### Throughput

Each operation moves 1,000 mana before upgrades. The bus ticks every 5–40 ticks depending on backlog, so the sustained rate depends on the amount per operation and the interval.

| Cards installed | Speed Cards | Super Speed Cards |
| ---: | ---: | ---: |
| 0 | ×1 | ×1 |
| 1 | ×8 | ×32 |
| 2 | ×32 | ×512 |
| 3 | ×64 | ×6,144 |
| 4 | ×96 | ×49,152 |

The multipliers stack, but a bus has only four upgrade slots. A Redstone Card adds the normal redstone modes.

<Column>
  <Row>
    <RecipeFor id="ae2thing_ab:part_mana_import" fallbackText="Requires AE2FC." />
    <RecipeFor id="ae2thing_ab:part_mana_export" fallbackText="Requires AE2FC." />
  </Row>
</Column>

## Mana Marker

<ItemImage id="ae2thing_ab:mana_marker" label="right" />

A configuration ghost, not a fuel. Drag it from NEI into a compatible AE config slot, most importantly the Universal Storage Bus, to make that bus expose mana. It is also the icon used for mana in NEI and terminal filters.

The mana import and export buses do not need a marker; they are already mana-only.

## Working layouts

### Charge the network from Botania

1. Place a Mana Import Bus against a mana pool or collector.
2. Put at least one mana cell in an ME Drive on the same network.
3. The bus fills the cell. Watch the idle drain when installing several cells.

### Feed Botania from the network

1. Place a Mana Export Bus against the target pool or consumer.
2. Store mana in cells first.
3. The bus exports whenever the target has room and the network has power.

### Use the Fluix Mana Pool as the Botania face

1. Connect the pool to the network with a channel.
2. Install mana cells.
3. Point spreaders, collectors or sparks at it as if it were a normal mana pool.

This avoids a separate bus for every pool and keeps mana in one network-wide buffer.

### Expose a Botania pool as storage

A Universal Storage Bus can expose a normal Botania mana receiver because Applied Botanics registers mana as an external storage type. This is useful for filtered or prioritized access, but it does not replace dedicated mana cells.

## Troubleshooting

**Mana disappears in the pool.**
The network has no mana storage. Add a mana cell or creative mana cell.

**The mana bus does nothing.**
Check the receiver, network power and channel, and whether the network has free capacity for imports or stored mana for exports.

**The terminal does not show mana.**
Mana is a separate AE stack type. Check the terminal's type filter and confirm Applied Botanics is installed.

**Can I use fluid pipes or tanks?**
No. Use mana cells, mana buses or the Fluix Mana Pool.
