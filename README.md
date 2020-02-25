# The End
[![Build](https://img.shields.io/circleci/build/github/wode490390/TheEnd/master)](https://circleci.com/gh/wode490390/TheEnd/tree/master)
[![Release](https://img.shields.io/github/v/release/wode490390/TheEnd)](https://github.com/wode490390/TheEnd/releases)
[![Release date](https://img.shields.io/github/release-date/wode490390/TheEnd)](https://github.com/wode490390/TheEnd/releases)
<!--[![Servers](https://img.shields.io/bstats/servers/4882)](https://bstats.org/plugin/bukkit/TheEnd/4882)
[![Players](https://img.shields.io/bstats/players/4882)](https://bstats.org/plugin/bukkit/TheEnd/4882)-->

This plugin is mainly used to generate the end world in [Nukkit](https://github.com/NukkitX/Nukkit) server.

[![](https://i.loli.net/2019/06/12/5d0035e6ec88465573.png)](https://www.mcbbs.net/thread-872583-1-1.html "末路之地")

If you found any bugs or have any suggestions, please open an issue on [GitHub Issues](https://github.com/wode490390/TheEnd/issues).

If you love this plugin, please star it on [GitHub](https://github.com/wode490390/TheEnd).

## Features
- [ ] World
  - [X] Main End Island
  - [X] Small End Island
  - [ ] End Midland
  - [ ] End Highland
  - [ ] End Barren
  - [X] End Podium
  - [X] Obsidian Pillar
  - [X] Obsidian Platform
  - [X] Chorus Tree
- [ ] Entities
  - [X] End Crystal
  - [X] Ender Dragon (requires [MobPlugin](https://github.com/Nukkit-coders/MobPlugin))
    - [ ] Re-summoning
- [ ] Misc
  - [X] End Poem
  - [X] End Portal
  - [ ] End Gateway

## Download
- [Releases](https://github.com/wode490390/TheEnd/releases)
- [Snapshots](https://circleci.com/gh/wode490390/TheEnd)

## Configurations

### config.yml
```yaml
enable-end-portal: true
exit-portal-activated: true
spawn-ender-dragon: true
generator:
  end:
    coordinate-scale: 684.412
    height:
      scale: 1368.824
    detail:
      noise-scale:
        x: 80.0
        y: 160.0
        z: 80.0
```

## Compiling
1. Install [Maven](https://maven.apache.org/).
2. Run `mvn clean package`. The compiled JAR can be found in the `target/` directory.

## Known Issues
+ The dimension is not changed when the player enters the portal. (Not supported by [Nukkit](https://github.com/NukkitX/Nukkit/issues/839))

## Metrics Collection

This plugin uses [bStats](https://github.com/wode490390/bStats-Nukkit) - you can opt out using the global bStats config, see the [official website](https://bstats.org/getting-started) for more details.

[![Metrics](https://bstats.org/signatures/bukkit/TheEnd.svg)](https://bstats.org/plugin/bukkit/TheEnd/4882)

###### If I have any grammar and terms error, please correct my wrong :)
