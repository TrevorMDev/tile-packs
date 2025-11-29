# Tile Packs

[![Active Installs](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/tile-packs)](https://runelite.net/plugin-hub/show/tile-packs)
[![Plugin Rank](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/tile-packs)](https://runelite.net/plugin-hub/show/tile-packs)

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://buymeacoffee.com/trevormdev)

[Tile Packs Discord](https://discord.gg/4EQRWxY3Wb)

---

This plugin serves as a collection of tile packs you can add and remove quickly and easily, for content all over the game.

These tiles do not interfere with the ground markers plugin, they are saved separately.

![A demo of using tile packs](readme_resources/tile_packs_demo.gif)

If you have tiles you would like added, create an issue including the ground markers, or join the Discord.
You can export the ground markers from Runelite by right-clicking the minimap orb.
Use this tool to double-check your exported tiles. https://runelite.net/tile/

---
# Guide

### How do I use this plugin?
After installing and enabling the plugin, you will get a new icon in the right panel of runelite.
Selecting this will show the tile packs and all their options.

![How to access the tile pack panel](readme_resources/tile_packs_panel_reference.png)

### Config options

![A built in title pack](readme_resources/tile_pack_reference.png)
![A custom tile pack](readme_resources/custom_pack_reference.png)

- Toggle a pack on and off
  - The green add icon ![add icon](src/main/resources/com/tilepacks/add_icon.png) will turn a pack on. The tiles will render the tiles in the world and minimap(if enabled).
  - The red remove icon ![remove icon](src/main/resources/com/tilepacks/remove_icon.png) will turn a pack off. The tiles will not render in the world or minimap.
- The help icon ![help icon](src/main/resources/com/tilepacks/help_icon.png) will open the source of the tiles of the pack in your browser. Most often a YouTube video.
- The copy icon ![copy icon](src/main/resources/com/tilepacks/copy_icon.png) will copy the tiles of the pack to your clipboard.
- The visible icon ![visible icon](src/main/resources/com/tilepacks/visible_icon.png) means the tile pack is not hidden. See [Searching and Filtering](#Searching and Filtering).
- The invisible icon ![invisible icon](src/main/resources/com/tilepacks/invisible_icon.png) means the tile pack is hidden. See [Searching and Filtering](#Searching and Filtering).
- The delete icon ![delete icon](src/main/resources/com/tilepacks/delete_icon.png) is to delete custom packs. This is permanent and they cannot be recovered. Default packs cannot be deleted, but they can be hidden.

### Searching and Filtering
![Searching and Filtering](readme_resources/search_and_filter_reference.png)

- You can search for tile packs by their pack name. The search is not case sensitive, but is not a smart search and must be a partial substring of the title.
- The eye icon with no line is for showing packs that are NOT hidden in the list. When white, it is enabled and the visible packs will be shown. When grey, it is disabled and the visible packs will not be shown.
- The eye icon with a line through it is for showing packs that are hidden in the list. When white, it is enabled and the hidden packs will be shown. When grey, it is disabled and the hidden packs will not be shown.

### Adding a custom pack
![Adding a custom pack](readme_resources/adding_custom_pack_reference.png)

You can add custom packs by scrolling all the way to the bottom of the tile pack list.
To add a custom pack, enter a pack name, and the tiles you want in the pack.

The tiles are formatted to be compatible with ground marker tiles.
You can import ground marker tiles by right clicking the minimap icon and choosing __Export ground markers__.
This will copy all loaded(in your render distance) ground markers to your clipboard.
If you get a formatting error, or want to edit the copied tiles, checkout the tool on https://runelite.net/tile/

---
## Credits

Icons made by [Freepik](https://www.flaticon.com/authors/pixel-perfect), [Utari Nuraeni](https://www.flaticon.com/authors/utari-nuraeni), [Andrean Prabowo](https://www.flaticon.com/authors/andrean-prabowo), and [uicon](https://www.flaticon.com/authors/uicon) from [flaticon.com](https://www.flaticon.com/).
