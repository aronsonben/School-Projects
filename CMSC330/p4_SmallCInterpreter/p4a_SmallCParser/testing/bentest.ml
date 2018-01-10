(* bentest.ml *)
#use "smallc.ml";;

let test = tokenize "int main(){int a;a=100;if(a>10){a=200;b=10;}}";;
let (func,_) = parse_Function test;;

(* pretty_print 0 func;; *)