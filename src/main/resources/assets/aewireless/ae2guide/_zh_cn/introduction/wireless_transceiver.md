---
navigation:
  title: AE无线收发器
  position: 1
  icon: aewireless:wireless_transceiver
item_ids:
  - aewireless:wireless_transceiver
---

# 无线收发器

<BlockImage id="aewireless:wireless_transceiver" scale="5" />
<RecipeFor id="aewireless:wireless_transceiver" />
AE无线收发器是一个可以无线传输AE频道和能量的设备。

## 基本用法
- **主端** ：需要连接到AE网络，是提供频道的一方。
- **从端** ：放置在需要频道的地方，从主端接收频道，一个主端可以连接多个从端。

## 一些提示
- 默认情况（未安装FTB团队），设备是公开的，任何人都可以连接到主端。
- FTB团队的兼容，当安装了FTB团队模组时，设备会绑定到玩家或团队，每个团队的频道之间相互隔离。
- 无线收发器支持跨维度传输频道，但主端和从端所在的区块必须保持加载状态。
- 支持跨纬度传输
- 几乎不耗电（1AE/t）
