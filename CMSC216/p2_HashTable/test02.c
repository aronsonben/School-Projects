#include "hashtable.h"
#include <stdio.h>
#include <string.h>

void test_assert(int test, const char* name, int idx)
{
   if (test) 
     printf("pass %s %d\n", name, idx);
   else printf("fail %s %d\n", name, idx);
}

int main(int argc, char* argv[])
{
  Table t;

  /* Initialize the table */
  init_table(&t);

  /* Insert into the table */
  test_assert(key_count(&t) == 0, "insert", 0);
  test_assert(!insert(&t, "202", "DC"), "insert", 1);
  test_assert(!insert(&t, "301", "W_MD"), "insert", 2);
  test_assert(key_count(&t) == 2, "insert", 3);
  test_assert(!insert(&t, NULL, "nor work"), "insert_null", 4);
  test_assert(key_count(&t) == 2, "insert", 5);
  test_assert(!insert(&t, "105", "Ben"), "insert_samekey", 6);
  test_assert(!insert(&t, "404", "Not found"), "insert", 7);
  test_assert(!insert(&t, "500", "last"), "insert", 8);
  test_assert(!insert(&t, "500", "not last any more!"), "insert_overwrite", 9);
  test_assert(!insert(&t, "202", "Boston"), "insert_ow2", 10);
  test_assert(key_count(&t) == 5, "full", 11);

  
  test_assert(!delete(&t, "500"), "delete", 12);
  test_assert(key_count(&t) == 4, "full-1", 13);
  test_assert(!search(&t, "301", NULL), "search", 14);
  
  reset_table(&t);
  test_assert(key_count(&t) == 0, "reset", 15);

  test_assert(!insert(&t, "202", "Boston"), "insert_again", 16);
  test_assert(!delete(&t, "500"), "fail_delete", 17);

  return 0;
}
