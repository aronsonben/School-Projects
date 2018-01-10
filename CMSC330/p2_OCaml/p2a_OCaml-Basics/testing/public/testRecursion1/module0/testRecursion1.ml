(* Test recursion  1 *)

#use "testUtils.ml";;
#use "basics.ml";;

let x = [5;6;7;3] ;;

(* Test get_val x n 	*)
prt_int (get_val x 0);;
prt_int (get_val x 1);;
prt_int (get_val x 2);;
prt_int (get_val x 3);;

(* Test get_vals x y *)
prt_int_list (get_vals x [0]);;
prt_int_list (get_vals x [1]);;
prt_int_list (get_vals x [3]);;
prt_int_list (get_vals x [0;2]);;
prt_int_list (get_vals x [0;2;1]);;
prt_int_list (get_vals x [0;2;1;3]);;

(* Test list_swap_val b u v *)
prt_int_list (list_swap_val x 7 5) ;;
prt_int_list (list_swap_val x 6 5) ;;
prt_int_list (list_swap_val x 3 5) ;;
prt_int_list (list_swap_val x 7 3) ;;

