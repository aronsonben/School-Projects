(* nfa_extra code: *)














(****** regex to str ****)

(*
rtsrec r1^rtsrec r2^"|"^""
let strc = String.make 1 c in String.concat " " (strc::strlst)

((rtsrec r1 ("."::strlst))::strlst)
(rtsrec r2 (rtsrec r1 ("|"::strlst)))::strlst
((rtsrec r1 strlst)::(rtsrec r2 strlst))::("|"::strlst)

 String.concat "" (rtsrec r1 ::(" "::(rtsrec r2::(" "::("."::strlst)))))
String.concat "" (rtsrec r1 ::(" "::(rtsrec r2::(" "::("|"::strlst)))))
String.concat  (rtsrec r1::("*"::strlst))
*)


(***** stats extra ****)


(* 
List.map (fun x -> let (a,b) = x in print_int a; print_int b; print_string "\n") flst1;

	let oec3 = (sA,sB)::[] in
let honly = List.filter (fun x -> let (sc,_,_) = x in if sc = sa then true else false) trs in
			let rec lstloop lst2 =
				match lst2 with 
					[]				-> outlist t1 ((List.length honly,1)::lst) (sa::senA) (sb::senB)
					|(h2a,h2b)::t2	->
						if h2a = List.length honly then 
							outlist t1 (List.map (fun x -> let (a,b) = x in if a = h2a then (a,b+1) else (a,b)) lst) (sa::senA) (sb::senB)
						else lstloop t2 in
			lstloop lst

let oec2 = List.filter (fun x -> let (a,b) = x in if b=0 then false else true) oec in 

---- - - - - - 
let rec lstloop lst finlst =
		match lst with
			[]		-> finlst
			|h2::t2	-> 
*)


(**** accept extra ****)

(* 
sloop t1 (e_closure m nxtlst) true in
let nxtlst = (move m strts h1) in
				match nxtlst with
					[]	-> torf
					|h2::t2 -> sloop t1 (e_closure m nxtlst) true in
	
	let ecss = e_closure m [ss] in
	match strlst with 
		[]	-> false
		|h::t ->
		let l = move m [ss] h in
		match l with
			[]	-> false
			|h2::t2 -> true
;;
---
let start = [ss] in let finals = fs in let ts = transitions in

let rec exp i l =
		if i < 0 then l else exp (i - 1) (s.[i] :: l) in
	exp (String.length s - 1) [] in


let ecss = e_closure m (start::[]) in
	let reach = move m ecss s.[0] in
	match reach with
		[]		-> false
		|h::t 	-> true
*)

(**************************************************************)
			(****** e closure extra ******)
(* e closre
let rec tsloop alph sts trs =
		match trs with
			| []	-> (ss,fs,ts)
			| h::t	-> 
			match h with
				 (s0,None,s1)	-> 	if List.mem s0 sts = false
										then tsloop alph (s0::sts) t
									else tsloop alph sts t
				|(s0,Some c,s1)	->	if List.mem s0 sts = false then
										if List.mem c alph = false then
												tsloop (c::alph) (s0::sts) t
										else tsloop alph (s0::sts) t
									else
										if List.mem c alph = false then
												tsloop (c::alph) sts t
										else tsloop alph sts t in
	tsloop [] [] ts
*)


(* e closure	
	let ssloop sl = match sl with
		 []		-> sl
		|hstate::tl1	-> match m with
			NFA (s,f,t)	-> 
			let tloop trs a = match trs with
					 []		-> s::[]
					|hts::tl2	-> 
					match hts with
						 (s0,None,s1)	-> 	if s0 = hstate then 
												if List.mem s1 a = false then
													tloop tl2 (s1::a)
												else tloop tl2 a
											else 
;;										
*)

(* e closure 
let rec tloop root acc2 trs  = match trs with
				[]		-> (List.rev acc2)
				|h2::t2	-> match h2 with
					(s0,Some c,_) 	-> 	if s0 = root then tloop root acc2 t2
										else tloop s0 acc2 t2
					|(s0,None,s1)	-> 	
						if s0 = root then
							if List.mem s1 acc2 = false then tloop s1 (s1::acc2) t2
							else tloop s1 acc2 t2
						else tloop s0 acc2 t2 in
			tloop h1 acc ts in
*)

(*
let rec tloop root acc2 trs  = match trs with
				[]		-> (List.rev acc2)
				|h2::t2	-> match h2 with
					(s0,Some c,_) 	-> 	if s0 = root then tloop root acc2 t2
										else tloop s0 acc2 t2
					|(s0,None,s1)	-> 	
						if s0 = root then
							if List.mem s1 acc2 = false then tloop s1 (s1::acc2) t2
							else tloop s1 acc2 t2
						else tloop s0 acc2 t2 in
			tloop h1 acc ts in
		sloop ss ((List.hd ss)::[])
*)		
			
			
			
			
			
			
(*			
			let rec bfs st trans = 
				match ts with
				[]		-> []
				|h::t	-> 
				match h with
					(sa,Some c, sb)	-> 	if sa = st then sb::(bfs st t)
										else bfs st t
					|(sa,None,sb)	->	if sa = st then sb::(bfs st t)
										else bfs st t in
			let htos = bfs h1 ts in
			let rec tloop root acc2 tos trs  = match trs with
				[]		-> (List.rev acc2)
				|h2::t2	-> match h2 with
					(sa,Some c,sb) 	-> 
						if sa = root then 
							let ntos = List.filter (fun x -> if x != sb then true else false) tos in
							tloop root acc2 ntos t2
						else tloop root acc2 tos t2
					|(s0,None,s1)	->
						if s0 = root then
							let ntos = List.filter (fun x -> if x != s1 then true else false) tos in
							if List.mem s1 acc2 = false then
								if ntos = [] then tloop s1 (s1::acc2) (bfs s1 ts) t2
								else tloop s1 (s1::acc2) ntos t2
							else 
								if ntos = [] then tloop s1 (s1::acc2) (bfs s1 ts) t2
								else tloop s1 (s1::acc2) ntos t2
						else tloop s0 acc2 tos t2 in
			tloop h1 acc htos ts in
	sloop ss ((List.hd ss)::[])
;;	
*)


(**************************************************************)