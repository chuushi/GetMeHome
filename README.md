# ![Logo](https://www.spigotmc.org/data/resource_icons/66/66257.jpg) GetMeHome
![Bukkit](https://img.shields.io/badge/bukkit-1.7%20--%201.14-brightgreen.svg)
[![GitHub All Release Downloads](https://img.shields.io/github/downloads/SimonOrJ/GetMeHome/total.svg?label=github%20downloads)](https://github.com/SimonOrJ/GetMeHome/releases)
[![GitHub release](https://img.shields.io/github/release/SimonOrJ/GetMeHome.svg)](https://github.com/SimonOrJ/GetMeHome/releases/latest)
[![GitHub pre-release](https://img.shields.io/github/release-pre/SimonOrJ/GetMeHome.svg?label=pre-release)](https://github.com/SimonOrJ/GetMeHome/releases)
[![Build Status](https://travis-ci.org/SimonOrJ/GetMeHome.svg?branch=master)](https://travis-ci.org/SimonOrJ/GetMeHome)
[![Maintainability](https://api.codeclimate.com/v1/badges/e45a154be37e3eac2375/maintainability)](https://codeclimate.com/github/SimonOrJ/GetMeHome/maintainability)

*A straightforward Spigot plugin that gets you home*

There are plenty of home plugins out there, but this plugin was built out of
necessity for a survival server. I could not find any reliable home plugin to
rely on for clean database storage that also supports multi-home. Any home
plugin that I found was either too bloated or too simple. I decided to take it
on my own, and so, GetMeHome was born.

GetMeHome has all the features you would need for a simple home plugin, and the
configuration is very simple to understand and set up!  It was tested to work on Minecraft
1.7 and up, and it will continue being updated to be future-proof!

GetMeHome features:

* `/home`, `/sethome`, `/delhome`, and `/listhomes`
* Support for **multi-home** by declaring custom permission nodes
* Ability to **change the default home** to go to when running `/home` without any
  arguments (`/setdefaulthome`)
* Individual **localization** based on individual **user's Minecraft game
  language**, currently supporting:
  * English (Default)
    * ɥsᴉꞁᵷuƎ (v1.0.3)
    * LOLCAT (v1.0.0)
    * Pirate Speak (v1.0.1)
    * Shakespearean English (v1.0.3)
  * Japanese (v1.0.3; by kj_Brooke)
  * Korean (v1.0.1)
* A way to go to or edit other player's homes

# Configuration

There are several configuration options that you can set.

* `message.prefix`: *Default: "&6[GetMeHome]".* The tag used for all messages
  from GetMeHome.
* `message.content-color` and `message.focus-color`: Change message colors
  without any hassle!
* `welcome-home-radius`: *Default: 4.* Distance away from home point to show
  the "Welcome home" message. Can be disabled by setting it to -1.
* `limit.default`: *Default: 1.* Change the default home limit here.
* `limit.<permission node>`: Add custom permission nodes and the home limits
  here!

For additional information, check [`config.yml`](src/main/resources/config.yml).

## Commands and Permissions

The default permissions should work well with your needs. All usable permission
nodes are listed below.

For additional information, check [`plugin.yml`](src/main/resources/plugin.yml).

### User commands

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

[![bStats GetMeHome Signature](https://bstats.org/signatures/bukkit/GetMeHome.svg)](https://bstats.org/plugin/bukkit/GetMeHome/)
