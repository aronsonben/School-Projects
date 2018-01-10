(* bentest.ml - test created by Ben Aronson *)
#use "testUtils.ml"
#use "nfa.ml"

let m = Nfa.make_nfa 0 [2] [(0, None, 1); (0, None, 2); (1, None, 2)];;

(*print_endline ( grader_string_of_int_list (Nfa.e_closure m [1]) ) ;; *)

let m = Nfa.make_nfa 0 [2] [
							(0, Some 'a', 1); (0, Some 'a', 2);
							(1, Some 'a', 2)
							]
;;							
(* print_endline ( grader_string_of_int_list (Nfa.move m [0] 'a') ) ;; *)

let m = Nfa.make_nfa 0 [2] [
							(0, Some 'a', 1); (0, Some 'a', 2);
							(1, Some 'a', 3); (0, Some 'b', 0)
							]
;;							
(* (print_endline ( grader_string_of_int_list (Nfa.move m [0;1] 'a') )) ;; *) 


let test_accept m str =
    print_endline 
	("accept(" ^ str ^ ") = " ^ 
	(string_of_bool (Nfa.accept m str) ))
;;


(* let m = Nfa.make_nfa 0 [1] [(0, None, 1);(1,None,0)];; *)



let m = Nfa.make_nfa 0 [1] [(0, None, 1);(1,None,0)];;

let m = Nfa.make_nfa 0 [1;2] [
							(0, Some 'a', 1);(1, Some 'a', 1);
							(1, Some 'b', 2);(2, Some 'a', 1)
							]
;;
(* test_accept m "ababa" *)



(* 
let m = Nfa.make_nfa 0 [1;2] [
								(0, Some 'a', 1);(1, Some 'a', 1);
								(1, Some 'b', 2)
								]
;;
test_accept m "aab";;

let m = Nfa.make_nfa 0 [2] [(0, Some 'a', 1); (0, Some 'b', 2)];;


*)



(* Nfa.e_closure m [0];;
print_endline ( grader_string_of_int_list (Nfa.e_closure m [0]) ) ;; returned [0;1]*)


let m = Nfa.make_nfa 0 [1] [(0, Some 'a', 0);(0,Some 'a',1)];;
(*print_endline ( grader_string_of_int_list (Nfa.move m [0] 'a') ) ;;*)

let m = Nfa.make_nfa 0 [2] [
							(0, Some 'a', 1); (0, None, 4);
							(1, Some 'a', 2); (1, Some 'b', 3);
							(2, Some 'a', 3); (4, Some 'b', 2)
							]
;;
(* print_endline ( grader_string_of_int_list (Nfa.move m [0] 'a') ) *)



let m = Nfa.make_nfa 0 [1] [(0, Some 'a', 1)];;
(*test_accept m "a";;*)


(*   stats    *)
let rec string_of_int_tuple_list l =
  let string_of_int_tuple (a,b) = "(" ^ string_of_int a ^ "," ^ string_of_int b ^ ")" in
    let rec string_of_elements l = match l with
      [] -> ""
      | (h::[]) -> string_of_int_tuple h
      | (h::t) -> string_of_int_tuple h ^ ";" ^ string_of_elements t
    in "[" ^ string_of_elements l ^ "]"
;;

let string_of_stats_tuple (a,b,c) = "(" ^ string_of_int a ^ "," ^ string_of_int b ^ "," ^ string_of_int_tuple_list c ^ ")";;

let m = Nfa.make_nfa 0 [1] [(0, Some 'a', 1)];;
(*print_endline ( string_of_stats_tuple (Nfa.stats m))*)

let r = Nfa.Concat(Nfa.Char('a'),Nfa.Concat(Nfa.Char('a'),Nfa.Char('b')));;


let r = Nfa.Star(Nfa.Union(Nfa.Char('a'),Nfa.Empty_String));;

let r = Nfa.Concat( Nfa.Union(Nfa.Char('a'), Nfa.Char('b')) , Nfa.Char('c'));;
(* print_endline (Nfa.regexp_to_string r) ;; *)




