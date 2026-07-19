**Languages: [English](README.md) · [简体中文](README.zh.md)**
---
---
### Introduction
**Yyz’s Backpack** is a lightweight, convenient backpack mod designed to provide a more efficient item management experience without breaking the vanilla UI style. The backpack can be opened directly in various interfaces (such as crafting tables, chests, furnaces, etc.) and supports crafting, sorting, quick moving, and other operations.

*(Insert showcase image here)*

## ✨ Features
- **Access Anywhere**: The backpack UI is displayed directly on top of most container screens, allowing you to take or put items without switching screens.
- **Crafting Assistance**: Supports crafting using items from the backpack, compatible with the recipe book and JEI, with one‑click quick fill support.
- **Dyeing System**: The backpack can be dyed like leather armour, allowing you to personalise its appearance.
- **Backpack Model**: A 3D model is shown on the player when the backpack is equipped, which can be hidden via config.
- **One‑Click Sorting**: Built‑in item sorting. Click the sort button or press a custom key (unbound by default) to sort, with multiple sort modes (Creative inventory order, name, count, ID, etc.) switchable by scrolling on the sort button.
- **Quick Move**: Shift + right‑click to quickly deposit items into the backpack; Shift + click to take items out. Buttons are also available for bulk quick move, supporting moving matching items or moving all items.
- **Quick Switch**: Click tabs above the backpack to quickly switch between backpacks; tabs show the backpack’s name and appearance. You can also press the hotkey (default `B`) to open the backpack currently under the mouse cursor.
- **Auto Collect**: When your inventory is full, newly obtained items automatically go into the backpack.
- **Encumbrance System**: Carrying multiple backpacks (default 3 or more) applies an encumbrance effect, reducing movement speed and jump height.

## 🎒 Backpack Tiers
| Backpack Type | Extra Slots | Special Ability |
|---------------|-------------|-----------------|
| Iron          | +18         | Base capacity   |
| Gold          | +36         | Base capacity   |
| Diamond       | +54         | Base capacity   |
| **Netherite** | **+54**     | **Fire immunity** |

## 🛠️ Crafting Recipes

*(Insert crafting images here)*

## 📦 Installation & Usage

1. Install the appropriate mod loader (**Fabric**, **Forge**, or **NeoForge**) for your Minecraft version.
2. Download the mod `.jar` file and place it into the `mods` folder in your game directory.
3. Launch the game and place the backpack anywhere in your inventory. If you have the corresponding mod installed, you can also place it in the curio slot.
4. Select the backpack to use via tabs or the hotkey.

### 🧩 Compatibility
#### 🤝 Compatible
- **Trinkets**
- **JEI**
- *(More to be added)*

#### ❌ Incompatible
- **All inventory‑sorting mods** (e.g., Inventory Tweaks, Mouse Tweaks, etc.):
    - Symptoms: They only sort the vanilla inventory and ignore the backpack; or they mix the backpack and inventory together and sort them as one; or they fail completely, sometimes causing crashes.
    - Reason: Due to the mod’s special implementation, it is not compatible with most sorting mods, and no universal fix has been found. **As an alternative, the mod includes a full‑featured sorting system** that meets daily needs.
- *(More to be added)*

## 🗺 Development Roadmap

- **Support more Minecraft versions**
- **Support more modded UI screens**

## 🔗 Links
- [GitHub](https://github.com/yyz729/yyzsbackpack)
- [Discord](https://discord.com/invite/3NPKBgXxVU)

## 🛠 Customisation

### Mod Configuration

Config file path: `/config/yyzsbackpack.json`

| Config Key | Description |
|------------|-------------|
| `model` | Whether to show the backpack model on the player’s back (true/false) |
| `heavy` | How many backpacks carried before the player gains the “encumbrance” effect |
| `controlPoss` | Coordinates of sorting, quick‑move, and other buttons on various UI screens. If a screen doesn’t need a button, delete its entry. |
| `uiOffsets` | Overall offset of the backpack UI on each container screen. |

### Backpack Customisation (Resource Packs & Data Packs)
You can completely redefine the backpack’s GUI layout, slot positions, and capacity. See the mod files or examples on GitHub for details.

- **Resource pack path**: `assets/yyzsbackpack/backpacks/<filename>.json`
- **Data pack path**: `data/yyzsbackpack/backpacks/<filename>.json`

#### Common Properties
| Property | Description |
|----------|-------------|
| `type` | Backpack material: `iron`, `gold`, `diamond`, `netherite` |
| `guiTexture` | Custom GUI texture path |
| `backgroundX`, `backgroundY` | Offset of the background relative to the top‑left of the original GUI |
| `maxVisibleTabs` | Number of backpack switch tabs visible at once (excess can be scrolled) |
| `segments` | List of slot layout segments |

#### Segment Properties
| Property | Description |
|----------|-------------|
| `startSlot`, `endSlot` | Slot index range for this segment |
| `order` | `"DEFAULT"` (grid layout, left‑to‑right, top‑to‑bottom) or `"CUSTOM"` (custom coordinates for each slot) |
| `startX`, `startY` | Starting coordinates (for `DEFAULT` mode) |
| `columns`, `rows` | Grid columns and rows (for `DEFAULT` mode). If slots exceed the grid, scrolling is supported. |
| `customPositions` | Array of `{"x": number, "y": number}` for each slot (for `CUSTOM` mode). *Note: scrolling is not supported in `CUSTOM` mode.* |

#### Properties Available Only in Data Packs
| Property | Description |
|----------|-------------|
| `size` | Sets the backpack size. In multiplayer, only the server setting takes effect. |
| `force_server` | If `true`, the server forces all clients to use this configuration (recommended for multiplayer). |