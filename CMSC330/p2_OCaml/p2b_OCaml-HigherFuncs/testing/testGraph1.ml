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

(* Test is_empty g *)
prt_bool (is_empty empty_graph);;
prt_bool (is_empty g);;
prt_bool (is_empty g2);;

(* Test num_nodes g *)
prt_int (num_nodes empty_graph);;
prt_int (num_nodes g);;
prt_int (num_nodes g2);;
prt_int (num_nodes g3);;
