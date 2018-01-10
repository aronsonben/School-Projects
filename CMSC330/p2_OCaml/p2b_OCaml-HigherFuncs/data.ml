(*-------------------------------------------------------------------------*)
(* functions from lecture you may use *)

#use "funs.ml";;

(* ===== *)
(* Part 1: Higher order functions *)
(* ===== *)

(* 
count_x x y 
'a -> 'a list -> int
returns how many times x occurs in y 
Example: count_x 3 [1;3;1;1;3] = 2, count_x "hello" ["there";"ralph"] = 0
*)

(* helper to check *)
let checkx x a h = if x = h then a+1 else a;;

let count_x x y = fold (checkx x) 0 y;;

(*
div_by_x x y
int -> int list -> bool list
return a list of booleans, each one indicating whether the correponding element in y is divisible by x
Example: div_by_x 2 [1;3;4;8;0] = [false;false;true;true;true]
*)


(**** MAKE SURE IF X IS 0 IT ADDS FALSE TO LIST ****)

(* helper to check div *)
let checkdiv x h = if x == 0 then false else if h mod x == 0 then true else false;;

let div_by_x x y = map (checkdiv x) y;;

(*
div_by_first y
int list -> bool list
return a list of booleans, each one indicating whether the correponding element in y is divisible by the first element of y
Example: div_by_first [2;3;4;8;0] = [true;false;true;true;true] and div_by_first [] = []
*)

let div_by_first y =
	match y with
	[]		-> []
	|h::t 	-> div_by_x h y
;;



(* 
pair_up x y 	 
'a list -> 'a list -> 'a list list  	 
a list of lists, where each element of x paired with each element of y
resulting lists must be in same order as in x
Example: pairup [] [] = []
pairup [1;2] [3;4;5]= [[1; 3]; [1; 4]; [1; 5]; [2; 3]; [2; 4]; [2; 5]]
*)

(* helper *)
let rec pairuphelp lst ele =
	match lst with
		[]		-> []
		|h::t	-> ([ele;h])::(pairuphelp t ele)
;;

let pair_up x y =
  let l = map (pairuphelp y) x in
  fold (append) [] l
;;

(* 
concat_lists x 	
'a list list -> 'a list 	
return a list consisting of the lists in x concatenated together
(note just top level of lists is concatenated, unlike List.flatten)
Examples: concat_lists [[1;2];[7];[5;4;3]] = [1;2;7;5;4;3]
concat_lists [[[1;2;3];[2]];[[7]]] = [[1;2;3];[2];[7]]
*)
let concat_lists x =
  match x with
	|[]		-> []
	|h::t	-> fold (append) h t

    
(* ===== *)
(* Part 2a: Programming with datatypes -- binary tree of integers *)
(* ===== *)
    
(* The following is an implementation of binary search trees whose nodes contain integeres *)

(* The type of a tree is a datatype: 
   a tree is either empty (just a leaf), or
   it is a node containing an integer, a left subtree, and a right subtree *)
type int_tree =
    IntLeaf 
  | IntNode of int * int_tree * int_tree

(* An empty tree is a leaf *)
let empty_int_tree = IntLeaf

(* Inserting x into tree t: 
   if the tree is empty, then adding x produces a single-node tree
   if x is greater than the value at the current node, 
     return a tree whose right subtree is replaced by the tree produced by
       inserting x into the current right subtree
   if x is already in the tree, then return the tree unchanged
   if x is less than the value at the current node, 
     do the opposite of when x was greater (i.e., insert in the left subtree)
*)
let rec int_insert x t =
  match t with
      IntLeaf -> IntNode(x,IntLeaf,IntLeaf)
    | IntNode (y,l,r) when x > y -> IntNode (y,l,int_insert x r)
    | IntNode (y,l,r) when x = y -> t
    | IntNode (y,l,r) -> IntNode(y,int_insert x l,r)

(* Checking whether x occurs in tree t: 
   Follow the same sort of procedure as insertion, but return true if x is in the tree
   and false otherwise
*)
let rec int_mem x t =
  match t with
      IntLeaf -> false
    | IntNode (y,l,r) when x > y -> int_mem x r
    | IntNode (y,l,r) when x = y -> true
    | IntNode (y,l,r) -> int_mem x l

(* Implement the following functions, operating on trees *)

(* 
int_size t 
int_tree -> int
returns how many nodes are in the tree
Example: int_size empty_int_tree = 0, int_size (int_insert 1 (int_insert 2 empty_int_tree)) = 2 
*)
let rec int_size t =
	match t with
		|IntLeaf				-> 0
		|IntNode (y,l,r)		-> 1 + int_size l + int_size r
;;

(* 
int_to_list t 
int_tree -> int list
returns a list of all values in the tree, resulting from an in-order traversal
Examples: int_to_list (int_insert 2 (int_insert 1 empty_int_tree)) = [1;2]
int_to_list (int_insert 2 (int_insert 2 (int_insert 3 empty_int_tree))) = [2;3]
*)    
let rec int_to_list t =
  match t with
		| IntLeaf 			-> []
		| IntNode (y,l,r)	-> (append) (int_to_list l) (y::(int_to_list r))
