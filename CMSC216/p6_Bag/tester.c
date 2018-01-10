#include <stdio.h>
#include <stdlib.h>
#include "bag.h"

/* write your testing program in this file; the tests below are two simple
 * examples
 */

# define SZ 4

/*
static void test1(void);
static void test2(void);
*/

/* tests that size() returns the right value for a bag with several
 * elements
 */
static void test01(void) {
  Bag bag;
  char element[3];
  int i;

  init_bag(&bag);

  for (i= 1; i <= 10; i++) {
    sprintf(element, "%d", i);
    add_to_bag(&bag, element);
  }

  if (size(bag) != 10) {
    printf("Buggy bag- size() is wrong!\n");
    exit(FOUND_BUG);
  }
}


/* tests that count() returns the right value for an element that has
 * several occurrences in a bag
 */
static void test02(void) {
  Bag bag;
  int i;

  init_bag(&bag);

  for (i= 1; i <= 10; i++)
    add_to_bag(&bag, "I love CMSC 216!");

  if (count(bag, "I love CMSC 216!") != 10) {
    printf("Buggy bag- count() is wrong!\n");
    exit(FOUND_BUG);
  }
}


/* testing: count, remove_occurrence, (add_to_bag, clear_bag); */
static void test1(void) {
  Bag bag;

  init_bag(&bag);

  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "Nelson = CMSC216");

  if(count(bag, "Nelsonito") == 2) {
    printf("Count is correct.\n");
  }
  if(remove_occurrence(&bag, "Nelsonito") == 1) {
    printf("Remove occurrence is correct.\n");
  } else {
    printf("DOES NOT WORK\n");
    exit(FOUND_BUG);
  }

  if(count(bag, "Benito") == -1) {
    printf("Count works.\n");
  }
  
  clear_bag(&bag);
  exit(CORRECT);
}

/* testing: remove_from_bag */
static void test2(void) {
  Bag bag;

  init_bag(&bag);

  add_to_bag(&bag, "Nelson");
  add_to_bag(&bag, "coredump");

  printf("Bag size: %d\n", (int)size(bag));

  if(remove_from_bag(&bag, "coredump") == -1) {
    printf("remove_from_bag doesn't work!\n");
    exit(FOUND_BUG);
  } else {
    printf("removed 'coredump' from bag!\n");
  }

  printf("Bag size: %d\n", (int)size(bag));
  clear_bag(&bag);
  exit(CORRECT);
}

/* testing: remove_from_bag */
static void test2_2(void) {
  Bag bag;

  init_bag(&bag);

  add_to_bag(&bag, "Nelson");
  add_to_bag(&bag, "coredump");

  printf("Bag size: %d\n", (int)size(bag));

  if(remove_from_bag(&bag, "Benito") == -1) {
    printf("cannot remove ""Benito"" from bag\n");
    exit(FOUND_BUG);
  } else {
    printf("removed 'Benito' from bag!\n");
  }

  printf("Bag size: %d\n", (int)size(bag));
  clear_bag(&bag);
  exit(CORRECT);
}

/* testing: remove_from_bag (remove head node) */
static void test3(void) {
  Bag bag;

  init_bag(&bag);

  add_to_bag(&bag, "Nelson");
  add_to_bag(&bag, "coredump");

  printf("Bag size: %d\n", (int)size(bag));

  if(remove_from_bag(&bag, "Nelson") == -1) {
    printf("remove_from_bag doesn't work!\n");
    exit(FOUND_BUG);
  } else {
    printf("removed 'Nelson' from bag!\n");
  }

  printf("Bag size: %d\n", (int)size(bag));
  clear_bag(&bag);
  exit(CORRECT);
}

/* testing: remove_from_bag (remove head node) */
static void test3_2(void) {
  Bag bag;

  init_bag(&bag);

  add_to_bag(&bag, "Nelson");
  add_to_bag(&bag, "coredump");
  add_to_bag(&bag, "Nelson");
  add_to_bag(&bag, "Nelson");
  add_to_bag(&bag, "Nelson");
  add_to_bag(&bag, "Nelson");

  printf("Bag size: %d\n", (int)size(bag));

  if(remove_from_bag(&bag, "Nelson") == -1) {
    printf("remove_from_bag doesn't work!\n");
    exit(FOUND_BUG);
  } else {
    printf("removed 'Nelson' from bag!\n");
  }

  printf("Bag size: %d\n", (int)size(bag));
  clear_bag(&bag);
  exit(CORRECT);
}

