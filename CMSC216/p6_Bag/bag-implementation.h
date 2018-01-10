/* Ben Aronson - ID: baronson - UID: 113548802
 * Project 6 - "bag-implementation.h"
 */

#ifndef BAG_IMPLEMENTATION_H
#define BAG_IMPLEMENTATION_H

/* using a linked list */

/* element will have key (i.e. name of animal) and pointer to next element */
typedef struct element {
  char *key;      /* name of Element */
  int occurrences;
  struct element *next;
} Element; 
/* structure "Element" contains the name of the element and how many
   times it occurs, as well as the next element */

/* bag structure has elements, key count and bag size */
typedef struct {
  int bag_size;
  Element *head;
} Bag;

#endif

