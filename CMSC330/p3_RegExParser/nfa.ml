(* CMSC 330 Organization of Programming Languages 
_____Fall_2016_____ FA2016P3REGEXOCAML
Dr. Anwar Mamat 
Project 3: Regular Expression Interpreter
 
*)


(*
Name: Ben Aronson
UID: 113548802
CMSC 330 - Section 0106
Project: Project 3 - Regular Expression Parser
*)


#load "str.cma"

(* ------------------------------------------------- *)
(* MODULE SIGNATURE *)
(* ------------------------------------------------- *)

module type NFA =
  sig
    (* You may NOT change this signature *)

    (* ------------------------------------------------- *)
    (* PART 1: NFA IMPLEMENTATION *)
    (* ------------------------------------------------- *)

    (* ------------------------------------------------- *)
    (* Abstract type for NFAs *)
    type nfa

    (* Type of an NFA transition.

       (s0, Some c, s1) represents a transition from state s0 to state s1
       on character c

       (s0, None, s1) represents an epsilon transition from s0 to s1
     *)
    type transition = int * char option * int

    (* ------------------------------------------------- *)
    (* Returns a new NFA.  make_nfa s fs ts returns an NFA with start
       state s, final states fs, and transitions ts.
     *)
    val make_nfa : int -> int list -> transition list -> nfa

    (* ------------------------------------------------- *)
    (*  Calculates epsilon closure in an NFA.

	e_closure m ss returns a list of states that m could
	be in, starting from any state in ss and making 0 or
	more epsilon transitions.

       There should be no duplicates in the output list of states.
     *)

    val e_closure : nfa -> int list -> int list

    (* ------------------------------------------------- *)
    (*  Calculates move in an NFA.

	move m ss c returns a list of states that m could
	be in, starting from any state in ss and making 1
	transition on c.

       There should be no duplicates in the output list of states.
     *)

    val move : nfa -> int list -> char -> int list

    (* ------------------------------------------------- *)
    (* Returns true if the NFA accepts the string, and false otherwise *)
    val accept : nfa -> string -> bool

    (* ------------------------------------------------- *)
    (* Gives the stats of the NFA

      the first integer representing the number of states
      the second integer representing the number of final states
      the (int * int) list represents the number of states with a particular number of transitions
      e.g. (0,1) means there is 1 state with 0 transitions, (1,2) means there is 2 states with 1 transition
      the list would look something like: [(0,1);(1,2);(2,3);(3,1)]

    *)

    val stats : nfa -> int * int * (int * int) list

    (* ------------------------------------------------- *)
    (* PART 2: REGULAR EXPRESSION IMPLEMENTATION *)
    (* ------------------------------------------------- *)

    (* ------------------------------------------------- *)
    type regexp =
	Empty_String
      | Char of char
      | Union of regexp * regexp
      | Concat of regexp * regexp
      | Star of regexp

    (* ------------------------------------------------- *)
    (* Given a regular expression, print it as a regular expression in
       postfix notation (as in project 2).  Always print the first regexp
       operand first, so output string will always be same for each regexp.
     *)
    val regexp_to_string : regexp -> string

    (* ------------------------------------------------- *)
    (* Given a regular expression, return an nfa that accepts the same
       language as the regexp
     *)
    val regexp_to_nfa : regexp -> nfa

    (* ------------------------------------------------- *)
    (* PART 3: REGULAR EXPRESSION PARSER *)
    (* ------------------------------------------------- *)

    (* ------------------------------------------------- *)
    (* Given a regular expression as string, parses it and returns the
       equivalent regular expression represented as the type regexp.
     *)
    val string_to_regexp : string -> regexp

    (* ------------------------------------------------- *)
    (* Given a regular expression as string, parses it and returns
       the equivalent nfa
     *)
    val string_to_nfa: string -> nfa

    (* ------------------------------------------------- *)
    (* Throw IllegalExpression expression when regular
       expression syntax is illegal
     *)
    exception IllegalExpression of string

end

(* ------------------------------------------------- *)
(* MODULE IMPLEMENTATION *)
(* ------------------------------------------------- *)

    (* Make all your code changes past this point *)
    (* You may add/delete/reorder code as you wish
       (but note that it still must match the signature above) *)

