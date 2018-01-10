#use "smallc.ml";;
if (Array.length Sys.argv) < 2 then raise (Error 101);;
let filename = Sys.argv.(1);;
let prg1 = read_lines filename;;
let code = List.fold_left (fun x y->x^y) "" prg1;;	
let t = tokenize code;;
let (a,b)=parse_Function t;;

let rec unparse_list2 = function
    [] -> ""
  | (x::[]) -> unparse2 x
  | (x::xs) -> (unparse2 x) ^ ";" ^ (unparse_list2 xs)
and unparse_data_type = function 
	|Type_Int->"Type_Int"	
and unparse2 = function
  | Id id -> "Id \"" ^ id ^ "\""
  | Num n -> "Num " ^ string_of_int n
  | Paren e->"Paren(" ^ unparse2 e ^")"
  |Fun (Type_Int, "main", List [], List body)->
  		"Fun(Type_Int,\"main\",List[]," ^ "List[" ^ unparse_list2 body ^ "])" 
  | Define (a,b)-> "Define(" ^ unparse_data_type a ^ "," ^ unparse2 b ^ ")"
  | Assign (a,b)->"Assign(" ^ unparse2 a ^ "," ^ unparse2 b ^ ")"
  | Sum (a,b)->"Sum(" ^ unparse2 a ^ "," ^ unparse2 b ^ ")"
  | Greater (a,b)->"Greater(" ^ unparse2 a ^ "," ^ unparse2 b ^ ")"
  | Less (a,b)->"Less(" ^ unparse2 a ^ "," ^ unparse2 b ^ ")"
  | Equal (a,b)->"Equal(" ^ unparse2 a ^ "," ^ unparse2 b ^ ")"
  | Mult (a,b)->"Mult(" ^ unparse2 a ^ "," ^ unparse2 b ^ ")"
  | Pow (a,b)->"Pow(" ^ unparse2 a ^ "," ^ unparse2 b ^ ")"
  | If (cond,e1,e2)->"If(" ^ unparse2 cond ^ "," ^ unparse2 e1 ^ "," ^ unparse2 e2 ^ ")"
  | Print e->"Print(" ^ unparse2 e ^ ")"
  | While (cond, body)-> "While(" ^ unparse2 cond ^ "," ^ unparse2 body ^ ")"
  | List l -> "List[" ^ unparse_list2 l ^ "]"
  |_->"Error"
;;

print_endline (unparse2 a);;
