(* Test Curried functions *)

#use "testUtils.ml";;
#use "basics.ml";;

let x = [5;6;7;3] ;;
let y = [1;2] ;;
let m = [(1,2); (3,4); (4,2)];;
let n = (2,5)::[];;

(* Test power_of *)
prt_bool (power_of 2 8);;
prt_bool (power_of 5 27);;
prt_bool (power_of 10 1);;

(* Test prod l *)
prt_int (prod x);;
prt_int (prod y);;

(* Test unzip l *)
let fstp (x,_) = x;;
let sndp (_,y) = y;;
prt_int (prod (fstp (unzip m)));;
prt_int (prod (sndp (unzip m)));;
prt_int (prod (fstp (unzip n)));;
prt_int (prod (sndp (unzip n)));;

(* Test maxpairall l *)
prt_int (fstp (maxpairall m));;
prt_int (sndp (maxpairall m));;
prt_int (fstp (maxpairall n));;
prt_int (sndp (maxpairall n));;
