(* smallc extra / testing command copy+paste file *)

ruby testing/gotest_public_parse_main.rb

ruby testing/gotest_public_prettyprint_main.rb

cat testing/public_parse_main/module0.expected

testing/public_parse_main/module0.output

testing/public_parse_main/module0.error

let test = tokenize "int main(){int a;a=100;}";;
let func = parse_Function test;;


(* 
	let outcome = statement_checker look2 in
	
	if outcome = 1 then 
		let (if1,rest0) = parse_statement r5 in	
		(* ( If(e1,List[if1],List[]), rest01 ) *)
		
		
		
		
	else if outcome = 0 then (* RBrace *)
		let r6 = match_tok look2 r5 in		(* consume '}' *)
		
		let (look3,_) = lookahead r6 in
		match look3 with
		|Tok_Else	->
			let r7 = match_tok look3 r6 in			(* consume 'else' *)
			let r8 = match_tok (Tok_LBrace) r7 in	(* consume '{' *)
			
			let (look4,_) = lookahead r8 in
			let outcome2 = statement_checker look4 in
			
			if outcome2 = 1 then 
				let (else1,rest1) = parse_statement r8 in
				
				let (restlook2,_) = lookahead rest1 in 
				let rest11 = match_tok restlook2 rest1 in	(*consume '}'*)
				
				( If(e1,List[if1],List[else1]), rest11 )
				
			else if outcome2 = 0 then (* RBrace *)
				let r9 = match_tok look4 r8 in		(* consume '}' *)
				
				(* Went into else but nothing there *)
				( If(e1,List[if1],List[]), r9 )
				
			else raise (IllegalExpression "if-else_wrong-tok")
			
		|_ -> ( If(e1,List[if1],List[]), r6)
	
	else raise (IllegalExpression "if_wrong-tok")
*)