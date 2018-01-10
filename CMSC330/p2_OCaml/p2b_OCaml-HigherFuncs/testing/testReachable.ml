#use "testUtils.ml";;
#use "data.ml";;

let g = add_edges
  [ { src = 1; dst = 2; };
    { src = 2; dst = 3; };
    { src = 3; dst = 4; };
    { src = 4; dst = 5; } ] empty_graph
;;

let g2 = add_edges
  [ { src = 1; dst = 2; };
    { src = 3; dst = 4; };
    { src = 4; dst = 3; } ] empty_graph
;;

let g3 = add_edges
  [ { src = 1; dst = 2; };
    { src = 1; dst = 3; };
    { src = 3; dst = 2; };
    { src = 2; dst = 1; } ] empty_graph
;;

let rec my_int_to_list t =
  match t with
      IntLeaf -> []
    | IntNode (y,l,r) ->
      let ls = int_to_list l in
      let rs = int_to_list r in
      append ls (y::rs)
;;

prt_int_list (my_int_to_list (reachable 1 g));;
prt_int_list (my_int_to_list (reachable 1 g2));;
prt_int_list (my_int_to_list (reachable 1 g3));;
prt_int_list (my_int_to_list (reachable 5 g3));;
