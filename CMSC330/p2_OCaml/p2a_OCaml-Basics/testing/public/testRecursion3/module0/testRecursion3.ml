(* Test Recursion 3  *)

#use "testUtils.ml";;
#use "basics.ml";;

let x = [5;6;7;3] ;;
let y = [5;6;7;5] ;;
let z = [7;5;6;5] ;;
let a = [3;5;8;9] ;;

(* Test addTail x y *)
prt_int_list (addTail x 3);;
prt_int_list (addTail [1;2] 3);;
prt_int_list (addTail [1;3] 3);;

(* Test distinct l *)
prt_int_list (distinct x);;
prt_int_list (distinct y);;
prt_int_list (distinct z);;
prt_int_list (distinct a);;
