(* Test Int Tree functions *)

#use "testUtils.ml";;
#use "data.ml";;

(* Test pinsert and pmem together *)

(* Make some trees *)
let t0 = empty_ptree Pervasives.compare;;
let t1 = (pinsert 2 (pinsert 1 t0));;
let t2 = (pinsert 3 t1);;
let t3 = (pinsert 5 (pinsert 3 (pinsert 11 t2)));;

(* See what's in them *)
let x = [5;6;8;3;11;7;2;6;5;1] ;;
prt_bool_list (map (fun y -> pmem y t0) x);;
prt_bool_list (map (fun y -> pmem y t1) x);;
prt_bool_list (map (fun y -> pmem y t2) x);;
prt_bool_list (map (fun y -> pmem y t3) x);;

(* Try with strings. We consider strings with the same length as equal. 
  So I could put a string of length 5 in the tree, and then any other
  string of length 5 is also considered present there. *)
let strlen_comp x y = Pervasives.compare (String.length x) (String.length y);;
let t0 = empty_ptree strlen_comp;;
let t1 = (pinsert "hello" (pinsert "bob" t0));;
let t2 = (pinsert "sidney" t1);;
let t3 = (pinsert "yosemite" (pinsert "ali" (pinsert "alice" t2)));;

(* See what's in them *)
let x = ["hello"; "bob"; "sidney"; "kevin"; "james"; "ali"; "alice"; "xxxxxxxx"];;
prt_bool_list (map (fun y -> pmem y t0) x);;
prt_bool_list (map (fun y -> pmem y t1) x);;
prt_bool_list (map (fun y -> pmem y t2) x);;
prt_bool_list (map (fun y -> pmem y t3) x);;
