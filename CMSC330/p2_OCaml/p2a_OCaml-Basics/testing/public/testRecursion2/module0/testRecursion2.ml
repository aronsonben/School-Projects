(* Test Recursion 2  *)

#use "testUtils.ml";;
#use "basics.ml";;

let x = [5;6;7;3] ;;
let y = [5;6;7;5] ;;
let z = [7;5;6;5] ;;
let a = [3;5;8;9] ;;

let p = [2;4;16;256;65536] ;;
let b = [3;9;27;81;243] ;;
let c = [2;3;4;5;6] ;;

(* Test index (x, v)	*)
prt_int (index x 5) ;;
prt_int (index x 6) ;;
prt_int (index x 7) ;;
prt_int (index x 3) ;;
prt_int (index z 5) ;;

(* Test find_new (x, y) *)
prt_int_list (find_new x [3]);;
prt_int_list (find_new x [3;5]);;
prt_int_list (find_new x [3;6]);;
prt_int_list_list (find_new [x;y;z] [y]) ;;
prt_int_list_list (find_new [x;y;z] [y;x]) ;;
prt_int_list_list (find_new [x;y;z] [x]) ;;

(* Test is_sorted x *)
prt_bool (power_list p) ;;
prt_bool (power_list b) ;;
prt_bool (power_list c) ;;
prt_bool (power_list a) ;;
