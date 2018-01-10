/*---------------------------------------------------------------

   CMSC 330 Project 6 - Maze Solver and SAT in Prolog

   NAME: Benjamin Aronson
   UID: 113548802
   Section: 0106

*/


%%%%%%%%%%%%%%%%%%%%%%
% Part 1 - Recursion %
%%%%%%%%%%%%%%%%%%%%%%

% ackermann - M and N are the two arguments, and R is the result. Cf http://mathworld.wolfram.com/AckermannFunction.html for the definition of the ackermann function

% ackermann(M,N,R) :- M is 0, R is N + 1,!. 
% ackermann(M,N,R) :- N is 0, M1 is M - 1, ackermann(M1,1,R2), R = R2,!.
ackermann(0,N,R) :- R is N + 1,!. 
ackermann(M,0,R) :- M1 is M - 1, ackermann(M1,1,R2), R = R2,!.
ackermann(M,N,R) :- M1 is M - 1, N1 is N - 1, ackermann(M,N1,R2), ackermann(M1,R2,R3), R = R3.

% prod - R is product of entries in list L

prod([H|T],R) :- prod(T,R2), R is R2*H,!.
prod([],1).

% fill - R is list of N copies of X

% fill(N,X,R) :- N > 0, N1 is N-1, fill(N1,X,R2), R = [X|R2],!.	 % find R
% fill(N,X,R) :- N > 0, N1 is N-1, R=[H|_], fill(N1,X,R), X = H,!. % find X
% fill(0,X,[]).		% X = X when R = []?



fill(N,X,R) :- N > 1, N1 is N-1, fill(N1,X,R2), R = [X|R2],!.
fill(1,H,[H]).
% fill(0,_,[]).



% genN - R is value between 0 and N-1, in order

genN(N,R) :- N > 0, N1 is N - 1, genN(N1,R).
genN(N,R) :- N > 0, N1 is N - 1, R = N1.

% genXY - R is pair of values [X,Y] between 0 and N-1, in lexicographic order

genXY(N,R) :- gxyhelp(N,N,R).
gxyhelp(N,M,R) :- N > 0, N1 is N - 1, gxyhelp(N1,M,R).
gxyhelp(N,M,R) :- N > 0, N1 is N-1, R = [N1,P], genN(M,P).

% flat(L,R) - R is elements of L concatentated together, in order
% flat(L,R) :- L = [], R = [].
% flat(L,R) :- L = [H|T], flat(T,R2), append([H],R2,R).
% flat(L,R) :- L = [H|T], flat(T,R2), append(H,R2,R3), R = R3.

flat(L,R) :- L = [H|T], flat(T,R2), ((append(H,R2,R),!);(append([H],R2,R),!)).
flat([],[]).

% is_prime(P) - P is an integer; predicate is true if P is prime.

% is_prime(P) :- ((P = 2) ; (P = 3)),!.

is_prime(P) :- D is float(sqrt(P)), phelp(P,2,D,E), E = 0,!.
is_prime(2).
is_prime(3).

phelp(1,N,D,1):- !.
phelp(P,N,D,E):-
	N =< D,
	M is mod(P,N),
	modcheck(M,A1),
	N1 is N + 1,
	phelp(P,N1,D,E1),
	E is E1 + A1,!.
phelp(P,N,D,0).

modcheck(0,1).
modcheck(_,0). 

/* phelp(P,N,D,E):- 
	N =< D, M is mod(P,N), 
	( 	(M = 0, E is 0,!) ;
		(M =\= 0, N1 is N + 1, phelp(P,N1,D,E), E is P)	), !.
phelp(P,N,D,E). 
N =< D,
	M is mod(P,N),
	N1 is N + 1,
	phelp(P,N1,D,E1),
	M =\= 0,
	E is E1 + 1,!.*/




% in_lang(L) - L is a list of atoms a and b; predicate is true L is in the language accepted by the following CFG:
/*    
CFG 
S -> T | V
T -> UU			
U -> aUb | ab
V -> aVb | aWb
W -> bWa | ba
*/

in_lang(L) :- parS(L,[]).

parS(L,M) :- (parT(L,M),!) ; (parV(L,M),!).
parT(L,O) :- parU(L,M), parU(M,O).

parU(L,M) :- check(L,a,L2), check(L2,b,M),!.
parU(L,M) :- check(L,a,L2),	parU(L2,M2), check(M2,b,M).

parV(L,M) :- check(L,a,L2),	parV(L2,M2), check(M2,b,M).
parV(L,M) :- check(L,a,L2),	parW(L2,M2), check(M2,b,M).

parW(L,M) :- check(L,b,L2),	parW(L2,M2), check(M2,a,M).
parW(L,M) :- check(L,b,L2), check(L2,a,M),!.

check([H|T],H,T).


%%%%%%%%%%%%%%%%%%%%%%%%
% Part 2 - Maze Solver %
%%%%%%%%%%%%%%%%%%%%%%%%

% stats(U,D,L,R) - number of cells w/ openings up, down, left, right
 
stats(U,D,L,R) :- 
	(X is 0, Y is 0, U2 is 0, D2 is 0, L2 is 0, R2 is 0,
	maze(N,_,_,_,_), statshelp(N,X,Y,U2,D2,L2,R2,S),
	S = [U,D,L,R]),!.

statshelp(N,X,Y,U2,D2,L2,R2,S):- 			% Iterate through cols
	X < N, Y < N,
	getcell(N,X,Y,U3,D3,L3,R3),
	U4 is U2 + U3,D4 is D2 + D3,
	L4 is L2 + L3, R4 is R2 + R3,
	X1 is X + 1,
	statshelp(N,X1,Y,U4,D4,L4,R4,S).
