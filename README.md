# ![Logo](https://www.spigotmc.org/data/resource_icons/66/66257.jpg) GetMeHome
![Bukkit](https://img.shields.io/badge/bukkit-1.7%20--%201.13.2-brightgreen.svg)
[![GitHub All Release Downloads](https://img.shields.io/github/downloads/SimonOrJ/GetMeHome/total.svg?label=github%20downloads)](https://github.com/SimonOrJ/GetMeHome/releases)
[![GitHub release](https://img.shields.io/github/release/SimonOrJ/GetMeHome.svg)](https://github.com/SimonOrJ/GetMeHome/releases/latest)
[![GitHub pre-release](https://img.shields.io/github/release-pre/SimonOrJ/GetMeHome.svg?label=pre-release)](https://github.com/SimonOrJ/GetMeHome/releases)
[![Build Status](https://travis-ci.org/SimonOrJ/GetMeHome.svg?branch=master)](https://travis-ci.org/SimonOrJ/GetMeHome)
[![Maintainability](https://api.codeclimate.com/v1/badges/e45a154be37e3eac2375/maintainability)](https://codeclimate.com/github/SimonOrJ/GetMeHome/maintainability)

*A straightforward Spigot plugin that gets you home*

**GetMeHome** has all the features you would need for a simple home plugin.
The configuration is very simple to understand and set up!  This plugin was
tested to work on Minecraft 1.7 and up, and it will continue being updated to
be future-proof!

There are plenty of home plugins out there, but this plugin was built out of
necessity for a survival server. There was no reliable home plugin to rely on
for clean storage and messages that also supports multi-home. Any home plugin
that I found was either too bloated or too simple. I decided to take it on my
own, and so, GetMeHome was born.

GetMeHome features:

* `/home`, `/sethome`, `/delhome`, and `/listhomes`
* Advanced **Multi-home support** by declaring custom permission nodes in
  `limit.yml`
  * `/setdefaulthome` to change default home
* **Warmup and cooldown delay support** by declaring custom permission nodes in
  `delay.yml`
* **Localization** based on MC language, officially supporting:
  * English (Default)
    * ɥsᴉꞁᵷuƎ (v1.0.3)
    * LOLCAT (v1.0.0)
    * Pirate Speak (v1.0.1)
    * Shakespearean English (v1.0.3)
  * Japanese (v1.0.3; by kj_Brooke)
  * Korean (v1.0.1)
* Custom localization or messages available (how-to work in progress)
* A way to go to or edit other player's homes

# Configuration

There are several configuration options that you can set.

* `message.prefix`: *Default: "&6[GetMeHome]".* The tag used for all messages
  from GetMeHome.
* `message.content-color` and `message.focus-color`: Change message colors
  without any hassle!
* `welcome-home-radius`: *Default: 4.* Distance away from home point to show
  the "Welcome home" message. Can be disabled by setting it to -1.

For additional information, check [`config.yml`](src/main/resources/config.yml).

## `limit.yml` and `delay.yml`

These two files follow the same exact structure.

```yaml
- perm:         permission.node
  value:        Home Limit
  operation:    set | world | add | subtract (optional)
  worlds:       [world_name(s), ...] (optional)
```

Home limit in `limit.yml` does not support "subtract" operation.

Examples are shown in [`limit.yml`](src/main/resources/limit.yml) and
[`delay.yml`](src/main/resources/delay.yml).

## Commands and Permissions

The default permissions should work well with your needs. All usable permission
nodes are listed below.

For additional information, check [`plugin.yml`](src/main/resources/plugin.yml).

### User Commands

Everyone is given these permissions by default.

* `getmehome.user`: Allows all commands a user needs to
  use the plugin features.  This enables the following permission nodes:
  * `getmehome.command.home`
  * `getmehome.command.sethome`
  * `getmehome.command.delhome`
  * `getmehome.command.listhomes`
  * `getmehome.command.setdefaulthome`
* `getmehome.command.home`: Allows going home.
  * `/home [name]`
* `getmehome.command.sethome`: Allows setting home.
  * `/sethome [name]`
* `getmehome.command.delhome`: Allows deleting own home.
  * `/delhome [name]`
* `getmehome.command.listhomes`: Allows listing list of own homes.
  * `/listhomes`
* `getmehome.command.setdefaulthome`: Allows changing the default home.
  * `/setdefaulthome <name>`

### Delay Bypass Commands

* `getmehome.delay.instantother`: No delay for /home to other player's home.
  (default: op)
* `getmehome.delay.allowmove`: Allows moving while waiting for /home warmup
  (default: false)

### Administration Commands

By default, these permissions are given only to server operators.

* `getmehome.reload`: Allows reloading plugin configuration. This discards
  homes not yet saved on the file!
  * `/getmehome reload` to reload.
  * `/getmehome clearcache` to clear home cache. *This doesn't do anything for
    now.*
* `getmehome.command.home.other`: Allows going to other player's home.
  * `/home <player> <name>`
* `getmehome.command.sethome.other`: Allows setting other player's home. This
  ignores the target player's home limits, so be sure to check the number
  using `/listhomes <player>`!
  * `/sethome <player> <name>`
* `getmehome.command.delhome`: Allows deletion of other player's home. There is
  no confirmation prompt, so be careful!
  * `/delhome <player> <name>`
* `getmehome.command.listhomes.other`: Allows listing of other players' homes.
  * `/listhomes <player>`

## Contributing

I can use some help with [localization](src/main/resources/i18n). Feel free to fork and create a PR with
new languages!

Check out [`CONTRIBUTING.md`](CONTRIBUTING.md) for more information!

# External Links

* [Spigot resource link](https://www.spigotmc.org/resources/getmehome.66257/)
* [BukkitDev resource link](https://dev.bukkit.org/projects/getmehome)
* [bStats Metrics](https://bstats.org/plugin/bukkit/GetMeHome/)
