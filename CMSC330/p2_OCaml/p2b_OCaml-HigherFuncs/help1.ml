type int_tree =
    IntLeaf 
  | IntNode of int * int_tree * int_tree
;;
(* An empty tree is a leaf *)
let empty_int_tree = IntLeaf



let rec int_insert x t =
  match t with
      IntLeaf -> IntNode(x,IntLeaf,IntLeaf)
    | IntNode (y,l,r) when x > y -> IntNode (y,l,int_insert x r)
    | IntNode (y,l,r) when x = y -> t
    | IntNode (y,l,r) -> IntNode(y,int_insert x l,r)
;;

let rec int_mem x t =
  match t with
      IntLeaf -> false
    | IntNode (y,l,r) when x > y -> int_mem x r
    | IntNode (y,l,r) when x = y -> true
    | IntNode (y,l,r) -> int_mem x l
