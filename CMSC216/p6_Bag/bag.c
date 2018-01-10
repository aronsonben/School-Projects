/* Ben Aronson - ID: baronson - UID: 113548802 
 * Project 6 - "bag.c"
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "bag.h"

/*static void add_to_front(Bag *bag, const char *element);*/

/******************** INIT BAG *************************/
/* initialize the Bag */
void init_bag(Bag *bag) {

  if(bag == NULL) { return; }

  bag->bag_size = 0;
  bag->head = NULL;
}

/************* PRINT BAG **************************/
/*static void print_bag(Bag *bag) {
  Element *current = NULL;

  current = bag->head;
  if(current == NULL) {
    printf("EMPTY\n");
  }
  while(current != NULL) {
    printf("current->key: %s - %d\n", current->key, current->occurrences);
    current = current->next;
  }
}
*/

/**************** ADD TO BAG *******************************/
/* add an element to the bag */
void add_to_bag(Bag *bag, const char *element) {
  Element *temp = NULL, *current = NULL, *prev = NULL;
  int found = 0;
  
  if(bag == NULL || element == NULL) {
    return;
  }
  
  current = bag->head;

  /** FIND **/
  while(current != NULL) {
      
    if(strcmp(current->key, element) == 0) { 
      found = 1;
      break;
    } else {
      prev = current;
      current = current->next;
    }
  }
  /** FIND **/

  if(found == 0) { /* when element is not found */
    /* create temp to hold new element item */
    temp = malloc(sizeof(Element));
    if(temp == NULL) {
      return;
    }
    temp->key = malloc(sizeof(char) * (strlen(element)+1));
    if(temp->key == NULL) {
      return;
    }
  
    strcpy(temp->key, element);
    temp->occurrences = 0;
    /* done creating temp */
    temp->occurrences++;
  }
  if(prev == NULL && found == 0) {  /* if current == NULL too I believe */
    /*temp->next = current;*/
    bag->head = temp;
    bag->head->next = NULL;
    bag->bag_size++;
  } else if(found == 1) {

    current->occurrences++;
    if(prev != NULL) {
      prev->next = current;
    }
  } else {
    temp->next = current;
    prev->next = temp;
    bag->bag_size++;
  }
}

/*********************** SIZE *********************************/
/* return size of bag (number of different elements) */
size_t size(Bag bag) {
  return bag.bag_size;
}


/********************** COUNT *******************************/
/* count number of occurrences of this element in the bag */
int count(Bag bag, const char *element) {
  Element *current = NULL;
  int found = 0, occur = 0;
  
  if( element == NULL) {
    return -1;
  }

  current = bag.head;
  /** FIND **/
  while(current != NULL) {
    if(strcmp(current->key, element) == 0) { 
      found = 1;
      break;
    } else {
      current = current->next;
    }
  }
  /** FIND **/

  if(found == 0) { return -1; } /* element not found */

  occur = current->occurrences; 
  
  return occur;
}

/***************** REMOVE OCCURRENCE ********************/
int remove_occurrence(Bag *bag, const char *element) {
  Element *current = NULL;
  int found = 0, occur = -1;

  if(bag == NULL || element == NULL) { return -1; }

  current = bag->head;
  /** FIND **/
  while(current != NULL) {
    if(strcmp(current->key, element) == 0) { 
      found = 1;
      break;
    } else {
      current = current->next;
    }
  }
  /** FIND **/

  if(found == 0) {
    return -1;
  } else {
    occur = current->occurrences;
    if(occur == 1) {  
      return -1; /* element must occur > 1 times. will never have 0 occur*/
    }
    current->occurrences--;
  }
  return current->occurrences;
}


/************** REMOVE FROM BAG ****************/
/* removes one element and all its occurrences from the bag */
int remove_from_bag(Bag *bag, const char *element) {
  Element *current = NULL, *prev = NULL;
  int found = 0, i=0, occur = 0;

  if(bag == NULL || element == NULL) { return -1; }
    
  current = bag->head;
  /** FIND **/
  while(current != NULL) {
    if(strcmp(current->key, element) == 0) { 
      found = 1;
      break;
    } else {
      prev = current;
      current = current->next;
    }
  }
  /** FIND **/

  if(found == 0) {   /* == if(current == NULL) */
    return -1; /* element not in bag */
  } else {     /* found == 1 */

    if(prev != NULL) {
      occur = current->occurrences;
      for(i=0; i < occur; i++) {
	current->occurrences--;
      }
      current->key = NULL;
      /* current == NULL */
      prev->next = current->next;
      bag->bag_size--;
    } else {
      occur = current->occurrences;
      for(i=0; i < occur; i++) {
	current->occurrences--;
      }
      current->key = NULL;
      /* current = NULL */
      bag->head = current->next;  /* deleted first item */
      bag->bag_size--;
    }
    free(current->key);
    free(current);
    
  }
  return 0;
}