let test_accept2 m str =
    print_endline 
        ("accept(" ^ str ^ ") = " ^ 
        (string_of_bool (Nfa.accept m str) ))
;;


let r = Nfa.Union(Nfa.Char('a'),Nfa.Char('b'));;


let r = Nfa.Concat(Nfa.Char('a'),Nfa.Char('b'));;


let r = Nfa.Star(Nfa.Char('a'));;
(*test_accept2 m "a";;*)

(* 
test_accept m "";;
test_accept m "a";;
test_accept m "b";;
test_accept m "ba";; *)

(* Union(a,b) test 
let m = Nfa.make_nfa 0 [1] [(0,None,2);(0,None,3);(2,Some 'a',4);
							(3,Some 'b',5);(4,None,1);(5,None,1);
							]
;;
*)
(* print_endline ( grader_string_of_int_list (Nfa.e_closure m [4]) ) ;; *)
(* print_endline ( grader_string_of_int_list (Nfa.move m [1;4] 'a') );; *) 
(* test_accept m "a";;*)



let m = Nfa.make_nfa 0 [2;3] [
							(0, None, 2); (2, Some 'a', 3);
							(2, Some 'b', 1); (1, None, 3);
							(3, Some 'a', 3)
							]
;;



let m = Nfa.make_nfa 0 [1] [
							(0,None,2);(2,None,4);(2,None,5);
							(4,Some 'a',6);(5,Some 'b', 7);
							(6,None,3);(7,None,3);(3,None,1);
							(0,None,1);(1,None,0)
							]
;;
(* print_endline ( grader_string_of_int_list (Nfa.e_closure m [6]) ) ;; *)
(* print_endline ( grader_string_of_int_list (Nfa.move m (Nfa.e_closure m [6]) 'a') );; *)
(*test_accept m "ab";;*)

let r = Nfa.Star(Nfa.Union(Nfa.Char('a'),Nfa.Char('b')));;

let m = Nfa.make_nfa 0 [7] [
							(0,None,1);(1,None,2);(1,None,4);
							(2,Some 'a',3);(4,Some 'c', 5);
							(3,None,6);(5,None,6);(6,None,1);
							(1,None,6);(6,Some 'b',7)
							]
;;
(* test_accept m "cb";;  *)

let m = Nfa.make_nfa 0 [1] [(0,None,1);(1,None,0)];;

let m = Nfa.make_nfa 0 [3;4] [(1,Some 'a',2);(2,None,0);(0,None,1);
							(0,Some 'b',3);(0,Some 'c',4);(4,None,4);
							(4,None,2);(3,None,4)
							]
;;

let m = Nfa.make_nfa 0 [0;1;2] [(0,Some 'a',0);(1,Some 'a',0)];;

let r = Nfa.Union( Nfa.Char('a'), Nfa.Union(Nfa.Char('b'),
		Nfa.Union(Nfa.Char('c'),Nfa.Star(Nfa.Char('d')))))
;;

let r = Nfa.Concat( Nfa.Star(Nfa.Union(Nfa.Char('a'),Nfa.Char('b'))),
			Nfa.Concat(Nfa.Char('a'),Nfa.Concat(Nfa.Char('a'),Nfa.Char('b'))))
;;
let m = Nfa.regexp_to_nfa r;;

let m = Nfa.make_nfa 0 [3] [(0,Some 'a',0);(0,Some 'b',0);
							(0,Some 'a',1);(1,Some 'b',2);
							(2,Some 'b',3)
							]
;;
(* print_endline ( grader_string_of_int_list (Nfa.e_closure m [0;2]) ) ;; 
print_endline ( grader_string_of_int_list (Nfa.move m [0;1] 'b') );;
test_accept m "babaabb";; *)

let m = Nfa.make_nfa 0 [2] [(0, Some 'a', 1); (0, Some 'b', 2)];;
test_accept m "";;
test_accept m "a";;
test_accept m "b";;
test_accept m "ba";;









