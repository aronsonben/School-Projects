(* Test Higher Order Functions 0 *)

#use "testUtils.ml";;
#use "data.ml";;

let x = [5;6;7;3] ;;
let y = [5;6;7;5] ;;
let z = [7;5;6;5] ;;
let a = [3;5;8;9] ;;

(* Test count_x x y *)
prt_int (count_x 2 x);;
prt_int (count_x 3 x);;
prt_int (count_x 5 x);;
prt_int (count_x 2 y);;
prt_int (count_x 3 y);;
prt_int (count_x 5 y);;
prt_int (count_x 2 z);;
prt_int (count_x 3 z);;
prt_int (count_x 5 z);;
prt_int (count_x 2 a);;
prt_int (count_x 3 a);;
prt_int (count_x 5 a);;

(* Test div_by_x y *)
prt_bool_list (div_by_x 2 x);;
prt_bool_list (div_by_x 3 x);;
prt_bool_list (div_by_x 5 x);;
prt_bool_list (div_by_x 2 y);;
prt_bool_list (div_by_x 3 y);;
prt_bool_list (div_by_x 5 y);;
prt_bool_list (div_by_x 2 z);;
prt_bool_list (div_by_x 3 z);;
prt_bool_list (div_by_x 5 z);;
prt_bool_list (div_by_x 2 a);;
prt_bool_list (div_by_x 3 a);;
prt_bool_list (div_by_x 5 a);;

(* Test div_by_first y *)
prt_bool_list (div_by_first x);;
prt_bool_list (div_by_first y);;
prt_bool_list (div_by_first z);;
prt_bool_list (div_by_first a);;
