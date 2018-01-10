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

(* Test src_edges g *)
prt_int_list (map (fun { dst = d } -> d) (src_edges 1 g));;
prt_int_list (map (fun { dst = d } -> d) (src_edges 2 g));;
prt_int_list (map (fun { dst = d } -> d) (src_edges 5 g));;
prt_int_list (map (fun { dst = d } -> d) (src_edges 1 g2));;
prt_int_list (map (fun { dst = d } -> d) (src_edges 2 g2));;
prt_int_list (map (fun { dst = d } -> d) (src_edges 3 g2));;
prt_int_list (map (fun { dst = d } -> d) (src_edges 2 g3));;
prt_int_list (map (fun { dst = d } -> d) (src_edges 4 g3));;
prt_int_list (map (fun { dst = d } -> d) (src_edges 1 g3));;

(* Test is_dst (src_edges g) *)
prt_bool_list (map (fun e -> is_dst 2 e) (src_edges 1 g));;
prt_bool_list (map (fun e -> is_dst 2 e) (src_edges 1 g2));;
prt_bool_list (map (fun e -> is_dst 2 e) (src_edges 1 g3));;
