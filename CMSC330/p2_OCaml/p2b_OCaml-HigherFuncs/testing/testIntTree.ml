(* Test Int Tree functions *)

#use "testUtils.ml";;
#use "data.ml";;

let t0 = empty_int_tree;;
let t1 = (int_insert 2 (int_insert 1 t0));;
let t2 = (int_insert 3 t1);;
let t3 = (int_insert 5 (int_insert 3 (int_insert 11 t2)));;

(* Test int_size t *)
prt_int (int_size t0);;
prt_int (int_size t1);;
prt_int (int_size t2);;
prt_int (int_size t3);;

(* Test int_insert_all xs t *)
let x = [5;6;8;3;0] ;;
let z = [7;5;6;5;1] ;;
let t4a = int_insert_all x t1;;
let t4b = int_insert_all z t1;;
prt_bool_list (map (fun y -> int_mem y t4a) x);;
prt_bool_list (map (fun y -> int_mem y t4b) x);;
prt_bool_list (map (fun y -> int_mem y t4a) z);;
prt_bool_list (map (fun y -> int_mem y t4b) z);;

(* Test max_elem t *)
let x = try max_elem t0 with Failure _ -> -1 in
prt_int x;;
prt_int (max_elem t1);;
prt_int (max_elem t2);;
prt_int (max_elem t3);;
