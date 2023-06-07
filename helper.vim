" don't lose maps or macros, keep them close (in this file)
" execute ':so %' to source this file

" copy the original into the translation
let @k='k0f"lyi"j0f"p0f"l'
" replace the selection by an automatic translation
let @t=":!trans --brief --no-warn :nl"

" perform automatic translation of empty string
map <F8> @kvi"@t<CR>
" put cursor at next translation
map <F9> /msgstr "\zs<CR>zz:noh<CR>
" select the whole translation and uppercase the words
map <F10> 0f"lvi":s/\%V\<.\%V/\u&/g<CR>0f"l
map <F12> @kvi"@t<CR>
