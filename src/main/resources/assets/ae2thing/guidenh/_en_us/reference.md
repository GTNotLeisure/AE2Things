---
navigation:
  title: Legacy & Reference
  parent: guide.md
  position: 30
  icon: ae2thing:crafting_debug_card
categories:
  - ae2things
author: vcwdfca
date: 2026-11-27
updated: 2026-11-27
---

# Legacy and Reference

This page collects material that is not part of the main feature modules: old Thaumcraft content, config options and common problems.

<Column>
  <Row>
    <ItemImage id="ae2thing:part_infusion_pattern_terminal" scale="4" />
    <ItemImage id="ae2thing:infusion_interface" scale="4" />
    <ItemImage id="ae2thing:part_thaumatorium_interface" scale="4" />
    <ItemImage id="ae2thing:essentia_discretizer" scale="4" />
    <ItemImage id="ae2thing:phial" scale="4" />
  </Row>
</Column>

## Legacy Thaumcraft content

> [!WARNING]
> On AE2 1080 and newer, including GTNH 2.9, these blocks are deprecated and their recipes are disabled. They may still exist for old worlds, but do not build a new automation chain around them.

| Item | Original purpose |
| --- | --- |
| <ItemImage id="ae2thing:part_infusion_pattern_terminal" label="right" /> | Encodes infusion, crucible and distillation patterns, and orders essentia. |
| <ItemImage id="ae2thing:infusion_interface" label="right" /> | Infusion-side interface. |
| <ItemImage id="ae2thing:part_thaumatorium_interface" label="right" /> | Thaumatorium interface. |
| <ItemImage id="ae2thing:essentia_discretizer" label="right" /> | Converts essentia handling into an ME-compatible form. |
| <ItemImage id="ae2thing:phial" label="right" /> | Essentia phial used by the legacy recipes. |

On older AE2 versions these blocks automate infusion, crucible and Thaumatorium workflows. On current GTNH, use modern Thaumic Energistics or GTNH-native essentia automation.

## Configuration

The main config file is `config/ae2thing/ae2thing.cfg`.

| Option | Default | Effect |
| --- | ---: | --- |
| `cellLink` | true | Enables the Infinity Cell link screen and UUID sharing. |
| `magnetRange` | 32 | Backpack Terminal magnet range, 8–64 blocks. |
| `exIOPortTransferContentsRate` | 256 | Extended IO Port transfer rate. |
| `multiPagesSupport` | 2 | Processing-pattern pages in the Dual Interface Terminal, 2–64. |
| `modernInteraction` | true | Click-to-fill/drain container interaction in terminals. |
| `defaultContainers` | `fluid=IC2:itemCellEmpty@0` | Preferred empty container per stack type. |
| `craftingHistorySize` | 200 | Crafting history entries kept, 100–300. |
| `terminalBaubles` | true | Allows supported terminals in any Baubles slot. |
| `backpackTerminalAddTicSupport` | false | Allows the Backpack Terminal to modify Tinkers' Construct tools. |
| `enhancedPins` | true | Enhanced native terminal pins. |
| `scrollPlayerPins` | false | Scroll player pins with the inventory instead of a fixed section. |
| `wirelessConnectorTerminalInfinityConnectionRange` | true | Removes the Wireless Connector Terminal's range limit. |
| `replaceHotIngot` | true | Replaces hot ingot outputs with cooled ingots during processing recipe transfer. |

## NEI options

The relevant options live under **AE2 Thing** and **AE2 Thing → Dual Interface Terminal**.

- Auto upload pattern, enhanced recipe search and search debug
- Recipe-name filling, including circuit and non-consumed ingredient options
- Terminal history, pinned crafting bar and inventory state
- NEI quick order and NEI auto crafting inside AE terminals
- Universal terminal mode

## Troubleshooting

**Why is an Infinity Cell draining so much power?**
The single-type Item and Fluid cells cost 2,000 AE/t each. Check how many are installed and remove unused cells. For a universal cell, read the drive's actual idle power.

**Why can't I link two cells?**
The link slot only accepts the same cell variant. The target must be empty, have no UUID, or already carry the same UUID. `cellLink` must also be enabled.

**Why did auto upload not place a pattern?**
Auto upload only exists on the Wireless Dual Interface Terminal. Enable it in the NEI options, ensure exactly one interface can run the recipe, and keep a blank pattern available. Duplicate or full targets are reported in chat.

**Why does the enhanced `@` search return nothing?**
Enable **Enhanced recipe search** in the NEI options first, then check the recipe pool name. The debug option prints the resolved pool and candidate interfaces to the client log.

**Why can't I craft the Thaumcraft blocks?**
They are deprecated on AE2 1080+ and their recipes are disabled. The items may still exist for old worlds.

**Where is Applied Botanics?**
Mana is a separate module. Continue with [Applied Botanics](appbot.md).

## Related pages

- [AE2Things overview](guide.md)
- [Storage](storage.md)
- [Terminals](terminals.md)
- [Buses & Automation](automation.md)
- [Applied Botanics](appbot.md)
