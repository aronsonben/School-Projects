#use "testUtils.ml"
#use "nfa.ml"

let test_accept2 m str =
    print_endline 
        ("accept(" ^ str ^ ") = " ^ 
        (string_of_bool (Nfa.accept m str) ))
;;

let r = Nfa.Concat( Nfa.Star( Nfa.Union(Nfa.Char('a'),Nfa.Char('b')) ), 
			Nfa.Concat( Nfa.Char('b'), Nfa.Concat( Nfa.Char('b'), Nfa.Star(Nfa.Char('a')) ) ) )
let m = Nfa.regexp_to_nfa r;;
test_accept2 m "abba";;