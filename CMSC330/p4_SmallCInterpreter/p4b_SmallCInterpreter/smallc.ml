(*  
	CMSC330 Fall 2016
	This ocaml code reads a C code and properly indents it
	
	compile for debug:
		ocamlc -g Str.cma smallc.ml 
	;
	@author: Ben Aronson
	@date: 10/19/2016
*)

#load "str.cma"

type data_type =
	|Type_Int
;;

(* Use this as your abstract syntax tree *)

type ast =
  | Id of string
  | Num of int
  | Define of data_type * ast
  | Assign of ast * ast
  | List of ast list
  | Fun of data_type * string * ast * ast   (* return type * function name * argument list * statement list *)
  | Sum of ast * ast
  | Greater of ast * ast
  | Equal of ast * ast
  | Less of ast * ast
  | Mult of ast * ast
  | Pow of  ast * ast
  | Print of ast
  | If of ast * ast * ast	(* cond * if brach * else branch *)
  | While of ast * ast
  | Paren of ast
  
;;

type token =
 | Tok_Id of string
 | Tok_Num of int
 | Tok_String of string
 | Tok_Assign
 | Tok_Greater
 | Tok_Less
 | Tok_Equal
 | Tok_LParen
 | Tok_RParen
 | Tok_Semi
 | Tok_Main
 | Tok_LBrace
 | Tok_RBrace
 | Tok_Int 
 | Tok_Float
 | Tok_Sum
 | Tok_Mult
 | Tok_Pow
 | Tok_Print
 | Tok_If
 | Tok_Else
 | Tok_While
 | Tok_END
 
(* tokens *)
let re_lparen = Str.regexp "("
let re_rparen = Str.regexp ")"
let re_lbrace = Str.regexp "{"
let re_rbrace = Str.regexp "}"
let re_assign = Str.regexp "="
let re_greater = Str.regexp ">"
let re_less = Str.regexp "<"
let re_equal = Str.regexp "=="
let re_semi = Str.regexp ";"
let re_int = Str.regexp "int"
let re_float = Str.regexp "float"
let re_printf = Str.regexp "printf"
let re_main = Str.regexp "main"
let re_id = Str.regexp "[a-zA-Z][a-zA-Z0-9]*"
let re_num = Str.regexp "[-]?[0-9]+"
let re_string = Str.regexp "\"[^\"]*\""
let re_whitespace = Str.regexp "[ \t\n]"
let re_add = Str.regexp "+"
let re_mult = Str.regexp "*"
let re_pow = Str.regexp "\\^"
let re_if = Str.regexp "if"
let re_else = Str.regexp "else"
let re_while = Str.regexp "while"


exception Lex_error of int
exception Parse_error of int ;;
exception IllegalExpression of string

