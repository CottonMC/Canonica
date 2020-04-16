<img src="icon.png" align="right" width="180px"/>

# Canonica


[>> Downloads <<](https://github.com/CottonMC/Canonica/releases)

*Hit the books!*

**This mod is open source and under a permissive license.** As such, it can be included in any modpack on any platform without prior permission. We appreciate hearing about people using our mods, but you do not need to ask to use them. See the [LICENSE file](LICENSE) for more details.

Canonica adds a configurable system for unifying item outputs of data-driven content via a hook for getting the "canonical" item from a given tag.

## Usage
TODO: add import instructions once published

Canonica has native support for recipes and loot tables.

### Recipes
To use Canonica in a recipe output, change the name of the `"item"` field in `"result"` to `"tag"`, and have the value of `"tag"` be the tag that you want to get the canonical resource from.
Canonica also injects into all types of smelting and all types of cutting recipes so you can use a full JSON object for the result field, instead of just providing a string.

### Loot Tables
To use Canonica in a loot table, add an entry of type `"canonica:tag"`, with the `"name"` field being the name of the tag you want to use.