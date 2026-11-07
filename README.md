# AE2Things
Neat little additions to AE2-GTNH

This mod adds a new type of cell the DISK: Deep Item Storage disk. This cell has no type limits.

## Downloads & Requirements
| GTNH  | AE2T   | Download                                                                                                                   | Maintenance |
|-------|--------|----------------------------------------------------------------------------------------------------------------------------|:-----------:|
| 2.7.4 | 1.1.28 | [![1.1.28](https://img.shields.io/badge/release-v1.1.28-orange)](https://github.com/asdflj/AE2Things/releases/v1.1.28)     |     ❌️      |
| 2.8.0 | 1.2.8  | [![1.2.8](https://img.shields.io/badge/release-v1.2.8-orange)](https://github.com/asdflj/AE2Things/releases/v1.2.8)        |    ❌️️       |
| 2.8.4 | 1.2.15 | [![1.2.15](https://img.shields.io/badge/release-v1.2.15-orange)](https://github.com/asdflj/AE2Things/releases/v1.2.15)     |    ❌️️       |
| 2.9.0 | 1.2.16 | [![Latest](https://img.shields.io/github/v/release/ABKQPO/AE2Things)](https://github.com/ABKQPO/AE2Things/releases/latest) |    ✔️       |

## License & Copyright

Copyright (c) 2024-2026 the AE2Things contributors (see the git history)<br>
Copyright (c) 2026 GTNotLeisure org (Applied Botanics module, 29x maintenance)

The project's own code is released under the GNU General Public License v3.0, see [LICENSE](LICENSE).

### Third-party material

Two upstream projects are involved. They carry different licenses, so they are kept separate here.

| Upstream                                                                  | License                                                | How it is used                                                                                | Bundled in this jar |
|:--------------------------------------------------------------------------|:-------------------------------------------------------|:----------------------------------------------------------------------------------------------|:-------------------:|
| [Botania](https://github.com/GTNewHorizons/Botania) (Vazkii)              | [Botania License](https://botaniamod.net/license.html) | runtime dependency: API calls and extended internals, its own pool texture is read at runtime |         ❌          |
| [appbot / Applied Botanics](https://github.com/ramidzkh/Applied-Botanics) | [LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html) | ported code and all resource files                                                            |         ✅          |

#### Botania (Vazkii) — runtime dependency, nothing bundled

The Applied Botanics module is built on top of Botania and requires it at runtime. It calls Botania's API
(`vazkii.botania.api.*`) and extends its non-API internals: `vazkii.botania.common.block.BlockModContainer`
and `vazkii.botania.common.block.tile.mana.TilePool` are used as base classes, other `vazkii.botania.common.*`
/ `vazkii.botania.client.*` classes are used for pool behaviour and rendering, and Botania's own
`textures/model/pool.png` is read at runtime. These are modifications and additions by this project, not
official Botania content; no Botania source files or assets are bundled in this jar.

Botania is licensed under the [Botania License](https://botaniamod.net/license.html), whose attribution clause
requires crediting Vazkii and indicating alterations, and whose copyleft clause requires derived projects to be
open source with a similar clause — both are satisfied by this project being GPLv3 with its sources published.

#### appbot / Applied Botanics — ported code and resource files (LGPL-3.0)

Parts of the module's code are ported and adapted from appbot. The module's resource files are taken or
adapted from appbot as well: the blockstates and block model of the fluix mana pool, and every texture — the
mana cells, the cell housing, the creative cell, the mana marker, the GUI / inventory icons and the fluix mana
pool model textures. Only the language files under `assets/ae2thing_ab/lang` are written for this project.

appbot is licensed under LGPL-3.0, which allows relicensing into a GPLv3 work; the original copyrights of the
ported code and resource files remain with appbot's authors. The 1.7.10 / GTNH port itself is this project's
work.

## Gallery
**Infinity cell**</br>
Can set link mode to link other infinity cell. It will share stored items
![infinity_cell_link.png](blob/infinity_cell_link.png)
**Middle click to quick order item**</br>
![order.gif](blob/order.gif)
**Terminal menu**</br>
![terminal_menu.gif](blob/terminal_menu.gif)
**NEI encode pattern**</br>
nei support display encode pattern
![nei_encode_pattern.gif](blob/nei_encode_pattern.gif)
**Cell Content**</br>
Display cell content
![cell_content.png](blob/cell_content.png)
**Middle click to quick take item from me system**</br>
![middle_click_quick_take_item.gif](blob/middle_click_quick_take_item.gif)
**Backpack Terminal**</br>
Can easily manage the items in the backpack, support magnet mode and fluid
![backpack_terminal.png](blob/backpack_terminal.png)
![backpack_terminal_2.png](blob/backpack_terminal_2.png)
**Dual Interface Terminal**</br>
Support encode pattern
![dual_interface_terminal.png](blob/dual_interface_terminal.png)
![dual_interface_terminal_2.png](blob/dual_interface_terminal_2.png)
**Rename In Pattern**</br>
`Ctrl + middle click` to edit item name
![pattern_rename.gif](blob/pattern_rename.gif)
**Auto Upload Pattern**</br>
![dual_interface_terminal_auto_upload.gif](blob/dual_interface_terminal_auto_upload.gif)
`Alt + left click` to upload manually</br>
`Alt + scroll` to select interface</br>
**Infusion Pattern Terminal**</br>
Support infusion recipe and distillation recipe,Can order fluid and essentia
![infusion_pattern_terminal.gif](blob/infusion_pattern_terminal.gif)
![infusion_pattern_terminal_2.gif](blob/infusion_pattern_terminal_2.gif)
![infusion_pattern_terminal.png](blob/infusion_pattern_terminal.png)
**Essentia discretizer**</br>
**Thaumatorium Interface**</br>
**Infusion Interface**</br>
support infusion intercepter
![infusion_interface_2.png](blob/infusion_interface_2.png)
**Wireless Connector Terminal**</br>
Can easily manage ae2 stuff wireless connector
![wireless_connector.png](blob/wireless_connector.png)</br>
**ME Fluid Packet Encoder**</br>
![fluid_packet_encoder.gif](blob/fluid_packet_encoder.gif)</br>
**Crafting Debug Card**</br>
![crafting_debug_card.gif](blob/crafting_debug_card.gif)</br>
FishBig and MDDyue
![doll_2.png](blob/doll_2.png)
