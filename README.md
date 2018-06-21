# GetMeHome
A straightforward Spigot plugin that gets you home

This plugin works on Minecraft 1.9.4 and up!

## [Configuration](src/main/resources/config.yml)

By default, everyone is limited to one home.  The plugin stores all the data
in the `homes.yml` file under plugin directory.  If that's all you need, then
you are good to go!

The MySQL functionality hasn't been implemented yet, so you'll have to wait for
that if you want to store player homes on a database.

You can modify the configuration file `config.yml` to fit your usage case. You
can configure:

- Color of the chat
- Permission group with different home limits

Check out [`config.yml`](src/main/resources/config.yml) for more
configuration information.

## [Commands and Permissions](src/main/resources/plugin.yml)

All the available commands and their aliases are listed in the main
[`plugin.yml`](src/main/resources/config.yml) file. Permission nodes are
registered in there as well.

## [Contributing](CONTRIBUTING.md)

This project uses Maven for all dependencies and packaging process.  If you want
to use an IDE with this code, clone this project and import it into a
Maven-compatible IDE or just use Maven. If you're unable to test, feel free to
make a pull request so I can test your changes.

Check out [`CONTRIBUTING.md`](CONTRIBUTING.md) for more information.

## [Spigot plugin page](#) (to be added)
