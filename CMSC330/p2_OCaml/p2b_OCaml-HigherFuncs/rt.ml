(*#use "testUtils.ml";;
#use "data.ml";;
*)

(* element not found: return empty list
empty_graph: return empty list *)


let g10 = add_edges 
	[	{src=5;dst=6}; 
		{src=5;dst=4}; 
		{src=6;dst=7};
		{src=7;dst=4};
		{src=4;dst=5}; 
		{src=4;dst=4};
		{src=1;dst=2}; 
		{src=2;dst=4};
		{src=10;dst=0};
		{src=11;dst=1};] empty_graph
;;

let g20 = add_edges 
	[	{src=1;dst=2}; 
		{src=1;dst=3}; 
		{src=2;dst=2}	] empty_graph
;;

(*
let rec my_int_to_list t =
  match t with
      IntLeaf -> []
    | IntNode (y,l,r) ->
      let ls = int_to_list l in
      let rs = int_to_list r in
      append ls (y::rs)
;;

prt_int_list (my_int_to_list (reachable 5 g02));;
*)