(* CMSC 330 / Project 3 *)
(* Student: Ben Aronson *)

(* Fill in the implementation and submit basics.ml *)

(* Part A: Simple functions *)

(* Implement a function head_divisor: int list -> bool that returns
    whether the head of the list given is a divisor of the second element *)

let head_divisor l = match l with
	| []			-> false
	| [x]			-> false
	| h1::h2::t		-> if h2 mod h1 = 0 then true else false
;;

(* Implement a function tuple_addr: int * int * int -> int that returns
    the addition of the three numbers inside the tuple
    Hint: you don't need match *)

let tuple_addr nums =
	let (x,y,z) = nums in
		x + y + z
;;

(* Implement a caddr_int : int list -> int that returns the second element
   of the list, if the list has two or more elements, and returns -1 if
   the list has zero or one elements. *)
let caddr_int l = 
	match l with
		[] 			-> -1
		| [x] 		-> -1
		| h1::h2::t -> h2
;;

(* Part B: Simple curried functions. *)

(* A curried function is one that takes multiple arguments "one at a
      time". For example, the following function sub takes two arguments and
   computes their difference:

   let sub x y = x - y

   The type of this function is int -> int -> int. Technically, this
   says that sub is a function that takes an int and returns a
   function that takes another int and finally returns the answer,
   also an int. In other words, we could write

   sub 2 1

   and this will produce the answer 1. But we could also do something
   like this:

   let f = sub 2 in
   f 1

   and this will also produce 1. Notice how we call sub with only one
   argument, so it returns a function f that takes the second
   argument. In general, you can think of a function f of the type

   t1 -> t2 -> t3 -> ... -> tn

   as a function that takes n-1 arguments of types t1, t2, t3, ...,
   tn-1 and produces a result of type tn. Such functions are written
   with OCaml syntax

   let f a1 a2 a3 ... = body

   where a1 has type t1, a2 has type t2, etc.
*)

(* Implement a function mult_of_n: int -> int -> bool. Calling
   mutl_of_n x y returns true if x is a multiple of y, and false
   otherwise. For example, mult_of_n 5 5 = true and mult_of_n 21 5 =
   false. Note that mult_of_n x 0 = false for all x. *)
   
let mult_of_n x y = 
	if y = 0 then false
	else if x mod y = 0 then true
	else false
;;

(* - Not sure if this was the way to do it so this is currying I think -
let mult_of_n 5 5 = 
	if y = 0 then false
	else if x mod y = 0 then true
	else false
*)



(* Implement a function triple_it: 'a -> 'b -> 'c -> 'a*'b*'c. Calling
   triple_it on arguments x, y and z, should return a tuple with those
   three arguments, e.g., triple_it 1 2 3 = (1,2,3) *)
let triple_it x y z = (x,y,z)

(*
let triple_it x y z =

  ** Do curried function here ** 
  
;;
*)

(* Write a function maxpair : int*int -> int*int -> int*int that takes
   two pairs of integers, and returns the pair that is larger,
   according to lexicographic ordering. For example, maxpair (1,2)
   (3,4) = (3,4), and maxpair (1,2) (1,3) = (1,3).
*)

let maxpair p1 p2 =
	if p1 < p2 then p2
	else if p1 > p2 then p1
	else p1
;;

(* Need to do curried part of this *)



(* Part C: Recursive functions *)

(* Write a function power_of : int -> int -> bool that
   returns true if the second value is a power of the first
   otherwise false *)

let rec power_of x y =
	if x = 0 && y = 0 then true
	else if y = 1 then true
	else
		let xo = x in
		let rec loop x2 =
			if x2 = y then true
			else if y < x2 then false
			else loop (x2*xo) in
		loop x
;;


(* Write a function prod : int list -> int. Calling prod l returns the
   product of the elements of l. The function prod should return 1 if
   the list is empty. *)

let rec prod l = 
	match l with
		| [] 	->  1
		| h::t 	-> h * (prod t)
;;

(* Write a function unzip : ('a*'b) list -> ('a list)*('b
   list). Calling unzip l, where l is a list of pairs, returns a pair
   of lists with the elements in the same order. For example, unzip
   [(1, 2); (3, 4)] = ([1; 3], [2;4]) and unzip [] = ([],[]).
*)


let rec unzip l = 
	let el1 = [] in
	let el2 = [] in
	match l with
		| [] 		-> (el1,el2)
		| (a,b)::t	-> 	let l1, l2 = unzip t in (a::l1,b::l2)
;;



(* | (x,y)::t	-> 	let l1, l2 = unzip t in (x::l1,y::l2)


		| [] 	-> 	failwith "empty list"
		| h::t	-> 	fst h::l1;;
					snd h::l2;;
*)
(* Write a function maxpairall : int*int list -> int*int. Calling
   maxpairall l returns the largest pair in the list l, according to
   lexicographic ordering. If the list is empty, it should return
   (0,0).  For example, maxpairall [(1,2);(3,4)] = (3,4) and
   maxpairall [(1,2);(2,1);(3,1)] = (3,1).
*)

let rec maxpairall l = 
	match l with
		|[] 			-> (0,0)
		|h::[]			-> h
		|h::h2::t		-> if h < h2 then maxpairall (h2::t)
							else maxpairall (h::t)
;;					

(* Write a function addTail : 'a list -> 'a -> 'a list. Calling
   addTail l e returns a new list where e is appended to the back of
   l. For example, addTail [1;2] 3 = [1;2;3]. *)

let rec addTail l x =
	match l with 
		| [] 	-> 	[x]
		| h::t	-> h::(addTail t x)
;;

(* 
EDIT - Came back and tried to make it more efficient, did some research
through the pervasives module, found @ concatenation and found source
code. That is better than this, but leaving this just in case that isn't
allowed.

let rev lst =
	let rec help rl lt = 
		match lt with
			| [] 		->	rl
			| h::t		-> help (h::rl) t in
		help [] lst
;;
   
let rec addTail l x = 
	let revlst = rev l in
	match revlst with
		| []	-> rev (x::[])
		| _		-> rev (x::revlst)
;;
*)

(*
get_val x n
int list -> int -> int
element of list x at index n, or -1 if not found
  (indexes start at 0)
Example: get_val [5;6;7;3] 1 => 6
*)
let rec get_val x n = 
	let rec iter x i = 
		match x with
			| []		-> -1
			| h::t		-> 	
				if n = i then h 
				else iter t (i+1) in
	iter x 0
;;
	

(*
get_vals x y
int list -> int list -> int list
list of elements of list x at indexes in list y,
-1*** if any indexes in y are outside the bounds of x; 
elements must be returned in order listed in y
Example: get_vals [5;6;7;3] [2;0] => [7;5]
*** = (changed this from Piazza -BA)
*)
let rec get_vals x y =
	match y with 
		| [] 	-> y
		| h::t 	-> get_val x h :: get_vals x t
;;

(*
list_swap_val b u v
'a list -> 'a -> 'a -> 'a list
list b with values u,v swapped
(change value of multiple occurrences of u and/or v, if found, and
change value for u even if v not found in list, and vice versa )
Example: list_swap_val [5;6;7;3] 7 5 => [7;6;5;3]
*)

let rec list_swap_val b u v = 
	let rec swloop b nlst =
		match b with
			| []		-> nlst
			| h::t 		-> 
			if h = u then swloop t (addTail nlst v)
			else if h = v then swloop t (addTail nlst u)
			else swloop t (addTail nlst h) in
	swloop b []
;;
	

(* Write a function index : 'a list -> 'a -> int. Calling index l e
   returns the index in l of the rightmost occurrence of e, or -1 if e
   is not present. The first element has index 0. For example, index
   [1;2;2] 1 = 0 and index [1;2;2;3] 2 = 2 and index [1;2;3] 4 = -1.

   Hint: it's easiest to write a helper function, but you can also do
   it without one.
*)

let rec index l e = 
	let rec findi lst dex i = 
		match lst with
			| []		-> dex 
			| h::t		-> 	if h = e then findi t i (i+1)
							else findi t dex (i+1) in
		findi l (-1) 0
;;

(*
distinct x
'a list -> 'a list
return list of distinct members of list x
*)
(* helper to check if element is in y*)
let rec checky xele y = 
	match y with
		| []		-> 	false
		| h::t		-> 	if h = xele then true 
						else checky xele t
;;

(* helper:  idea to get rid of ele once it has been seen *)
let rec rmv lst e =
	match lst with
		| []	-> []
		| h::t	-> 
		if h = e then rmv t e
		else h::rmv t e
;;

let rec distinct l = 
	match l with
		| []		-> []
		| h::t		->
		if checky h t = true then h::distinct (rmv l h)
		else h::distinct t
;;

(*
find_new x y
'a list -> 'a list -> 'a list
list of members of list x not found in list y
maintain relative order of elements in result
Example: find_new [4;3;7] [5;6;5;3] => [4;7]
*)

let rec find_new x y = 
		match x with
			| []			-> []
			| h1::t1		-> 
			if checky h1 y = true then find_new t1 y
			else h1::find_new t1 y
;;

(*
  power_list l
  int list -> bool
  returns true if each consecutive value in the list is a
  power of the previous element in the list
*)

let rec power_list l = 
	match l with
		| []		-> true
		| h::[]		-> power_list []
		| h1::h2::t	-> 	if h1*h1 = h2 then power_list t
						else false
;;						
