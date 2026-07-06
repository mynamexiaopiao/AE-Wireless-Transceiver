---
navigation:
  title: AE Wireless Transceiver
  position: 1
  icon: aewireless:wireless_transceiver
item_ids:
  - aewireless:wireless_transceiver
---

# AE Wireless Transceiver

<BlockImage id="aewireless:wireless_transceiver" scale="5" />
<RecipeFor id="aewireless:wireless_transceiver" />

The AE Wireless Transceiver is a device capable of wirelessly transmitting AE channels across any distance, even between dimensions.

## Basic Concepts

### Master End
The Master End must be connected to an AE2 network and serves as the channel provider. It registers a named channel and manages all connected Sub Ends. One Master can connect to multiple Subs.

### Sub End
The Sub End is placed where channels are needed and receives them wirelessly from the Master. A Sub only connects to one Master at a time.

## GUI Overview

Right-click the transceiver to open its GUI:

- **Channel List** (left panel): Shows all available channel names. Click to select, search by typing in the text field above.
- **Add** button: Opens a dialog to create a new channel name.
- **Remove** button: Removes the currently selected channel (requires confirmation).
- **Mode** button: Toggles between Master and Sub mode.
- **Disconnect** button: Disconnects the current channel.
- **Energy unit toggle** (toolbar icon): Switches between AE and FE display.
- **Link overlay toggle** (toolbar icon, Master only): Shows visual connection lines to all linked Sub Ends.

### Channel List Usage
1. Type in the search field to filter channels.
2. Click a channel to select it.
3. Click **Add** to create a new channel, or **Remove** to delete the selected one.

## Wireless Connector

<RecipeFor id="aewireless:wireless_connect" />

The Wireless Connector is an item used together with the transceiver. It can also be equipped in a Curios slot.

### Binding to a Channel
1. Place a Wireless Transceiver and set it to **Master** mode.
2. Create or select a channel in the GUI.
3. Hold the Wireless Connector, sneak + right-click the Master transceiver to bind it to that channel.

### Connecting to AE Blocks (Wireless Direct Connect)
After binding a channel, right-click any AE2 machine (ME Controller, Interface, Drive, etc.) with the Wireless Connector. The machine will join the wireless network directly, without needing a transceiver.

### Mode Toggle
Sneak + right-click on air to toggle between **Connect** mode and **Destroy** mode.
- **Connect mode**: Bind the connector to transceivers or AE blocks.
- **Destroy mode**: Right-click a bound block to remove its wireless connection.

## Energy Mechanics

When enabled in the config (`isEnergy`), energy consumption is calculated per tick:

| Mode | Formula | Default (batteryMultiplier=1.0) |
|---|---|---|
| Master | Fixed `baseEnergy` | 100 AE/t |
| Sub (same dimension) | Distance to Master x `batteryMultiplier` | Varies by distance |
| Sub (cross-dimension) | Distance from origin(0,0) x `batteryMultiplier` | Varies by position |

For cross-dimensional connections, the distance is calculated using the Sub End's XZ distance from its dimension origin (0,0), which increases the further away the Sub is placed.

## Configuration

| Option | Default | Description |
|---|---|---|
| isEnergy | true | Enable/disable energy consumption |
| baseEnergy | 100.0 | Base energy for Master mode (AE/t) |
| batteryMultiplier | 1.0 | Sub energy = distance x multiplier |
| maxDistance | 0 | Max range (0 = unlimited) |
| crossDimensional | true | Allow connections between dimensions |
| shiftAutoConnect | true | Auto-connect when sneaking |

## Compatibility

- **FTB Teams**: Channels isolated per team. Only team members can access their team's transceivers.
- **Curios**: The Wireless Connector can be worn in a curios slot for auto-connect.
- **Jade/WTHIT**: Shows transceiver status, mode, owner, and channel info.

# AE 无线收发器

<BlockImage id="aewireless:wireless_transceiver" scale="5" />
<RecipeFor id="aewireless:wireless_transceiver" />

AE 无线收发器是一个可以无线传输 AE 频道的设备，支持同维度和跨维度传输。

## 基本概念

### 主端
主端需要连接到 AE 网络，是提供频道的一方。它注册频道名称并管理所有连接的从端，一个主端可连接多个从端。

### 从端
从端放置在需要频道的地方，从主端无线接收频道，一个从端同时只能连接一个主端。

## GUI 介绍

右键打开收发器 GUI：

- **频道列表**（左侧面板）：显示所有可用频道名称，点击选择，上方搜索框可过滤。
- **添加**按钮：打开对话框创建新频道。
- **删除**按钮：删除当前选中的频道（需确认）。
- **模式**按钮：切换主端/从端模式。
- **断开**按钮：断开当前频道连接。
- **能量单位切换**（工具栏图标）：切换 AE / FE 显示。
- **链接显示**（工具栏图标，仅主端）：标记所有已连接的从端位置。

### 频道操作
1. 在搜索框输入文字过滤频道。
2. 点击选择一个频道。
3. 点击**添加**创建新频道，或**删除**移除选中的频道。

## 无线连接器

<RecipeFor id="aewireless:wireless_connect" />

无线连接器是配合收发器使用的物品，也可放入 Curios 饰品栏。

### 绑定频道
1. 放置无线收发器，设置为**主端**模式。
2. 在 GUI 中创建或选择一个频道。
3. 手持连接器，潜行 + 右键点击主端收发器，绑定到该频道。

### 无线直连
绑定频道后，手持连接器右键点击任何 AE2 机器（ME 控制器、接口、驱动器等），该机器会直接加入无线网络，无需额外收发器。

### 模式切换
潜行 + 在空气中右键，切换**连接模式**和**破坏模式**。
- **连接模式**：将连接器绑定到收发器或 AE 方块。
- **破坏模式**：右键已绑定的方块以移除无线连接。

## 能量机制

启用能量消耗（`isEnergy`）时，每 tick 耗能计算方式：

| 模式 | 公式 | 默认值（batteryMultiplier=1.0） |
|---|---|---|
| 主端 | 固定 baseEnergy | 100 AE/t |
| 从端（同维度） | 与主端距离 x batteryMultiplier | 随距离变化 |
| 从端（跨维度） | 从端到原点(0,0)的XZ距离 x batteryMultiplier | 随位置变化 |

跨维度时，使用从端所在维度的 XZ 平面原点距离计算，距离原点越远耗电越高。

## 配置项

| 选项 | 默认值 | 说明 |
|---|---|---|
| isEnergy | true | 启用/禁用能量消耗 |
| baseEnergy | 100.0 | 主端基础耗能（AE/t） |
| batteryMultiplier | 1.0 | 从端耗能 = 距离 x 系数 |
| maxDistance | 0 | 最大传输距离（0 = 无限制） |
| crossDimensional | true | 允许跨维度连接 |
| shiftAutoConnect | true | 潜行放置时自动连接 |

## 兼容性

- **FTB Teams**：频道按队伍隔离，只有同队成员可访问。
- **Curios**：无线连接器可放入饰品栏用于自动连接。
- **Jade/WTHIT**：查看收发器时显示状态、模式、所有者、频道信息。