module NfaImpl =
struct

type transition = int * char option * int

(* type nfa = ()  to implement *)
(* Alphabet * List of states * Start state * List of final states * List of transitions *)

(*type nfa = char list * int list * int * int list * transition list*)
type nfa = NFA of int * int list * transition list

(* 
make_nfa : int -> int list -> transision list -> nfa 
ss = starting state || fs = final states || ts = transition list 
*)
let make_nfa ss fs ts = NFA (ss,fs,ts)


(* nfa -> starting state -> list of states ss can reach with e-transitions *)
let e_closure m ss = match ss with
	[]	-> []
	|_ 	-> 
	match m with NFA (_,_,transitions) -> let ts = transitions in
	let rec sloop sts acc = match sts with
		[]		-> (List.rev acc)
		|h1::t1	-> 
			let rec cloop seen check = match check with
				[]		-> sloop t1 seen
				|h2::t2	-> 
				let rec tloop trs = match trs with
					[]		-> cloop seen t2
					|h3::t3	-> match h3 with
						(sa,Some c,sb)	-> tloop t3
						|(sa,None,sb)	-> 
							if List.mem sa seen = true && List.mem sb seen = false then cloop (sb::seen) (sb::check)
							else tloop t3 in
				tloop ts in
			cloop acc (h1::[]) in
	sloop ss ss
;;		

(* nfa -> int list -> char -> int list 
m = nfa || ss = starting states (list) || c = character *)
let move m ss c = match ss with
	[]	-> []
	|_ 	-> 
	match m with NFA (_,_,transitions) -> let ts = transitions in
	let rec sloop sts acc = match sts with
		[]	-> (List.rev acc)
		|h1::t1	->
		let rec tloop trs acc2 = match trs with
			[]		-> sloop t1 acc2
			|h2::t2 -> match h2 with
				(sa,Some ch,sb)	-> 
					if sa = h1 && ch = c && List.mem sb acc2 = false then tloop t2 (sb::acc2)
					else tloop t2 acc2
				|(sa,None,sb)	-> tloop t2 acc2 in
		tloop ts acc in
	sloop ss []
;;

let rec print_list = function 
	[] -> ()
	| e::l -> print_int e ; print_string " " ; print_list l
;;

(* helper to check for t/f if string param is empty *)
let rec fcheck l1 fs = match l1 with
		[]		-> false
		|h::t	-> if List.mem h fs = true then true else fcheck t fs
;;
(* fcheck (e_closure m (ss::[])) fs false *)

(* helper to convert string to list *)
let rec str2lst s i lst = 
			if i < (String.length s) then str2lst s (i+1) (s.[i]::lst)
			else List.rev lst
;;

(* nfa -> string -> bool 
m = nfa || s = string *)
let accept m s = 
	match m with NFA (ssts,fsts,transitions) -> 
	let ss = ssts in
	let fs = fsts in
	let strlst = str2lst s 0 [] in
	let rec sloop str st8s =
		match str with
			[]		-> fcheck (e_closure m st8s) fs
			|h1::t1	-> 
			let ec = e_closure m st8s in
			let mvs = move m ec h1 in
			(* print_list st8s; print_string "\t"; print_char h1; print_string "\t";print_list mvs; print_string "\n";*)
			match mvs with 
				[] 		-> false
				|_ 		-> sloop t1 mvs
	in sloop strlst (ss::[])
;;


