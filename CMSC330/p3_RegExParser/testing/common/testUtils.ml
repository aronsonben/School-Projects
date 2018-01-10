let rec grader_string_of_int_elements l =
  match l with
    [] -> ""
  | (h::[]) -> Pervasives.string_of_int h
  | (h::t) -> Pervasives.string_of_int h ^ ";" ^ grader_string_of_int_elements t;; 

let grader_string_of_int_list l =
  "[" ^ grader_string_of_int_elements (List.sort compare l) ^ "]";;

let grader_string_of_int_tuple_list(a, b) =
  "(" ^ grader_string_of_int_list a ^ ", " ^ grader_string_of_int_list b ^ ")";;

let rec grader_string_of_bool_elements l =
  match l with
    [] -> ""
  | (h::[]) -> Pervasives.string_of_bool h
  | (h::t) -> Pervasives.string_of_bool h ^ ";" ^ grader_string_of_bool_elements t;; 

let grader_string_of_bool_list l =
  "[" ^ grader_string_of_bool_elements l ^ "]";;

let rec print_clist l = match l with 
    [] -> print_string "]"
  | (h::[]) -> print_char h; print_string "]"
  | (h::t) -> print_char h; print_string ";"; print_clist t;;

let  print_tuple(a, b) = 
  print_string "(["; print_clist(a); print_string ", ["; print_clist(b); print_string ")";;

