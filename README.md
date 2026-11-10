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

---

### Config options

![A built in title pack](readme_resources/tile_pack_reference.png)
![A custom tile pack](readme_resources/custom_pack_reference.png)

- Toggle a pack on and off
  - The green add icon ![add icon](src/main/resources/com/tilepacks/add_icon.png) will turn a pack on. The tiles will render the tiles in the world and minimap(if enabled).
  - The red remove icon ![remove icon](src/main/resources/com/tilepacks/remove_icon.png) will turn a pack off. The tiles will not render in the world or minimap.
- The YouTube ![youtube](src/main/resources/com/tilepacks/youtube.png), Discord ![discord](src/main/resources/com/tilepacks/discord.png), or help icon ![help icon](src/main/resources/com/tilepacks/help_icon.png) will open the source/guide for the tiles of the pack in your browser.
- The copy icon ![copy icon](src/main/resources/com/tilepacks/copy_icon.png) will copy the tiles of the pack to your clipboard.
- The visible icon ![visible icon](src/main/resources/com/tilepacks/visible_icon.png) means the tile pack is not hidden. See [Searching and Filtering](#Searching and Filtering).
- The invisible icon ![invisible icon](src/main/resources/com/tilepacks/invisible_icon.png) means the tile pack is hidden. See [Searching and Filtering](#Searching and Filtering).
- The delete icon ![delete icon](src/main/resources/com/tilepacks/delete_icon.png) is to delete custom packs. This is permanent and they cannot be recovered. Default packs cannot be deleted, but they can be hidden.
- The edit icon ![edit icon](src/main/resources/com/tilepacks/edit_icon.png) will take you into edit mode for a pack. This only appears for packs that are enabled.

---

### Searching and Filtering
![Searching and Filtering](readme_resources/search_and_filter_reference.png)

- You can search for tile packs by their pack name. The search is not case sensitive, but is not a smart search and must be a partial substring of the title.
- The eye icon with no line is for showing packs that are NOT hidden in the list. When white, it is enabled and the visible packs will be shown. When grey, it is disabled and the visible packs will not be shown.
- The eye icon with a line through it is for showing packs that are hidden in the list. When white, it is enabled and the hidden packs will be shown. When grey, it is disabled and the hidden packs will not be shown.

---

### Editing a Pack
![Editing a Pack](readme_resources/edit_pack_icon.png)

Choose **Edit** ![edit icon](src/main/resources/com/tilepacks/edit_icon.png) on any pack to open the pack editor, which replaces the pack list with an edit panel.

---

The edit panel allows you to:
- Edit the packs name
- Edit the default pack color
- add/remove tiles from the pack.

Use the top left back arrow ![back icon](src/main/resources/com/tilepacks/back_icon.png) to return to the list view.

![edit panel](readme_resources/edit_panel.png)

The default color is the color used for marking new tiles.

---

While in edit mode for a pack, hold **Shift** and right click to interact with a tile. 

- Choose `Mark in '[name]' pack` to mark that tile in the pack, which will use the current default color of the pack.
- Choose `Remove from '[name]' pack` to remove that tile in the pack.
- Choose `Edit label in '[name]' pack` to edit the label of the pack.

![mark tile](readme_resources/mark_tile.png) ![unmark tile](readme_resources/unmark_tile.png)

Title changes are saved automatically.

Tile colors are drawn in this order: the global override color when enabled > the tile's own color > the pack's default color > then yellow.

### Adding a custom pack
![Adding a custom pack](readme_resources/adding_custom_pack_buttons.png)

You can add custom packs by scrolling all the way to the bottom of the tile pack list.

---

Click `Imprt Pack with Tiles` to import an existing tile pack. 

In this interface, enter a pack name, and the tiles you want to add. Checking `Enter edit mode?` will take you straight into editing of the new pack. Click `OK` when done.

![import pack](readme_resources/import_pack.png)

---

Click `Create Empty Pack` to create an empty pack.

In this interface, enter a pack name. Checking `Enter edit mode?` will take you straight into editing of the new pack. Click `OK` when done.

![create empty pack](readme_resources/create_empty_pack.png)

---

You can import ground marker tiles by right clicking the minimap icon and choosing __Export ground markers__.
This will copy all loaded(in your render distance) ground markers to your clipboard.
If you get a formatting error, or want to edit the copied tiles, checkout the tool on https://runelite.net/tile/

---
## Credits

Icons made by [Freepik](https://www.flaticon.com/authors/pixel-perfect), [Utari Nuraeni](https://www.flaticon.com/authors/utari-nuraeni), [Andrean Prabowo](https://www.flaticon.com/authors/andrean-prabowo), and [uicon](https://www.flaticon.com/authors/uicon) from [flaticon.com](https://www.flaticon.com/).