let tokenize s =
 let rec tokenize' pos s =
   if pos >= String.length s then
     [Tok_END]
   else begin
     if (Str.string_match re_lparen s pos) then
       Tok_LParen::(tokenize' (pos+1) s)
     else if (Str.string_match re_rparen s pos) then
       Tok_RParen::(tokenize' (pos+1) s)
     else if (Str.string_match re_add s pos) then
       Tok_Sum::(tokenize' (pos+1) s)
     else if (Str.string_match re_mult s pos) then
       Tok_Mult::(tokenize' (pos+1) s)
     else if (Str.string_match re_equal s pos) then
       Tok_Equal::(tokenize' (pos+2) s)
     else if (Str.string_match re_if s pos) then
       Tok_If::(tokenize' (pos+2) s)
     else if (Str.string_match re_else s pos) then
       Tok_Else::(tokenize' (pos+4) s)    
     else if (Str.string_match re_while s pos) then
       Tok_While::(tokenize' (pos+5) s)       
	else if (Str.string_match re_pow s pos) then
       Tok_Pow::(tokenize' (pos+1) s)
    else if (Str.string_match re_printf s pos) then
       Tok_Print::tokenize' (pos+6) s
    else if (Str.string_match re_lbrace s pos) then
       Tok_LBrace::(tokenize' (pos+1) s)
    else if (Str.string_match re_rbrace s pos) then
       Tok_RBrace::(tokenize' (pos+1) s)
    else if (Str.string_match re_assign s pos) then
       Tok_Assign::(tokenize' (pos+1) s)
    else if (Str.string_match re_greater s pos) then
       Tok_Greater::(tokenize' (pos+1) s)
    else if (Str.string_match re_less s pos) then
       Tok_Less::(tokenize' (pos+1) s)
    else if (Str.string_match re_semi s pos) then
       Tok_Semi::(tokenize' (pos+1) s)
    else if (Str.string_match re_int s pos) then
       Tok_Int::(tokenize' (pos+3) s)
    else if (Str.string_match re_float s pos) then
       Tok_Float::(tokenize' (pos+5) s)
    else if (Str.string_match re_main s pos) then
       Tok_Main::(tokenize' (pos+4) s)
     else if (Str.string_match re_id s pos) then
       let token = Str.matched_string s in
       let new_pos = Str.match_end () in
       (Tok_Id token)::(tokenize' new_pos s)
     else if (Str.string_match re_string s pos) then
       let token = Str.matched_string s in
       let new_pos = Str.match_end () in
       let tok = Tok_String (String.sub token 1 ((String.length token)-2)) in
       tok::(tokenize' new_pos s)
     else if (Str.string_match re_num s pos) then
       let token = Str.matched_string s in
       let new_pos = Str.match_end () in
       (Tok_Num (int_of_string token))::(tokenize' new_pos s)
     else if (Str.string_match re_whitespace s pos) then
       tokenize' (Str.match_end ()) s
     else
       raise (Lex_error pos)
   end
 in
 tokenize' 0 s
 
 
 (* C Grammar *)
 (* 
 
 basicType-> 'int'
  mainMethod-> basicType 'main' '(' ')' '{' methodBody '}'
  methodBody->(localDeclaration | statement)*
  localDeclaration->basicType ID ';'
  statement->
    whileStatement
    |ifStatement
    |assignStatement
    |printStatement
  
  assignStatement->ID '=' exp ';'
  ifStatement -> 'if' '(' exp ')'  '{' ( statement)* '}'  ( 'else' '{'( statement)* '}')?
  whileStatement -> 'while''(' exp ')' '{'(statement )*'}'
  printStatement->'printf' '(' exp ')' ';'
  exp -> additiveExp (('>'  | '<'  | '==' ) additiveExp )*
  additiveExp -> multiplicativeExp ('+' multiplicativeExp)*
  multiplicativeExp-> powerExp ( '*' powerExp  )*
  powerExp->primaryExp ( '^' primaryExp) *
  primaryExp->'(' exp ')' | ID 
  ID->( 'a'..'z' | 'A'..'Z') ( 'a'..'z' | 'A'..'Z' | '0'..'9')*
  WS-> (' '|'\r'|'\t'|'\n') 



*)

(***** DELETE!! *****)
let tok_to_str t = ( match t with
          Tok_Num v -> string_of_int v
        | Tok_Sum -> "+"
        | Tok_Mult ->  "*"
        | Tok_LParen -> "("
        | Tok_RParen -> ")"
		| Tok_Pow->"^"
        | Tok_END -> "END"
        | Tok_Id id->id
		| Tok_String s->s
		| Tok_Assign->"="
		 | Tok_Greater->">"
		 | Tok_Less->"<"
		 | Tok_Equal->"=="
		 | Tok_Semi->";"
		 | Tok_Main->"main"
		 | Tok_LBrace->"{"
		 | Tok_RBrace->"}"
		 | Tok_Int->"int" 
		 | Tok_Float->"float"
		 | Tok_Print->"printf"
		 | Tok_If->"if"
		 | Tok_Else->"else"
		 | Tok_While-> "while"
    )

let print_token token =
	print_string "Input token = " ;
	print_string (tok_to_str token);
	print_endline ""
;;

let print_token_list tokens =
	print_string "Input token list = " ;
	List.iter (fun x -> print_string (" " ^ (tok_to_str x))) tokens;
	print_endline ""
;;
(********************)


(*----------------------------------------------------------
  function lookahead : token list -> (token * token list)
	Returns tuple of head of token list & tail of token list
*)

let lookahead tok_list = match tok_list with
        [] -> raise (IllegalExpression "lookahead")
        | (h::t) -> (h,t)
;;        

(* -------------- Your Code Here ----------------------- *)
(* Begin helpers *)

(* 
function match_tok: token -> token list -> token 
	Matches the first argument with the head of the token list;
	if they match, returns the tail of the token list
	otherwise throws an exception
Note: Anwar's Github structure - need to edit this 
*)
let match_tok token tok_list =
	match tok_list with
		| (h::t) when token = h -> t
		| _ -> raise (IllegalExpression "match_tok")
;;





(* Method to parse anything inside brackets *)


(* 
match l9 with
			|[]		-> (Fun(e1,main,List [], e3),lst)
			|_		-> raise (IllegalExpression "pFunc-ended too soon")

let (e2,lst2) = parse_main lst1 in *)
(* Below is just to catch errors at the end--parse_end finlst; 
*)

(******* PARSE FUNCTION ******)
(*token list -> ast * token list *)
let rec parse_Function lst =
	(* Parse basicType, "main(){", methodBody, and "}END" *)
	let (e1,l1) = parse_basicType lst in
	let (e2,l2) = lookahead l1 in	
	match e2 with						(* "main" *)
	|Tok_Main	-> 
		let main = tok_to_str e2 in (* Tok_Main -> "main" *)
		let l3 = match_tok (Tok_LParen) l2 in	(* ( *)
		let l4 = match_tok (Tok_RParen) l3 in	(* ) *)
		let l5 = match_tok (Tok_LBrace) l4 in	(* { *)
		
		let (e3,l6) = parse_methodBody l5 [] in
		
		let (bracelook,_) = lookahead l6 in		
		let l7 = match_tok (bracelook) l6 in	(* } *)
		let l9 = match_tok (Tok_END) l7 in		(*END*)
		check_end l9;
		(* Will only reach this far if it hasn't thrown exception *)
		(Fun(e1,main,List [], e3),lst)
	| _			-> raise (IllegalExpression "not main")	
and check_end l9 =
	match l9 with
	|[]	-> ()
	|_	-> raise (IllegalExpression "check_end error-still have toks")
and parse_basicType toklist = 
	let (look,rest) = lookahead toklist in
	match look with
		|Tok_Int 	-> (Type_Int,rest)
		|_			-> raise (IllegalExpression "parse_basicType")
and parse_methodBody lst fresh =
	let (look,rest) = lookahead lst in
	match look with
	(* "}"=end of body | "int"=assign | "_"=statement (for now) *)
	|Tok_RBrace	-> 
		(* If hit RBrace, end methodBody *)
		if fresh = [] then (List[],lst)
		else let freshrev = (List.rev fresh) in (List(freshrev),lst)
	|Tok_Int	-> (* localDeclaration *)
		let (define,r1) = parse_localDeclaration lst in
		parse_methodBody r1 (define::fresh)
	|Tok_Id s 		-> (* assign statement *)
		let (asgn,r2) = parse_statement lst in 
		parse_methodBody r2 (asgn::fresh)
	|Tok_If			-> (* if statement *)
		let (ifst,r3) = parse_statement lst in 
		parse_methodBody r3 (ifst::fresh)
	|Tok_While		-> (* while statement *)
		let (whst,r5) = parse_statement lst in 
		parse_methodBody r5 (whst::fresh)
	|Tok_Print		-> (* print statement *)
		let (prnt,r6) = parse_statement lst in 
		parse_methodBody r6 (prnt::fresh)
	|_				-> raise (IllegalExpression "statement")
	
and parse_localDeclaration lst =
	(* e1 = Type_Int | e2 = Id "a" | e3 = ';' *)
	let (e1,r1) = parse_basicType lst in
	let (e2,r2) = parseID r1 in
	let (e3,r3) = lookahead r2 in
	match e3 with
	|Tok_Semi 	-> (Define(e1,e2),r3)
	|_			-> raise (IllegalExpression "localDeclaration")
and parse_statement lst = 
	(* Could be: ID, If, While, Print - need to match these cases *)
	let (look,rest) = lookahead lst in
	match look with
	|Tok_Id s 		-> parse_assign s rest		
	|Tok_If			-> parse_if lst					(* if *)
	|Tok_While		-> parse_while lst				(* while *)
	|Tok_Print		-> parse_print lst				(* print *)
	|_				-> raise (IllegalExpression "statement")
and parse_assign id lst = 
	(* passing ID in for now *)
	let (assigntok,_) = lookahead lst in
	let r1 = match_tok (assigntok) lst in	(* consume ID *)
	let (e1,r2) = parseExp r1 in 		
	let (look,r3) = lookahead r2 in			(* consume ';' *)
	match look with
	|Tok_Semi	-> (Assign(Id id,e1),r3)
	|_			-> raise (IllegalExpression "assign-semi")
and parse_print lst = 
	let (look,_) = lookahead lst in
	match look with
	|Tok_Print ->	
	let r1 = match_tok look lst in		(* consume 'printf' *)
	let r2 = match_tok (Tok_LParen) r1 in	(* consume '(' *)
	let (e1,r3) = parseExp r2 in
	let r4 = match_tok (Tok_RParen) r3 in	(* consume ')' *)
	let r5 = match_tok (Tok_Semi) r4 in		(* consume ';' *)
	(Print(e1),r5)
	|_	-> raise (IllegalExpression "print")

and parse_while lst = 
	let (look,_) = lookahead lst in
	(match look with
	|Tok_While	->
	let r1 = match_tok look lst in 		(* consume 'while' *)
	let r2 = match_tok (Tok_LParen) r1 in	(* consume '(' *)
	let (e1,r3) = parseExp r2 in
	let r4 = match_tok (Tok_RParen) r3 in	(* consume ')' *)
	let r5 = match_tok (Tok_LBrace) r4 in	(* consume '{' *)
	let (a,r,wlst) = while_help r5 [] in
	(While(e1,wlst), r) 
	|_	-> raise (IllegalExpression "while_badinput") )
	

and while_help lst track =
	let (look,r10) = lookahead lst in
	if statement_checker look = true then
		let (a1,rest) = parse_statement lst in
		let (slook2,_) = lookahead rest in
		(match slook2 with
		|Tok_RBrace	->
			(* Stops when RBrace found *)
			let rest2 = match_tok (Tok_RBrace) rest in (* consume '}' *)
			let revtrack = List.rev (a1::track) in
			
			(a1,rest2,List(revtrack))
			
		|_	-> (while_help rest (a1::track))	) (* add ast to list *)
		
	else if decl_check look = true then
		let (a1,rest) = parse_statement lst in
		let (slook2,_) = lookahead rest in
		(match slook2 with
		|Tok_RBrace	->
			(* Stops when RBrace found *)
			let rest2 = match_tok (Tok_RBrace) rest in (* consume '}' *)
			let revtrack = List.rev (a1::track) in
			
			(a1,rest2,List(revtrack))
			
		|_	-> (while_help rest (a1::track))	) (* add ast to list *)
	
	else if rb_chk look = true then
		(match track with
		|[]		-> (Num(1),r10,List[])
		|_		-> 
		let revtrack = List.rev track in
		(Num 1,r10,List(revtrack))		)
	else raise (IllegalExpression "while-help")
	
and parse_if lst =
	let (look,_) = lookahead lst in
	match look with
	|Tok_If ->
	let r1 = match_tok look lst in			(* consume 'if' *)
	let r2 = match_tok (Tok_LParen) r1 in 	(* consume '(' *)
	let (e1,r3) = parseExp r2 in
	let r4 = match_tok (Tok_RParen) r3 in	(* consume ')' *)
	let r5 = match_tok (Tok_LBrace) r4 in 	(* consume '{' *)
	
	let (look2,rlook2) = lookahead r5 in
	(match look2 with 	(* either '}' or if branch *)
		|Tok_RBrace	->	(* empty if branch = List[]*)
			let (look3,rlook3) = lookahead rlook2 in
			(match look3 with
				|Tok_Else	->
				let (el,r7,ellst) = parse_else rlook3 in	(* consumes 'else' *)
				( If(e1,List[],ellst), r7) 
				(* '}' already consumed in r7 *)
				
				|_	-> (If(e1,List[],List[]), rlook2)	)
		
		|_ 	-> 	(* not empty if branch *)
			let (a1,r6,iflst) = (if_st_help r5 []) in
			let (look4,rlook4) = lookahead r6 in
			(match look4 with
				|Tok_Else	->
				let (el2,r8,ellst) = parse_else rlook4 in (* consumes 'else' *)
				(If(e1,iflst,ellst), r8)
				
				|_	-> (If(e1,iflst,List[]), r6)		)	)
	|_	-> raise (IllegalExpression "if_no-if")

and if_st_help lst track =
	(* Function to recursively check all statements in branch *)
	let (slook,_) = lookahead lst in
	if statement_checker slook = true then
		let (a1,rest) = parse_statement lst in
		let (slook2,_) = lookahead rest in
		(match slook2 with
		|Tok_RBrace	->
			(* Stops when RBrace found *)
			let rest2 = match_tok (Tok_RBrace) rest in (* consume '}' *)
			let revtrack = List.rev (a1::track) in
			
			(a1,rest2,List(revtrack))
			
		|_	-> (if_st_help rest (a1::track))	) (* add ast to list *)
		
	else if decl_check slook = true then
		let (a1,rest) = parse_localDeclaration lst in
		let (slook2,_) = lookahead rest in
		(match slook2 with
		|Tok_RBrace	->
			(* Stops when RBrace found *)
			let rest2 = match_tok (Tok_RBrace) rest in (* consume '}' *)
			let revtrack = List.rev (a1::track) in
			
			(a1,rest2,List(revtrack))
			
		|_	-> (if_st_help rest (a1::track))	) (* add ast to list *)
		
	else if rb_chk slook = true then
		let r2 = match_tok (Tok_RBrace) lst in
		let revtrack = List.rev track in
		(Num 1,r2,List(revtrack))  (****** THIS IS IT FIX THIS *******)
	else raise (IllegalExpression "if-help")
	(* raise error since it is not RBrace or not statement *)

and parse_else lst = 
	(* list comes in having consumed else already *)
	let (elselook,_) = lookahead lst in
	let elr2 = match_tok elselook lst in	(* consume '{' *)
	let (a,rest,ellst) = (if_st_help elr2 []) in
	(a,rest,ellst)
	(* returns else branch and list without RBrace *)
	
and statement_checker token = 
	(* checks if current token is a statement *)
	match token with
	|Tok_Id s 	-> true		
	|Tok_If		-> true				
	|Tok_While	-> true			
	|Tok_Print	-> true
	|_			-> false

and decl_check token =
	match token with
	|Tok_Int	-> true
	|_			-> false

and rb_chk token =
	match token with 
	|Tok_RBrace	-> true 
	|_	-> false

	(* Begin expression code *)
and parseExp lst =
	let (e1,r1) = parse_additive lst in
	let (look,r2) = lookahead r1 in
	match look with
	|Tok_Semi 	-> (e1,r1)	(* if ';' return to assign - e1 should be an AST *)
	|Tok_Less 	->  
		let (e2,r2) = parseExp r2 in
		(Less(e1,e2),r2) (* might need to swap this *)
	|Tok_Equal	->	
		let (e3,r3) = parseExp r2 in
		(Equal(e1,e3),r3) (* might need to swap this *)
	|Tok_Greater->
		let (e4,r4) = parseExp r2 in
		(Greater(e1,e4),r4) (* might need to swap this *)
	|_			-> (e1,r1)
and parse_additive lst =
	let (e1,r1) = parse_multiplicative lst in
	let (look,rest) = lookahead r1 in
	match look with
	|Tok_Sum 	-> 
		let (e2,r2) = parse_additive rest in (Sum(e1,e2),r2)
	|_	-> (e1,r1)
and parse_multiplicative lst =
	let (e1,r1) = parse_power lst in
	let (look,rest) = lookahead r1 in
	match look with
	|Tok_Mult 	-> 
		let (e2,r2) = parse_multiplicative rest in (Mult(e1,e2),r2)
	|_ 			-> (e1,r1)
and parse_power lst = (* Parse "... ^ ..." *)
	(* e1 = (...)|ID|INITLIT -- look = lookahead of rest of list after parse_primary
		e2 = parse the second part of "...^..." *)
	let (e1,r1) = parse_primary lst in
	let (look,rest) = lookahead r1 in
	match look with
	|Tok_Pow 	-> 
		let (e2,r2) = parse_power rest in (Pow(e1,e2),r2)
	|_ 			-> (e1,r1)
and parse_primary lst =	
	(* Parse '(' ... ')' (or ID or INITLIT) *)
	(* e2 (AST)= whatever is inside the parentheses w/ r2 as the ")..." *)
	(* r2r is the rest of the list without the RParen *)
	let (look,rest) = lookahead lst in
	match look with 
	|Tok_LParen -> 
		let (e2,r2) = parseExp rest in	(*do: '(' exp ')' *)
		let (e2e,_) = lookahead r2 in
		let r3 = match_tok e2e r2 in	(* consumes RParen *)
		(Paren(e2),r3)
	|Tok_Id	s	-> let (id,r4) = parseID lst in (Id s,r4)
	|Tok_Num d 	-> let (dig,r5) = parseINITLIT lst in (dig,r5) (* dig is an AST of Num of int *)
	|_		-> raise (IllegalExpression "primary")

and parseID lst = 
	let (look,rest) = lookahead lst in
	match look with
	|Tok_Id s	-> (Id s, rest)
	|_			-> raise (IllegalExpression "ID")
and parseINITLIT lst =  
	(* Returns AST of Num of int *)
	let (look,rest) = lookahead lst in
	match look with
	|Tok_Num d 	-> (Num(d),rest) 
	|_			-> raise (IllegalExpression "digit")
;;






(* ------------------------------------------------------*)





exception Error of int ;;




let read_lines name : string list =
  let ic = open_in name in
  let try_read () =
    try Some (input_line ic) with End_of_file -> None in
  let rec loop acc = match try_read () with
    | Some s -> loop (s :: acc)
    | None -> close_in ic; List.rev acc in
  loop []


let tok_to_str t = ( match t with
          Tok_Num v -> string_of_int v
        | Tok_Sum -> "+"
        | Tok_Mult ->  "*"
        | Tok_LParen -> "("
        | Tok_RParen -> ")"
		| Tok_Pow->"^"
        | Tok_END -> "END"
        | Tok_Id id->id
		| Tok_String s->s
		| Tok_Assign->"="
		 | Tok_Greater->">"
		 | Tok_Less->"<"
		 | Tok_Equal->"=="
		 | Tok_Semi->";"
		 | Tok_Main->"main"
		 | Tok_LBrace->"{"
		 | Tok_RBrace->"}"
		 | Tok_Int->"int" 
		 | Tok_Float->"float"
		 | Tok_Print->"printf"
		 | Tok_If->"if"
		 | Tok_Else->"else"
		 | Tok_While-> "while"
    )

let print_token_list tokens =
	print_string "Input token list = " ;
	List.iter (fun x -> print_string (" " ^ (tok_to_str x))) tokens;
	print_endline ""
;;





(* -------------- Your Code Here ----------------------- *)


let rec unwrap alst =
	let l =List.fold_left (fun a x -> x::a) [] alst in
	(List.rev l)
;;

let rec indent pos =
	print_string (String.make pos '_')
;;

(* pos = indent | x = ast *)
let rec pretty_print pos x=
	match x with
	|Fun (t,f,a,mb)	-> print_string "int main(){\n"; 
		(match mb with 
			|List[]	-> pretty_print 0 mb;
			|_ 	-> pretty_print 4 mb;	);		
		print_string "}\n";
	
	|List a	-> 
		let l = unwrap a in
		let rec printlst stlst = match stlst with
			|[]		-> print_string ""; (* return *)
			|h::t	-> pretty_print pos h; printlst t
		in printlst l;
	|Id s 	-> print_string s;
	|Num d	-> print_int d;
	|Define (t,id)	-> 
		indent pos; print_string "int "; 
		pretty_print 0 id; print_string ";\n";
	|Assign (id,v)	->
		indent pos; pretty_print 0 id; print_string " = ";
		pretty_print 0 v; print_string ";\n";
	|Sum (a1,a2)	->
		indent pos; pretty_print 0 a1; print_string " + ";
		pretty_print 0 a2;
	|Mult (a1,a2)	->
		indent pos; pretty_print 0 a1; print_string " * ";
		pretty_print 0 a2;
	|Pow (a1,a2)	->
		indent pos; pretty_print 0 a1; print_string " ^ ";
		pretty_print 0 a2;
	|Greater (a1,a2)->
		indent pos; pretty_print 0 a1; print_string " > ";
		pretty_print 0 a2;
	|Equal (a1,a2)->
		indent pos; pretty_print 0 a1; print_string " == ";
		pretty_print 0 a2;
	|Less (a1,a2)->
		indent pos; pretty_print 0 a1; print_string " < ";
		pretty_print 0 a2;
	|Paren a	->
		indent pos; print_string "("; pretty_print 0 a; 
		print_string ")";
	
	|If (c,i,e)	->
		(* check if i or e is empty -- (pos+4)? *)
		indent pos; print_string "if("; pretty_print 0 c; print_string "){\n"; 
		(match i with
			|List[]	-> 
				indent pos; print_string "}";
				(match e with
				|List[]	-> print_string "\n";
				|_		-> 
				print_string "else{\n"; pretty_print (pos+4) e; 
				indent pos; print_string "}\n";		); 
			|_		-> 
				pretty_print (pos+4) i; indent pos; print_string "}";
				(match e with
				|List[]	-> print_string "\n";
				|_		-> 
				print_string "else{\n"; pretty_print (pos+4) e; 
				indent pos; print_string "}\n";		); 
		); 
		
	|While (c,s)-> 
		indent pos; print_string "while("; pretty_print 0 c; print_string "){\n";
		pretty_print (pos+4) s; indent pos; print_string "}\n";
	
	|Print a	->
		indent pos; print_string "printf("; pretty_print 0 a;
		print_string ");\n"; 
	
	()	

;;


(* ----------------------------------------------------- *)


(*
you can test your parser and pretty_print with following code 
*)

(*

let prg1 = read_lines "main.c";;
let code = List.fold_left (fun x y->x^y) "" prg1;;	
let t = tokenize code;;
let (a,b)=parse_Function t;;

*)