;;  

(* 
int_insert_all xs t 
int list -> int_tree -> int_tree
returns a tree t' that is the same as t but has all integers in xs added to it
Examples: int_to_list (int_insert_all [1;2;3] empty_int_tree) = [1;2;3]
Note: Try to use fold to implement this function on one line
*)    
let rec int_insert_all xs t = fold_right int_insert (rev xs) t;;


(* 
max_elem t 
int_tree -> int
returns the maximum element in the tree
  throws exception Failure "max_elem" if the tree is empty
Example: max_elem (int_insert_all [1;2;3] empty_int_tree) = 3
Note: This should take time O(height of the tree)
*)    
let rec max_elem t =
	match t with
		| IntLeaf		-> 	failwith "max_elem"
		| IntNode(y,l,r)-> 	if r == IntLeaf then y
							else max_elem r
;;

(* lowest common ancestorof x y in int_tree t 
throws exception Failure "common" for an empty tree, or x,y does not exists  *)

(* helper *)


let rec common t x y = 
	match t with
		| IntLeaf		-> failwith "common"
		| IntNode(i,l,r)-> 	
			if (x = i) && (y = i) then i
			else if (x = i) || (y = i) then i
			else if (int_mem x l) == true && (int_mem y l) == true then 
				common l x y
			else if (int_mem x r) == true && (int_mem y r) == true then 
				common r x y
			else if ( ( (int_mem x l) == true && (int_mem x r) == false ) ||
						( (int_mem y l) == false && (int_mem y r) == true ) ) ||
					( ( (int_mem y l) == true && (int_mem y r) == false ) ||
						( (int_mem x l) == false && (int_mem x r) == true ) )
					then i
			else failwith "common"
;;

	
(* ===== *)
(* Part 2b: Programming with datatypes -- polymorphic binary tree *)
(* ===== *)

(* The previous part defined a binary tree over only integers. But we
   should be able to define a binary tree over any kind of data, as
   long as it is totally ordered. We capture this idea with the
   following data type definitions.
*)

