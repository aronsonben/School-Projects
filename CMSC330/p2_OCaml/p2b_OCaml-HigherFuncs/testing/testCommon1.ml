#use "testUtils.ml";;         
#use "data.ml";; 
let t0 = empty_int_tree;;     
let t1 = (int_insert 2 (int_insert 5 t0));;
let t3 = (int_insert 10 (int_insert 3 (int_insert 11 t1)));;       
let t4 = (int_insert 15 t3);;
let t5 = (int_insert 1 t4);;

(*
          5
	 / \
        2   11
       / \  / \  
      1  3 10 15      *)
prt_int (common t5 1 11);;
prt_int (common t5 1 10);;
prt_int (common t5 2 10);;
prt_int (common t5 2 3);;
prt_int (common t5 10 11);;
prt_int (common t5 11 11);; 

