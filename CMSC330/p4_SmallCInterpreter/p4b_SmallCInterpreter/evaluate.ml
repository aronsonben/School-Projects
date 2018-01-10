(* Ben Aronson - Project 4b *)

exception IErr of string

type vtype = Nul | Dig of int

let rec unwrap alst =
	let l =List.fold_left (fun a x -> x::a) [] alst in
	(List.rev l)
;;

let rec power e1 e2 a =
	match e2 with
		|1		-> a
		|_ 		-> power e1 (e2-1) (a*e1)
;;

let rec eval env x =
	let rec ehelp x2 = 
		match x2 with
		|Fun (t,f,a,l)	-> ehelp l
		|List a 	-> 
			let lt = unwrap a in 
			let rec iterlst lst = match lst with
				|[]		-> 0
				|h::t	-> let _ = ehelp h in iterlst t
			in iterlst lt
		
		|Id s 			-> 
			if ((Hashtbl.mem env s = false) || (Hashtbl.find env s = Nul)) then raise (IErr "id")
			else (match (Hashtbl.find env s) with Dig i-> i |_ -> raise (IErr "id"))
			
		|Num d 			-> d
		
		|Define (t,id)	-> 
			let k = ( match id with Id s -> s |_ -> raise (IErr "define-str")) in
			if (Hashtbl.mem env k) then (raise (IErr "define"))
			else Hashtbl.add env k (Nul); 0
			
		|Assign (id,v)	->
			let k = match id with Id s -> s |_ -> raise (IErr "define-str") in
			(
			if ((Hashtbl.mem env k = false)) then raise (IErr "assign")
			else let value = ehelp v in Hashtbl.replace env k (Dig(value)); 0
			)
		
		|Sum (a1,a2)	-> (ehelp a1) + (ehelp a2)
		|Mult (a1,a2)	-> (ehelp a1) * (ehelp a2)
		|Pow (a1,a2)	-> let e = (ehelp a1) in power e (ehelp a2) e
		|Greater (a1,a2)-> if (ehelp a1) > (ehelp a2) then 1 else (-1)
		|Equal (a1,a2)	-> if (ehelp a1) = (ehelp a2) then 1 else (-1)
		|Less (a1,a2)	-> if (ehelp a1) < (ehelp a2) then 1 else (-1)
		|Paren a 		-> ehelp a
		
		|If (c,e1,e2)	->	
			if (ehelp c) > 0 then (ehelp e1)
			else if (ehelp c) < 0 then (ehelp e2) 
			else 0 (* error? *)
		
		|While (c,e)	->
			if (ehelp c) > 0 then let _ = (ehelp e) in (ehelp x2)
			else 0 (* error? - end while loop *)
			
		
		|Print a 	-> let e = ehelp a in print_int e; print_string "\n"; 0

	
	in let _ = ehelp x in env 
;;


(* 
(match (Hashtbl.find env s) with Dig i-> i |_ -> raise (IErr "id"))
|Assign (id,v)	->
			let k = match id with Id s -> s |_ -> raise (IErr "err") in
			if Hashtbl.mem env k = true then
				let value = match v with Num d -> d|_ -> raise (IErr "err") in
				Hashtbl.replace env k value
			else raise (IErr "assign"); env  *)

(* 
let rec pretty_print pos x=
	match x with
	|Fun (t,f,a,mb)	-> 
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
	|Id s 	-> 			*
	|Num d	-> 			*
	|Define (t,id)	->  -
	|Assign (id,v)	->	-
	|Sum (a1,a2)	->	*
	|Mult (a1,a2)	->	*
	|Pow (a1,a2)	->	*
	|Greater (a1,a2)->	*
	|Equal (a1,a2)	->	*
	|Less (a1,a2)	->	*
	|Paren a		->	-
	|If (c,i,e)		->	
	|While (c,s)	-> 
	|Print a		->	*
	()	
;;
let rec unwrap alst =
	let l =List.fold_left (fun a x -> x::a) [] alst in
	(List.rev l)
;;
*)


(*************************************************)
(* 
|List a	-> 
	let l = unwrap a in
	let rec iterlst lst = match lst with
		|[]		-> 0
		|h::_	-> ehelp h
	in iterlst l
|Id s 	-> 
	(if Hashtbl.mem env s = false && not(Hashtbl.find env s = Nul) then s
	else raise (IErr "id exists or is already declared"));0
|Num d 	-> 0
|Define (t,id)	-> (* check if t is Type_Int ? *)
	let s = ehelp id in (Hashtbl.add env s Nul); 0
|Assign (id,v)	-> 
	(if Hashtbl.mem env id = true then Hashtbl.replace env id v
	else raise (IErr "variable not declared") ); 0
|_		-> 0 
*)