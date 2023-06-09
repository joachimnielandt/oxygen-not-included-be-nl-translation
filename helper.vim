" don't lose maps or macros, keep them close (in this file)
" execute ':so %' to source this file

" copy the original into the translation
let @k='k0f"lyi"j0f"p0f"l'
" replace the selection by an automatic translation
let @t=":!trans --brief --no-warn :nl"

" put cursor at next empty translation
map <F7> /msgid ".\+"\nmsgstr "\zs"<CR>zz
" perform automatic translation of empty string
map <F8> @kvi"@t<CR>
" put cursor at next translation
map <F9> /msgstr "\zs<CR>zz:noh<CR>
" select the whole translation and uppercase the words
map <F10> 0f"lvi":s/\%V\<.\%V/\u&/g<CR>0f"l
map <F12> @kvi"@t<CR>

" introduce \z to fold on the last search expression 
" 'zr' for more or 'zm' for less context
nnoremap \z :setlocal foldexpr=(getline(v:lnum)=~@/)?0:(getline(v:lnum-1)=~@/)\\|\\|(getline(v:lnum+1)=~@/)?1:2 foldmethod=expr foldlevel=0 foldcolumn=2<CR>
