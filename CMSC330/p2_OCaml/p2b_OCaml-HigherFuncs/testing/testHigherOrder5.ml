#use "testUtils.ml";;
#use "data.ml";;

let l1 = [1] ;;
let l2 = [1;2] ;;
let l3 = [1;2;5] ;;
let l4 = [3;4] ;;
let l5 = [1;2] ;;
let l6 = [3;4;5];;

let l7 = ["a";"b"];;
let l8 = ["c";"d"];;

let l9 = ["Hello";"Goodbye"] ;;
let l0 = ["Nolan!";"Charles!";"Anwar!";"World!"] ;;


(* Test pair_up *)
prt_int_list_list (pair_up l1 l1);;
prt_int_list_list  (pair_up l2 l2);;
prt_int_list_list (pair_up l3 l4);;
prt_int_list_list (pair_up l5 l6);;

prt_str_list_list (pair_up l7 l8);;
prt_str_list_list (pair_up l9 l0);;
   

