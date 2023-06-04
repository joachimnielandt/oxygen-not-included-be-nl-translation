# About
Flemish translation of Oxygen Not Included. 

# Contribute
How to contribute:
- Create a new branch and modify `nl_BE.pot`. 
- Create a pull request to merge into the `main` branch.

I try to keep this file up to date, check status below for work in progress.

# Status
- All translations were added using automation
- Syntax was fixed
- Final manual check up to: line 12553

# Changelog
- 2023/05/22 - started translation based on Steam version of `strings_template.pot` 

# Todo
Some things to clean up or check...
- radbolts > radbolten
- duplicants > duplicanten
- snazzy suits
- primo garb
- 'cot' > veldbed?

# VIM
Useful macros:

## Go to next empty translation 
`:let @j /msgstr ""<CR>f"lzz`

## Copy original text 
`:let @k ?msgid<CR>f"lyi"j0f"p0wlzz`

## Translate current line
`:let @t k0f"lvi"yjjkop0V:!trans -rbkbkbbrief --no-warn Lkb:nlddkf"pv$dkf"pJx0f"l`
`:let @t k0f"lyi"j0f"p0f"lvi":!trans -brief --no-autocorrect --no-warn :nl<CR>0f"l

## Uppercase selection
```vim
:let @u :'<,'>s/\%V\<.\%V/\u&/g
```
