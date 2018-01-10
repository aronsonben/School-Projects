% This maze is unsolvable.
maze(2,1,1,0,0).
cell(0,0,[],[]).
cell(1,0,[d],[0.5]).
cell(0,1,[r],[1,1]).
cell(1,1,[u,l],[1,1]).
path('path1',1,1,[l]).
path('invalidPath',0,1,[u]).
