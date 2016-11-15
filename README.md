# GetMeHome
A straightforward Spigot plugin that gets you home

## [Configuration](src/main/resources/config.yml)

By default, everyone are limited to one home.  The plugin stores all the data
in the `homes.yml` file under plugin directory.  If that's all you need, then
you are good to go!

You may modify the configuration file `config.yml` to fit your usage case.  The
only configurable settings are:

- Option to store data on MySQL or SQLite
- Permission group with different home limited

Please visit [`config.yml`](src/main/resources/config.yml) for more
configuration information.

## [Commands](src/main/resources/plugin.yml)

All the available commands and their aliases are listed in the main
[`plugin.yml`](src/main/resources/config.yml) file.  Special important
permission nodes are registered in there as well.

## [Contributing](CONTRIBUTING.md)

This project uses Maven for all dependencies and packaging process.  If you want
to use an IDE with this code, clone this project and import it into a
Maven-compatible IDE.

Check out [`CONTRIBUTING.md`](CONTRIBUTING.md) for more information.

## [Spigot plugin page](#) (to be added)
