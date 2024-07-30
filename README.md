# About
Dutch/Flemish translation of Oxygen Not Included. Published on https://steamcommunity.com/sharedfiles/filedetails/?id=2986507109.

# Contribute
How to contribute:
- Create a new branch and modify `strings.po`. 
- Create a pull request to merge into the `main` branch.

When using VIM, check the `.vim` file for some handy shortcuts.

# Test locally
Put the `release` files into the following folder: `C:\Users\<username>\Documents\Klei\OxygenNotIncluded\mods\local\nl-be-flemish`. This should result in a new `Translation` being available in ONI. Select it in the main menu under `Translations`.

# Translation service

Run the command below to enable the translation service used in the kotlin code:

```bash
docker run -ti --rm -p 5000:5000 libretranslate/libretranslate
```