(*

match mvs with 
					[] 		-> false
					|h2::t2 -> if List.mem h2 seen then (sloop t1 t2 (h2::[]))
								else sloop t1 mvs (h2::[])
;;								
								
		let rec slp2 i st8 tf = 
			if i < (String.length s) then let ele = s.[i] in
				let mvst = move m (e_closure m st8) ele in
				match mvst with [] -> false | _ -> 
				if fcheck (e_closure m mvst) fs false = true then 
					slp2 (i+1) mvst true
				else slp2 (i+1) mvst false 
			else tf
		in slp2 0 (ss::[]) false



let strlst = str2lst s 0 [] in
let rec sloop str strts torf =
	match str with
		[]		-> if torf = true then true else false 
		|h1::t1	-> 
		
		let rec checkfs nl torf2 = match nl with
			[]	-> torf2 
			|_ 	-> let ecnl = (e_closure m nl) in
			match ecnl with
			[]		-> torf2
			|h2::t2	->
			if List.mem h2 fs = false then
				if (sloop t1 (e_closure m (h2::[])) false) = false then
					(checkfs t2 false )
				else true	(* CHECK! *)					
			else (sloop t1 (e_closure m (h2::[])) true) in
		checkfs (move m strts h1) false in
		
(sloop strlst (e_closure m [ss]) false)







let rec sloop str strts torf =
			match str with
				[]		-> if torf = true then true else false 
				|h1::t1	-> 
					let rec checkfs nl torf2 = match nl with
						[]		-> torf2
						|h2::t2	->
						let ech2 = e_closure m (h2::[]) in
						if mttcheck ech2 fs false = false then
							checkfs t2 false
						else (sloop t1 (e_closure m (h2::[])) true) in
					checkfs (move m strts h1) false in
		(sloop strlst (e_closure m [ss]) false)





let rec checkfs nl torf2 = match nl with
					[]	-> torf2 
					|_ 	-> let ecnl = (e_closure m nl) in
						match ecnl with
						[]		-> torf2
						|h2::t2	->
						if List.mem h2 fs = false then
							checkfs t2 false
						else (sloop t1 (e_closure m (h2::[])) true) in
					checkfs (move m strts h1) false in


if (sloop t1 (e_closure m (h2::[])) false) = false then
	checkfs t2 false
else true	(* CHECK! *)					
else (sloop t1 (e_closure m (h2::[])) true) in


else (sloop t1 (e_closure m (h2::[])) true) in

let mvstarts = (move m strts h1) in match mvstarts with
						[] -> torf
						|_ -> checkfs (e_closure m mvstarts) false

if torf = true then true else false 
let tf = false in
				if (List.fold_left (fun x a h -> if (List.mem h fs = true) then let a = true in a 
												else a) tf (e_closure(ss)) ) = true then true
				else false
*)


(*** Delete this later ***)
let rec string_of_int_tuple_list l =
  let string_of_int_tuple (a,b) = "(" ^ string_of_int a ^ "," ^ string_of_int b ^ ")" in
    let rec string_of_elements l = match l with
      [] -> ""
      | (h::[]) -> string_of_int_tuple h
      | (h::t) -> string_of_int_tuple h ^ ";" ^ string_of_elements t
    in "[" ^ string_of_elements l ^ "]"
;;

let string_of_stats_tuple (a,b,c) = "(" ^ string_of_int a ^ "," ^ string_of_int b ^ "," ^ string_of_int_tuple_list c ^ ")";;