statshelp(N,X,Y,U2,D2,L2,R2,S):-			% Change row 
	X = N, X1 is 0, Y1 is Y + 1,
	statshelp(N,X1,Y1,U2,D2,L2,R2,S).
statshelp(N,0,N,U2,D2,L2,R2,S):-			% Base case
	S = [U2,D2,L2,R2].
	
getcell(N,X,Y,U,D,L,R):- 
	cell(X,Y,DS,_), 
	findall(u,member(u,DS),RU), findall(d,member(d,DS),RD),
	findall(l,member(l,DS),RL), findall(r,member(r,DS),RR),
	len(RU,RUL), len(RD,RDL), len(RL,RLL), len(RR,RRL),
	U is RUL, D is RDL, L is RLL, R is RRL .

% Helper to get length of list
len([],0).
len([H|T],R) :- len(T,R1), R is R1 + 1.

% validPath(N,W) - W is weight of valid path N rounded to 4 decimal places


validPath(N,W) :- 
	% maze(SZ,_,_,_,_),
	path(N,SX,SY,PDS),
	vphelp(N,SX,SY,PDS,W0), round4(W0,W).

vphelp(N0,X,Y,[],0).
vphelp(N0,X,Y,[PH|PT],W):- 
	% cell(SX,SY,[],[]), 	-- take care of this
	cell(X,Y,CDirs,CWts), 		% get current cell data
	member(PH,CDirs), 			% check if first ele in path list is with this cell
 	getwt(PH,CDirs,CWts,WT),	% WT is the weight of this section of the path
	movehelp(PH,X,Y,NX,NY),		
	vphelp(N0,NX,NY,PT,W1), W is WT + W1,!. 


movehelp(D,X,Y,NX,NY):-
	( D = u, NX is X, NY is Y - 1,! );
	( D = d, NX is X, NY is Y + 1,! );
	( D = l, NX is X - 1, NY is Y,! );
	( D = r, NX is X + 1, NY is Y,! ).

getwt(H1,[H1|_],[H2|_],H2).
getwt(E,[_|T1],[_|T2],WT0):- getwt(E,T1,T2,WT0),!.

round4(X,Y) :- T1 is X*10000, T2 is round(T1), Y is T2/10000.

% findDistance(L) - L is list of coordinates of cells at distance D from start

findDistance(L) :- 
	maze(_,SX,SY,_,_),
	cell(SX,SY,_,_),
	Curr = [[SX,SY]],
	DL0 = [0, Curr],
	Seen = Curr, L0 = [DL0],
	fdh(Curr,1,Seen,L1),
	append(L0,L1,L2),
	removelast(L2,L).
	
%	traverse ( curr , D, Seen, R )
fdh([],_,_,[]).
fdh(C,D,S,R):-
	getcoords(C,S,Adj),
	append(Adj,S,S1),
	D1 is D + 1,
	fdh(Adj,D1,S1,R1),
	DL = [D, Adj],
	append([DL],R1,R),!.
%
getcoords([],_,[]).
getcoords([H|T],S,R):-
	getxy(H,S,XY),
	getcoords(T,S,R2),
	append(XY,R2,R3),
	makeset(R3,R),!.
%
getxy([X,Y],S,R1):-
	cell(X,Y,Dir,_),
	getxy2(X,Y,Dir,S,PL),
	setof(P,pmem(P,PL,S),R1),!.	
	% ^this is the issue if there is nothing that satisfies pmem 
getxy(CO,_,[]).	
%
getxy2(X,Y,[],_,[]).
getxy2(X,Y,[H|T],S,R1):-
	movehelp(H,X,Y,X1,Y1),
	getxy2(X,Y,T,S,R2),
	P = [X1,Y1],
	append([P],R2,R1).
%
pmem(P,PL,S):-
	member(P,PL),
	\+ member(P,S).
%
removelast([],[]).
removelast([H],[]).
removelast([H|T],R) :- removelast(T,R1), R = [H|R1],!.
%
makeset(L,R) :- setof(P,member(P,L),R),!.
makeset([],[]).




% solve - True if maze is solvable, fails otherwise.

/* 	L 	= 	[ [0, [ [X1,Y1],... ] ] , ... ]  *	
 *	DL 	= 	[0 , [ [X1,Y1],... ] ]			 * H1=[D,[[X1,Y1],...]]
 *	PL	=	[ [X1,Y1] , ... ]				 * H2=[[X1,Y1],...]
 */

solve :- 
	maze(_,_,_,EX,EY),
	findDistance(L),
	breaklist(L,L1),
	member([EX,EY],L1),!.


breaklist([],[]).
breaklist([ [_,PL] |T1],RL):-
	% write(PL), write("\n"),
	breakpairlst(PL,R1),
	breaklist(T1,R2),
	append(R1,R2,RL).

breakpairlst([],[]).
breakpairlst([H|T],RL):-
	breakpairlst(T,R1),
	RL = [H|R1].











%%%%%%%%%%%%%%%%
% Part 3 - SAT %
%%%%%%%%%%%%%%%%



% eval(F,A,R) - R is t if formula F evaluated with list of 
%                 true variables A is true, false otherwise

eval(F,A,R) :- fail.

% varsOf(F,R) - R is list of free variables in formula F

varsOf(F,R) :- fail.

% sat(F,R) - R is a list of true variables that satisfies F

sat(F,R) :- fail.

% Helper Function
% subset(L, R) - R is a subset of list L, with relative order preserved

subset([], []).
subset([H|T], [H|NewT]) :- subset(T, NewT).
subset([_|T], NewT) :- subset(T, NewT).

