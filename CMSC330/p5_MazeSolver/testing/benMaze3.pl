% This maze is totally open.

public_findDistance :- 
        write('% public_findDistance'),nl,
	findall(L, findDistance(L), Out), 
	write(Out), nl.

maze(3,1,1,2,2).
cell(0,0,[r,d],[1,1]).
cell(1,0,[l,r,d],[1,1,1]).
cell(2,0,[l,d],[1,1]).
cell(0,1,[u,d,r],[1,1,1]).
cell(1,1,[u,d,l,r],[1,1,1,1]).
cell(2,1,[u,d,l],[1,1,1]).
cell(0,2,[r,u],[1,1]).
cell(1,2,[l,r,u],[1,1,1]).
cell(2,2,[l,u],[1,1]).
path('path1',1,1,[u,l,d,d,r,r,u,u,l,d]).
path('path2',0,0,[d,d,d]).