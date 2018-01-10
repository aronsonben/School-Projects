(* Test Higher Order Functions 1  *)

#use "testUtils.ml";;
#use "data.ml";;

let x = [5;6;7;3] ;;
let y = [5;6;7;5] ;;
let z = [7;5;6;5] ;;
let a = [3;5;8;9] ;;

(* Test grow_lists x y	*)
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


(* Test pair_upUp *)
prt_int_list_list (pair_up l1 l1);;
prt_int_list_list  (pair_up l2 l2);;
prt_int_list_list (pair_up l3 l4);;
prt_int_list_list (pair_up l5 l6);;
prt_str_list_list (pair_up l7 l8);;
prt_str_list_list (pair_up l9 l0);;
   


(* Test concat_lists *)
prt_int_list (concat_lists [a;z]) ;; 
prt_int_list (concat_lists [x;a;z]) ;; 
prt_int_list (concat_lists [y;a;a;z]) ;; 
prt_int_list_list (concat_lists [[a];[z]]) ;; 
prt_int_list_list (concat_lists [[a;y];[z;x]]) ;; 
prt_int_list_list (concat_lists [[a;y];[a];[z;x]]) ;; 