/************* IS SUB BAG ***************************/
int is_sub_bag(Bag bag1, Bag bag2) {
  Element *b1curr = NULL, *b2curr = NULL;
  int b1size = -1, b2size = -1, found = 0;
  
  b1size = bag1.bag_size;
  b2size = bag2.bag_size;
  
  if(b1size == 0) {
    return 1;     /* "bag1 has no elements" = return 1 */
  } else if(b1size > b2size) {
    return 0;     /* bag1 cannot be sub bag of bag2 if it is bigger */
  } else {

    b1curr = bag1.head;
    b2curr = bag2.head;

    /** FIND **/
    while(b1curr != NULL) {
      
      
      while(b2curr != NULL) {

	if(strcmp(b1curr->key, b2curr->key) != 0) {
	  b2curr = b2curr->next;
	} else {

	  if(b1curr->occurrences <=  b2curr->occurrences) {
	    found++;
	    break;
	  } else {
	    b2curr = b2curr->next;
	  }
	}
	
      }
      b2curr = bag2.head;
      b1curr = b1curr->next;
    }
    /** FIND **/

    if(found == b1size) {
      return 1;
    } else {
      return 0;
    }
  }
}

/********************** BAG UNION *******************************/
/* create new bag that contains all elements from both bags */
Bag bag_union(Bag bag1, Bag bag2) {
  Bag new_bag;
  Element *curr1 = NULL, *curr2 = NULL, *temp = NULL;
  int found = 0, occur = -1;

  init_bag(&new_bag);

  if(bag1.bag_size == 0 && bag2.bag_size == 0) {
    return new_bag;
  }
  
  if(bag1.bag_size == 0) {
    /* if bag1 is empty, then just add bag2 to new_bag
       and return new_bag */
    curr2 = bag2.head;
    temp = curr2;
    occur = temp->occurrences;
    while(temp != NULL) {    
      add_to_bag(&new_bag, temp->key);
      
      /* deal with elements with occurrences > 1 */
      if(occur == 1) {
	temp = temp->next;
	if(temp != NULL) {
	  occur = temp->occurrences;
	}
      } else {
	/* won't move to next element if occur isn't 1 */
	occur--;
      }
    }
    return new_bag;
  }
  
  curr1 = bag1.head;
  temp = curr1;
  occur = temp->occurrences;
  while(temp != NULL) {
    add_to_bag(&new_bag, temp->key);
    /* deal with elements with occurrences > 1 */
    if(occur == 1) {
      temp = temp->next;
      if(temp != NULL) {
	occur = temp->occurrences;
      }
    } else {
      /* won't move to next element if occur isn't 1 */
      occur--;
    }
  }

  /* in case one is empty */
  if(bag2.bag_size == 0) {
    /* If bag2 size is 0, there are no more elements to add or 
       check for, so just return the new_bag that already added
       all of bag1's elements*/
    return new_bag;
  }

  /* reassign curr1 to point to the new bag */
  curr1 = new_bag.head;
  curr2 = bag2.head;
  /** FIND - bag2**/
  while(curr2 != NULL) {
    /* search for current bag2 element in new_bag */
    while(curr1 != NULL) {
       if(strcmp(curr2->key, curr1->key) == 0) {
	 found = 1; /* curr2 element found in new_bag */
	 break;
       } else {
	 curr1 = curr1->next;
       }       
    }
    
    if(found == 1) {
      /* element found in new_bag - increase occurrences */
      curr1->occurrences = curr1->occurrences + curr2->occurrences;
    } else {
      temp = curr2;
      add_to_bag(&new_bag, temp->key);
      /*add_to_front(&new_bag, temp->key);*/
    }
    
    curr2 = curr2->next;
    curr1 = new_bag.head;
  }  
  /** FIND - bag2 **/

  return new_bag;
}

/**************** CLEAR BAG **********************/
/* clears all the elements in the bag */
void clear_bag(Bag *bag) {
  Element *temp = NULL, *current = NULL;
  
  current = bag->head;

  while(current != NULL) {
    temp = current;
    current = current->next;
    free(temp->key);
    free(temp);
  }
  bag->head = NULL;
}