/* testing: is_sub_bag (1) */
static void test4(void) {
  Bag bag1;
  Bag bag2;

  init_bag(&bag1);
  init_bag(&bag2);

  add_to_bag(&bag1, "b");
  add_to_bag(&bag1, "c");
  add_to_bag(&bag1, "a");
  
  add_to_bag(&bag2, "a");
  add_to_bag(&bag2, "b");
  add_to_bag(&bag2, "c");
  add_to_bag(&bag2, "d");

  if((is_sub_bag(bag1, bag2)) == 0) {
    printf("Bag 1 NOT SUB BAG of Bag 2.\n");
    clear_bag(&bag1);
    clear_bag(&bag2);
    exit(FOUND_BUG);
  } else {
    printf("Bag 1 is a sub bag of Bag 2.\n");
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  exit(CORRECT);
}

/* testing: is_sub_bag (2-bag1.size > bag2.size) */
static void test5(void) {
  Bag bag1;
  Bag bag2;

  init_bag(&bag1);
  init_bag(&bag2);

  add_to_bag(&bag1, "b");
  add_to_bag(&bag1, "c");
  add_to_bag(&bag1, "a");
  
  add_to_bag(&bag2, "a");


  if((is_sub_bag(bag1, bag2)) == 0) {
    printf("Bag 1 NOT SUB BAG of Bag 2.\n");
    clear_bag(&bag1);
    clear_bag(&bag2);
    exit(FOUND_BUG);
  } else {
    printf("Bag 1 is a sub bag of Bag 2.\n");
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  exit(CORRECT);
}

/* testing: is_sub_bag (3-bag1.size == 0) */
static void test6(void) {
  Bag bag1;
  Bag bag2;

  init_bag(&bag1);
  init_bag(&bag2);

  add_to_bag(&bag2, "a");

  if((is_sub_bag(bag1, bag2)) == 0) {
    printf("Bag 1 NOT SUB BAG of Bag 2.\n");
    clear_bag(&bag1);
    clear_bag(&bag2);
    exit(FOUND_BUG);
  } else {
    printf("Bag 1 is a sub bag of Bag 2.\n");
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  exit(CORRECT);
}

/* testing: is_sub_bag (4-different implementation) */
static void test7(void) {
  Bag bag1;
  Bag bag2;

  init_bag(&bag1);
  init_bag(&bag2);

  add_to_bag(&bag1, "coredump");
  add_to_bag(&bag1, "Benito");
  add_to_bag(&bag1, "Piazza");
  
  add_to_bag(&bag2, "This");
  add_to_bag(&bag2, "Is");
  add_to_bag(&bag2, "Missing");
  add_to_bag(&bag2, "Words");

  if((is_sub_bag(bag1, bag2)) == 0) {
    printf("Bag 1 NOT SUB BAG of Bag 2.\n");
    clear_bag(&bag1);
    clear_bag(&bag2);
    exit(FOUND_BUG);
  } else {
    printf("Bag 1 is a sub bag of Bag 2.\n");
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  exit(CORRECT);
}

/* testing: is_sub_bag (5-different occurrences) - should  work
   b/c while "Benito" is over, coredump and Piazza are still a 
   subset */
static void test8(void) {
  Bag bag1;
  Bag bag2;

  init_bag(&bag1);
  init_bag(&bag2);

  add_to_bag(&bag1, "coredump");
  add_to_bag(&bag1, "Benito");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag1, "Piazza");
  
  add_to_bag(&bag2, "coredump");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag2, "Piazza");

  if((is_sub_bag(bag1, bag2)) == 0) {
    printf("Bag 1 NOT SUB BAG of Bag 2.\n");
    clear_bag(&bag1);
    clear_bag(&bag2);
    exit(FOUND_BUG);
  } else {
    printf("Bag 1 is a sub bag of Bag 2.\n");
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  exit(CORRECT);
}

/* testing: is_sub_bag (5-different occurrences) - SHOULD  work  */
static void test9(void) {
  Bag bag1;
  Bag bag2;

  init_bag(&bag1);
  init_bag(&bag2);

  add_to_bag(&bag1, "Piazza");
  add_to_bag(&bag1, "Benito");
  printf("---\n---\n");
  add_to_bag(&bag2, "coredump");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag2, "Piazza");

  if((is_sub_bag(bag1, bag2)) == 0) {
    printf("Bag 1 NOT SUB BAG of Bag 2.\n");
    clear_bag(&bag1);
    clear_bag(&bag2);
    exit(FOUND_BUG);
  } else {
    printf("Bag 1 is a sub bag of Bag 2.\n");
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  exit(CORRECT);
}

/* 9.2 - sort of opposite of last one */
static void test9_2(void) {
  Bag bag1;
  Bag bag2;

  init_bag(&bag1);
  init_bag(&bag2);


  add_to_bag(&bag1, "Benito");
  add_to_bag(&bag1, "coredump");
  add_to_bag(&bag1, "Benito");
  
  add_to_bag(&bag2, "coredump");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag2, "Benito");  
  add_to_bag(&bag2, "Benito");
  
  if((is_sub_bag(bag1, bag2)) == 0) {
    printf("Bag 1 NOT SUB BAG of Bag 2.\n");
    clear_bag(&bag1);
    clear_bag(&bag2);
    exit(FOUND_BUG);
  } else {
    printf("Bag 1 is a sub bag of Bag 2.\n");
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  exit(CORRECT);
}

/* 9.3 - same size one different ele */
static void test9_3(void) {
  Bag bag1;
  Bag bag2;

  init_bag(&bag1);
  init_bag(&bag2);


  add_to_bag(&bag1, "Benito");
  add_to_bag(&bag1, "coredump");
  add_to_bag(&bag1, "Nelson");
  
  add_to_bag(&bag2, "coredump");
  add_to_bag(&bag2, "Nelson");
  add_to_bag(&bag2, "Goats n' Money");
  
  if((is_sub_bag(bag1, bag2)) == 0) {
    printf("Bag 1 NOT SUB BAG of Bag 2.\n");
    clear_bag(&bag1);
    clear_bag(&bag2);
    exit(FOUND_BUG);
  } else {
    printf("Bag 1 is a sub bag of Bag 2.\n");
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  exit(CORRECT);
}
/*union*/
static void test10(void) {
  Bag bag1;
  Bag bag2;
  Bag bag3;

  init_bag(&bag1);
  init_bag(&bag2);
  init_bag(&bag3);

  add_to_bag(&bag1, "Piazza");
  add_to_bag(&bag1, "Benito");
  
  add_to_bag(&bag2, "coredump");
  add_to_bag(&bag2, "Benito");

  bag3 = bag_union(bag1, bag2);

  if( (size(bag3) != 3) && (count(bag3, "Benito")!=2) &&
      (count(bag3, "Piazza")!=1) && (count(bag3, "coredump")!=1)) {
    exit(FOUND_BUG);
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  clear_bag(&bag3);
  exit(CORRECT);
}
/* union - more elements */
static void test11(void) {
  Bag bag1;
  Bag bag2;
  Bag bag3;

  init_bag(&bag1);
  init_bag(&bag2);
  init_bag(&bag3);

  add_to_bag(&bag1, "Piazza");
  add_to_bag(&bag1, "Benito");
  add_to_bag(&bag1, "Benito");
  
  add_to_bag(&bag2, "coredump");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag2, "Piazza");

  bag3 = bag_union(bag1, bag2);

  if( (size(bag3) != 3) && (count(bag3, "Benito")!=4) &&
      (count(bag3, "Piazza")!=2) && (count(bag3, "coredump")!=1)) {
    exit(FOUND_BUG);
  }
  
  clear_bag(&bag1);
  clear_bag(&bag2);
  clear_bag(&bag3);
  exit(CORRECT);
}
/* union no bag1 */
static void test12(void) {
  Bag bag1;
  Bag bag2;
  Bag bag3;

  init_bag(&bag1);
  init_bag(&bag2);
  init_bag(&bag3);
  
  add_to_bag(&bag2, "coredump");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag2, "Benito");
  add_to_bag(&bag2, "Piazza");

  bag3 = bag_union(bag1, bag2);

  if( (size(bag3) != 3) && (count(bag3, "Benito")!=2) &&
      (count(bag3, "Piazza")!=1) && (count(bag3, "coredump")!=1)) {
    exit(FOUND_BUG);
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  clear_bag(&bag3);
  exit(CORRECT);
}
/* union add nothing */
static void test13(void) {
  Bag bag1;
  Bag bag2;
  Bag bag3;

  init_bag(&bag1);
  init_bag(&bag2);
  init_bag(&bag3);

  bag3 = bag_union(bag1, bag2);


  if( (size(bag3) != 0) && (count(bag3, "Benito")!=0) &&
      (count(bag3, "Piazza")!=0) && (count(bag3, "coredump")!=0)) {
    exit(FOUND_BUG);
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  clear_bag(&bag3);
  exit(CORRECT);
}

/* union add only bag1 */
static void test14(void) {
  Bag bag1;
  Bag bag2;
  Bag bag3;

  init_bag(&bag1);
  init_bag(&bag2);
  init_bag(&bag3);
  
  add_to_bag(&bag1, "Piazza");
  add_to_bag(&bag1, "Benito");
  add_to_bag(&bag1, "Benito");
    
  bag3 = bag_union(bag1, bag2);

  if( (size(bag3) != 2) && (count(bag3, "Benito")!=2) &&
      (count(bag3, "Piazza")!=1) && (count(bag3, "coredump")!=0)) {
    exit(FOUND_BUG);
  }

  clear_bag(&bag1);
  clear_bag(&bag2);
  clear_bag(&bag3);
  exit(CORRECT);
}

/* union "stress" test (kind of) */
static void test15(void) {
  Bag bag1, bag2, bag3;  
  char element[3];
  int i;

  init_bag(&bag1);
  init_bag(&bag2);
  init_bag(&bag3);

  for(i=1; i <= 50; i++) {
    sprintf(element, "%d", i);
    add_to_bag(&bag1, element);
  }
  for(i=51; i <= 100; i++) {
    sprintf(element, "%d", i);
    add_to_bag(&bag2, element);
  }			   
  bag3 = bag_union(bag1, bag2);
  
  if (size(bag3) != 100) {
    printf("Buggy bag- size() is wrong!\n");
    exit(FOUND_BUG);
  }
  clear_bag(&bag1);
  clear_bag(&bag2);
  clear_bag(&bag3);
  exit(CORRECT);
}

/* testing: count, remove_occurrence, (add_to_bag, clear_bag); */
static void test16(void) {
  Bag bag;

  init_bag(&bag);

  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "Nelson = CMSC216");

  printf("Count: %d\n", count(bag, "Nelsonito")); 

  if(remove_occurrence(&bag, "Nelsonito") == 1) {
    printf("Remove occurrence works\n");
  } else {
    printf("DOES NOT WORK\n");
    exit(FOUND_BUG);
  }

  printf("Count: %d\n", count(bag, "Nelsonito"));  

  clear_bag(&bag);
  exit(CORRECT);
}

/* testing: count, remove_occurrence, (add_to_bag, clear_bag); */
static void test17(void) {
  Bag bag;

  init_bag(&bag);

  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "Nelson = CMSC216");

  printf("Count: %d\n", count(bag, "Nelsonito")); 

  if(remove_occurrence(&bag, "Benito") == -1) {
    printf("Cannot remove 'Benito' - is not in bag\n");
  } else {
    printf("DOES NOT WORK\n");
    exit(FOUND_BUG);
  }

  printf("Count: %d\n", count(bag, "Nelsonito"));  

  clear_bag(&bag);
  exit(CORRECT);
}

/* testing: count, remove_occurrence, (add_to_bag, clear_bag); */
static void test18(void) {
  Bag bag;

  init_bag(&bag);

  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "CMSC216");

  printf("Count: %d\n", count(bag, "CMSC216")); 

  if(remove_occurrence(&bag, "CMSC216") == -1) {
    printf("Cannot remove last instance of 'CMSC216'\n");
  } else {
    printf("DOES NOT WORK\n");
    exit(FOUND_BUG);
  }

  printf("Count: %d\n", count(bag, "CMSC216"));  

  clear_bag(&bag);
  exit(CORRECT);
}

/* testing: count, remove_occurrence, (add_to_bag, clear_bag); */
static void test19(void) {
  Bag bag;

  init_bag(&bag);

  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "CMSC216");
  add_to_bag(&bag, "Nelsonito");
  add_to_bag(&bag, "Nelsonito");

  printf("Count: %d\n", count(bag, "Nelsonito")); 

  remove_occurrence(&bag, "Nelsonito");
  remove_occurrence(&bag, "Nelsonito");
  remove_occurrence(&bag, "Nelsonito");    
  
  if(remove_occurrence(&bag, "Nelsonito") == -1) {
    printf("Cannot remove last instance of 'Nelsonito'\n");
  } else {
    printf("DOES NOT WORK\n");
    exit(FOUND_BUG);
  }

  printf("Count: %d\n", count(bag, "Nelsonito"));  

  clear_bag(&bag);
  exit(CORRECT);
}


int main() {
  printf("Beginning testing...\n");
  test01();
  test02();

  

  test1();
  test2();
  test2_2();
  test3_2();
  test3();
  test4();
  test5();
  test6();
  test7();
  test8();
  test9();
  test9_2();
  test9_3();
  test10();
  test11();
  test12();
  test13();
  test14();
  test15();
  test16();
  test17();
  test18();
  test19();
  
  printf("No errors detected!\n");
  
  return CORRECT;
}