(* This says, as before, that a tree is either a leaf or a node, but
   now the node may contain a value of any type 'a, not just ints. *)
type 'a atree =
    Leaf
  | Node of 'a * 'a atree * 'a atree

(* Since a tree may contain values of any type, we need a way to
   compare those values. For this purpose, we define the type of
   comparison functions: they take two values of type 'a and return an
   int. If the returned value is negative, then the first value is less
   than the second; if positive, then the first is greater; if 0, then
   the two values are equal. *)

type 'a compfn = 'a -> 'a -> int 

(* can use this for testing purposes *)
let comp a b =
	if a < b then -1
	else if a > b then 1
	else 0
;;

(* Finally: a polymorphic binary tree: This definition bundles the
   tree with its comparison function so that the latter can be used when needed
   by the tree's functions, pinsert and pmem, below. *)
type 'a ptree = 'a compfn * 'a atree

(* An empty tree is a leaf and a comparison function *)
let empty_ptree f : 'a ptree = (f,Leaf)

(*
pinsert x t 
'a -> 'a ptree -> 'a ptree
returns a tree t' that is the same as t but has x added to it
*)

(* print helper *)
let rec print_ptree ((f,t):'a ptree) = 
	match t with
		| Leaf -> print_string "Leaf\n"
		| Node (n, left, right) ->
			print_int n; print_string "\n";
			print_string "Left: "; print_ptree (f,left);
			print_string "Right: "; print_ptree (f,right)
;;

let rec pinsert x ((f,t):'a ptree) =
	match t with
		| Leaf 			-> ((f,Node( x, Leaf, Leaf)):'a ptree)
		| Node(y,l,r) 	when f x y > 0 -> 
						let (fn,rnew) = pinsert x ((f,r):'a ptree) in
						( (fn, Node(y,l,rnew)):'a ptree )
		| Node(y,l,r)	when f x y = 0 -> ((f,t):'a ptree)
		| Node(y,l,r)	-> 
						let (fn,lnew) = pinsert x ((f,l):'a ptree) in
						( (fn, Node(y,lnew,r)):'a ptree )
;;
 
 
(*
pmem x t 
'a -> 'a ptree -> bool
returns whether x appears in tree t
*)   
let rec pmem x ((f,t):'a ptree) =
	match t with
		| Leaf			-> false
		| Node(y,l,r)	when f x y > 0 -> pmem x ((f,r):'a ptree)
		| Node(y,l,r)	when f x y = 0 -> true
		| Node(y,l,r)	-> pmem x ((f,l):'a ptree)


(* int_mem
let rec int_mem x t =							
 match t with
    IntLeaf -> false
  | IntNode (y,l,r) when x > y -> int_mem x r
  | IntNode (y,l,r) when x = y -> true
  | IntNode (y,l,r) -> int_mem x l
 
:: takes in (int * int_tree)
 :: match (t:int_tree) with:
	:: ILeaf ->
		:: If 't' is an IntLeaf return FALSE
	:: INode(y,l,r) ->
		:: 	1) x > y? 
				:: YES: recurse on right branch
				:: NO: next case
		::	2) x = y?
				:: YES: {found} - return TRUE
				:: NO: next case
		::	3) x < y? (-implicit-)
				:: YES: recurse on left branch
				:: NO: (invalid)
;;
*)  
   
(* Examples:

let t0 = empty_ptree (fun x y -> if x < y then -1 else if x > y then 1 else 0);;
let t1 = pinsert 1 (pinsert 8 (pinsert 5 t0));;
pmem 5 t0 = false;;
pmem 5 t1 = true;;
pmem 1 t1 = true;;
pmem 2 t1 = false;;

*)
     
(* ===== *)
(* Part 3: Programming with records -- graphs *)
(* ===== *)

(* A graph is a set of nodes, represented as an int_tree, and a list
   of edges.  A node is represented as an integer, and an edge is a
   record identifying its source and destination nodes. *)
type node = int;;
type edge = { src: node; dst: node; };;
type graph = { nodes: int_tree; edges: edge list };;

(* an empty graph (has type graph) *)
let empty_graph = {nodes = empty_int_tree; edges = [] }

(* 
add_edge e g 
edge -> graph -> graph
returns a new graph that is the same as g, but with e added
Note: does not worry about duplicate edges
*)
let add_edge ({ src = s; dst = d } as e) { nodes = ns; edges = es } =
  let ns' = int_insert s ns in
  let ns'' = int_insert d ns' in
  let es' = e::es in
  { nodes = ns''; edges = es' }

(* 
add_edges es g 
edge list -> graph -> graph
returns a new graph that is the same as g, but with all edges in es added
Note: does not worry about duplicate edges
*)
let add_edges es g =
  fold (fun g e -> add_edge e g) g es

(* IMPLEMENT THE FOLLOWING *)
    
(* 
is_empty g 
graph -> bool
returns whether the graph is empty
Example: is_empty empty_graph = true
is_empty (add_edge {src=1; dst=2} empty_graph) = false
*)      
let is_empty g = if g.nodes = IntLeaf then true else false

(* 
num_nodes g 
graph -> int
returns the number of nodes that appear in g
Example: num_nodes (add_edge {src=1; dst=2} empty_graph) = 2
Example: num_nodes (add_edge {src=1; dst=1} empty_graph) = 1
*)
let num_nodes g = int_size g.nodes

(* 
is_dst x e 
node -> edge -> bool
returns true if x is the destination of the given edge
Example: is_dst 1 { src=1; dst = 2 } = false
is_dst 2 {src = 1; dst = 2 } = true
*)    
let is_dst x e = if e.dst = x then true else false
    
(* 
src_edges x g
node -> graph -> edge list
returns those edges in g whose source node is x
Example: 
src_edges 1 
  (add_edges [{src=1;dst=2}; {src=1;dst=3}; {src=2;dst=2}] empty_graph) =
[{src=1;dst=2}; {src=1;dst=3}]
*)    

(* CHECK THIS REV *)

let src_edges x g =
	let rec sehelp x ge =
		match ge with
			| []	-> []
			| h::t	-> if h.src = x then h::(sehelp x t)
						else sehelp x t in
	rev (sehelp x g.edges)
;;

(* 
reachable n g
node -> graph -> int_tree
returns a set of nodes reachable from n, in g, where the set is represented as an int_tree
Example: 
int_to_list 
  (reachable 1 
    (add_edges [{src=1;dst=2}; {src=1;dst=3}; {src=2;dst=2}] empty_graph)) =
[1;2;3];;
int_to_list 
  (reachable 3 
    (add_edges [{src=1;dst=2}; {src=1;dst=3}; {src=2;dst=2}] empty_graph)) =
[3]
*)  

(* get only the dist from src_edges edge list *)
let rec getdsts elst =
	match elst with
		| []	-> []
		| h::t	-> h.dst::(getdsts t)
;;

(* helper to check if a dst is already visited *)
let rec vcheck ele visited =
	match visited with
		| []	-> 	false
		| h::t	-> 	if h = ele then true
					else vcheck ele t 
;;

let rec reachhelp t v q g = 
	match q with
		| []		-> v
		| h1::t1 	-> 
		if vcheck h1 v then reachhelp h1 v t1 g
		else reachhelp (h1) (h1::v) ( append t1 ( getdsts (src_edges h1 g) ) ) g
;;

let reachable n g =
	if is_empty g then empty_int_tree
	else if int_mem n g.nodes = false then empty_int_tree
	else
		let q = getdsts (src_edges n g) in  
		let v = n::[] in
		let nodelist = rev (reachhelp n v q g) in
		if nodelist = [] then empty_int_tree
		else int_insert_all (nodelist : int list) empty_int_tree
		
;;







