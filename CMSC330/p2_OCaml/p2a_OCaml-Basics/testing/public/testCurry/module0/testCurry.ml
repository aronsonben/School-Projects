(* Test Curried functions *)

#use "testUtils.ml";;
#use "basics.ml";;

(* Test mult_of_n x n *)
prt_bool (mult_of_n 0 5);;
prt_bool (mult_of_n 2 2);;
prt_bool (mult_of_n 5 5);;
prt_bool (mult_of_n 16 5);;
prt_bool (mult_of_n 21 3);;

(* Test triple_it x y z *)
let fst (x,_,_) = x;;
let snd (_,y,_) = y;;
let thd (_,_,z) = z;;
prt_int (fst (triple_it 1 2 3));;
prt_int (snd (triple_it 1 2 3));;
prt_int (thd (triple_it 1 2 3));;

(* Test max_pair (x,y) (m,n) *)
let fstp (x,_) = x;;
let sndp (_,y) = y;;
prt_int (fstp (maxpair (1,3) (2,3)));;
prt_int (sndp (maxpair (1,4) (2,3)));;
prt_int (sndp (maxpair (1,4) (1,3)));;
