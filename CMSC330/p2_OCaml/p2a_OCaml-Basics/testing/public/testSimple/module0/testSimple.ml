(* Test basics *)

#use "testUtils.ml";;
#use "basics.ml";;

(* Test head_divisor *)
prt_bool (head_divisor []);;
prt_bool (head_divisor [2;3]);;
prt_bool (head_divisor [2;4;6;8]);;
prt_bool (head_divisor [3;6;9;16]);;

(* Test tuple_addr *)
prt_int (tuple_addr (1,2,3));;
prt_int (tuple_addr (30,40,50));;

(* Test caddr_int x *)
prt_int (caddr_int [1]);;
prt_int (caddr_int [1;2]);;
prt_int (caddr_int [1;2;1;1]);;