(* nfa -> (# states, # finals, outgoing edge count list) *)
let stats n = 
	match n with NFA (ss,fs,ts) -> (* (0,0,(0,0)::[]) *)
	let rec outlist trs senA senB =
		match trs with
			[]		-> (senA,senB)
			|h1::t1	-> 
			let (sa,_,sb) = h1 in outlist t1 (sa::senA) (sb::senB)			
	in let (sA,sB) = outlist ts [] [] in
	let rec loopA l sn finlst = match l with
		[]		-> finlst
		|h2::t2	-> 
			if List.mem h2 sn = true then loopA t2 sn finlst
			else let ctA = List.fold_left (fun a x -> if x = h2 then (a+1) else a) 0 l in
			let rec flscan fl = match fl with
				[]				-> loopA t2 (h2::sn) ((ctA,1)::finlst)
				|h3::t3	-> 	let (h3a,h3b) = h3 in if ctA = h3a then loopA t2 (h2::sn) (List.map (fun x -> let (a,b) = x in if a = h3a then (a,b+1) else (a,b)) fl)
									else flscan t3 in
			flscan finlst in
	let flst1 = List.sort compare (loopA sA [] []) in
	let rec loopB l clst finlst = match l with
		[]		-> finlst 
		|h4::t4 -> if List.mem h4 clst = false then let (a,b) = List.hd finlst in if a=0 then 
						(loopB t4 (h4::clst) ((a,b+1)::(List.tl finlst))) else loopB t4 (h4::clst) ((0,1)::finlst)
					else loopB t4 clst finlst in
	let flst2 = loopB sB sA flst1 in
	let sAllNodups = List.fold_left (fun a h -> if List.mem h a = false then h::a else a) [] 
						(List.fold_left (fun a h -> if List.mem h a = false then h::a else a) sA sB) in
	( 		List.length sAllNodups,
			List.length fs,
			flst2 )
;;





type regexp =
	  Empty_String
	| Char of char
	| Union of regexp * regexp
	| Concat of regexp * regexp
	| Star of regexp
;;



(* regexp -> string 
Return string in postfix order *)
let regexp_to_string r =
	let rec rtsrec reg = match reg with
		Empty_String 	-> "E"
		|Char (c)		-> String.make 1 c
		|Union (r1,r2)	-> (rtsrec r1)^" "^(rtsrec r2)^" "^"|"
		|Concat (r1,r2)	-> (rtsrec r1)^" "^(rtsrec r2)^" "^"."
		|Star (r1)		-> (rtsrec r1)^" "^"*"
	in rtsrec r
	
;;



let next =
	let count = ref 0 in
		function () ->
			let temp = !count in
			count := (!count) + 1;
			temp
;;



(* 
print_int st; print_int nd; print_int q1; 

List.append (re2nfa r1 q1 f1) ((st, None, q1)::(f1, None, nd)::(st, None, nd)::(nd, None, st)::[])
print_int ta; print_int tb;print_int tc;print_int td; *)



(* regexp -> nfa
Return nfa that accepts the same language as regexp *)
let regexp_to_nfa r =
	let start = next () in
	let final = next () in
	let rec re2nfa re st nd = match re with
		Empty_String 	-> (st, None, nd)::[]
		|Char (c)		-> (st, Some c, nd)::[]
		|Union (r1,r2)	-> 
			let q1 = next () in let q2 = next () in let f1 = next () in let f2 = next () in
			(List.append ((st, None, q1)::(st, None, q2)::(f1, None, nd)::(f2, None, nd)::[])
						(List.append (re2nfa r1 q1 f1) (re2nfa r2 q2 f2)) )
		|Concat (r1,r2)	-> 
			let q1 = next () in let q2 = next () in let f1 = next () in let f2 = next () in
			List.append ((st,None,q1)::(f1, None, q2)::(f2, None, nd)::[])
						(List.append (re2nfa r1 q1 f1) (re2nfa r2 q2 f2)) 
		|Star (r1)		-> 
			let q1 = next () in let f1 = next () in
			List.append ((st, None, q1)::(f1, None, nd)::(st, None, nd)::(nd, None, st)::[]) 
						(re2nfa r1 q1 f1)  in 
	make_nfa start (final::[]) (re2nfa r start final)
;;

exception IllegalExpression of string


(* 
 print_int st; print_int nd; 
print_int st; print_int q1; print_int f1; print_int nd;
re2nfa r start final [] []

 in
	match n0 with NFA (ss, fs, ts) ->
	match ts with
		[]	-> failwith "empty"
		|_	-> failwith "not empty"
;;

(* helper for base case *)
let rec re2nfa re st nd =
	match re with
		Empty_String 	-> (st, None, nd)::[]
		|Char (c)		-> print_int st; print_int nd; 
							let fin = nd in (st, Some c, fin)::[]
		|Union (r1,r2)	-> 
			let q1 = next () in let q2 = next () in let f1 = next () in let f2 = next () in
			List.append ((st, None, q1)::(st, None, q2)::(f1, None, nd)::(f2, None, nd)::[])
						(List.append (re2nfa r1 q1 f1) (re2nfa r2 q2 f2) ) 
		|Concat (r1,r2)	-> 
			let q1 = next () in let q2 = next () in let f1 = next () in let f2 = next () in
			List.append (List.append (re2nfa r1 q1 f1) (re2nfa r2 q2 f2)) 
						((f1, None, q2)::(f2, None, nd)::[])
		|Star (r1)		-> 
			let q1 = next () in let f1 = next () in
			print_int f1; print_int nd;
			List.append (re2nfa r1 q1 f1) ((st, None, q1)::(f1, None, nd)::[])
;;

*)









(************************************************************************)
(* PARSER. You shouldn't have to change anything below this point *)
(************************************************************************)

(* Scanner code provided to turn string into a list of tokens *)

type token =
   Tok_Char of char
 | Tok_Epsilon
 | Tok_Union
 | Tok_Star
 | Tok_LParen
 | Tok_RParen
 | Tok_END

let re_var = Str.regexp "[a-z]"
let re_epsilon = Str.regexp "E"
let re_union = Str.regexp "|"
let re_star = Str.regexp "*"
let re_lparen = Str.regexp "("
let re_rparen = Str.regexp ")"

let tokenize str =
 let rec tok pos s =
   if pos >= String.length s then
     [Tok_END]
   else begin
     if (Str.string_match re_var s pos) then
       let token = Str.matched_string s in
       (Tok_Char token.[0])::(tok (pos+1) s)
	 else if (Str.string_match re_epsilon s pos) then
       Tok_Epsilon::(tok (pos+1) s)
	 else if (Str.string_match re_union s pos) then
       Tok_Union::(tok (pos+1) s)
	 else if (Str.string_match re_star s pos) then
       Tok_Star::(tok (pos+1) s)
     else if (Str.string_match re_lparen s pos) then
       Tok_LParen::(tok (pos+1) s)
     else if (Str.string_match re_rparen s pos) then
       Tok_RParen::(tok (pos+1) s)
     else
       raise (IllegalExpression "tokenize")
   end
 in
 tok 0 str

(*
  A regular expression parser. It parses strings matching the
  context free grammar below.

   S -> A Tok_Union S | A
   A -> B A | B
   B -> C Tok_Star | C
   C -> Tok_Char | Tok_Epsilon | Tok_LParen S Tok_RParen

   FIRST(S) = Tok_Char | Tok_Epsilon | Tok_LParen
   FIRST(A) = Tok_Char | Tok_Epsilon | Tok_LParen
   FIRST(B) = Tok_Char | Tok_Epsilon | Tok_LParen
   FIRST(C) = Tok_Char | Tok_Epsilon | Tok_LParen
 *)

let lookahead tok_list = match tok_list with
	[] -> raise (IllegalExpression "lookahead")
	| (h::t) -> (h,t)

let rec parse_S l =
	let (a1,l1) = parse_A l in
	let (t,n) = lookahead l1 in
	match t with
		Tok_Union -> (
		let (a2,l2) = (parse_S n) in
		(Union (a1,a2),l2)
		)
		| _ -> (a1,l1)

and parse_A l =
	let (a1,l1) = parse_B l in
	let (t,n) = lookahead l1 in
	match t with
	Tok_Char c ->
		let (a2,l2) = (parse_A l1) in (Concat (a1,a2),l2)
	| Tok_Epsilon ->
		let (a2,l2) = (parse_A l1) in (Concat (a1,a2),l2)
	| Tok_LParen ->
		let (a2,l2) = (parse_A l1) in (Concat (a1,a2),l2)
	| _ -> (a1,l1)

and parse_B l =
	let (a1,l1) = parse_C l in
	let (t,n) = lookahead l1 in
	match t with
	Tok_Star -> (Star a1,n)
	| _ -> (a1,l1)

and parse_C l =
	let (t,n) = lookahead l in
	match t with
   	  Tok_Char c -> (Char c, n)
	| Tok_Epsilon -> (Empty_String, n)
	| Tok_LParen ->
		let (a1,l1) = parse_S n in
		let (t2,n2) = lookahead l1 in
		if (t2 = Tok_RParen) then
			(a1,n2)
		else
			raise (IllegalExpression "parse_C 1")
	| _ -> raise (IllegalExpression "parse_C 2")

let string_to_regexp str =
	let tok_list = tokenize str in
	let (a,t) = (parse_S tok_list) in
	match t with
	[Tok_END] -> a
	| _ -> raise (IllegalExpression "string_to_regexp")

let string_to_nfa s = regexp_to_nfa (string_to_regexp s)

end

module Nfa : NFA = NfaImpl;;
