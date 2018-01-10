#use "testUtils.ml";;                                                                                                                                                              
#use "data.ml";;                                                                                                                                                                   

(*   8
    / \
   3   12
  / \  / \
 2  6 9   16

*)


let t0 = empty_int_tree;;                                                                                                                                                           
let t1 = (int_insert 3 (int_insert 8 t0));;
let t2 = (int_insert 2 (int_insert 6 t1));;                                                                                                                                        
let t3 = (int_insert 12 t2);;                                                                                                                       
let t4 = (int_insert 16 (int_insert 9 t3));;

prt_int (common t4 2 6);;
prt_int (common t4 9 16);;
prt_int (common t4 2 9);;
prt_int (common t4 3 8);;
prt_int (common t4 6 8);;   
prt_int (common t4 12 8);;
prt_int (common t4 8 16);;

