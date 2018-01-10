/* add an element to the Bag */
void add_to_bag(Bag *bag, const char *element) {
  Element *new = NULL, *current = NULL;
  int bsize = 0, i = 0, found = 0;
  
  /* "if bag or ele are NULL, return w/o changing anything*/
  if(bag == NULL || element == NULL) {
    return;
  }
  
  bsize = bag->bag_size;
  /*
  printf("size of Element: %lu\n", sizeof(Element));
  bag->elements[i] = malloc(sizeof(Element));
  if(bag->elements[i] == NULL) {
    printf("bag->elements[%d] malloc failed\n", i);
    return;
  }
  bag->elements[i]->key = malloc(sizeof(char) * (strlen(element) + 1));
  if(bag->elements[i]->key == NULL) {
    printf("bag->elements[%d] malloc failed\n", i);
    return;
  }
  */
  
  /*** try to FIND element in bag ***/
  printf("Size of bag: %d\n", bag->bag_size);
  for( ; i < bsize; i++) {
    /*    if(bag->elements[i] != NULL) {*/
      current = bag->elements[i];

      while(current != NULL) {
	/* if the current key equals the new key, it has been found */
	  
	if(strcmp(current->key, element) == 0) {
	  found = 1;
	  /*"make a cpy of str passed to element... (*) */
	  /*strcpy(new->key, element); */
	  break;
	} else {
	  current = current->next;
	}
      }
    
  }
  /*** end of FIND ***/



  if(current == NULL) { /* nothing there */
    
    current = malloc(sizeof(Element));
    if(current == NULL) {
      printf("'current' malloc failed\n");
      return;
    }
    current->key = malloc(sizeof(char) * (strlen(element) + 1));
    if(current->key == NULL) {
      printf("current->key malloc failed\n");
      return;
    }

    strcpy(current->key, element);
    /*
    current->occurrences = bag->elements[i]->occurrences;
    */
    current->occurrences++;
    current->next = NULL;
    bag->elements[i] = current;
    bag->bag_size++;
    printf("%s added to bag\n", element);
    printf("element at elements[%d]: %s\n", i, bag->elements[i]->key);
    printf("number of occurrences at elements[%d]: %d\n",
	   i, bag->elements[i]->occurrences);
    printf("-------------\n");
  } else if(found == 0) {

    printf("found == 0\n");
    
    new = malloc(sizeof(Element));
    if(new == NULL) {
      printf("found==0 and 'new' element malloc failed\n");
      return;
    }
    new->key = malloc(sizeof(char) * (strlen(element) + 1));
    if(new->key == NULL) {
      printf("new->key malloc failed\n");
      return;
    }

    strcpy(new->key, element);
    new->next = NULL;
    bag->elements[bsize+1] = new;
    bag->bag_size++;

  } else { /* found == 1 */
    bag->elements[i]->occurrences++;
  }

}


/* return number of occurrences of param element in param bag */
int count(Bag bag, const char *element) {
  Element *current = NULL;
  int found = 0, bsize = 0, i=0;
  
  if(element == NULL) { return -1; }
  /* if(bag == NULL) { return -1; } ???? */

  printf("Enter count\n");
  
  bsize = bag.bag_size;

  /** start FIND element ***/
  for(i=0; i < bsize; i++) {
    /*    if(bag->elements[i] != NULL) {*/
      current = bag.elements[i];

      while(current != NULL) {
	/* if the current key equals the new key, it has been found */
	  
	if(strcmp(current->key, element) == 0) {
	  found = 1;
	  break;
	} else {
	  current = current->next;
	}
      }
  }
  /*** end FIND element ***/
  if(found == 0) { return -1; } /* element not found / no occurrences */
  
  return 0; 
}
/*
  strcpy(current->key, element);
  current->occurrences = bag.elements[i]->occurrences;
  current->next = bag.elements[i]->next;
*/ 






/* clears the bag of everything */
void clear_bag(Bag *bag) {
  /*Element *current = NULL, *ele = NULL;*/
  int i = 0;
  
  if(bag == NULL) { return; }

  for( ; i < bag->bag_size; i++) {
    if(bag->elements[i] != NULL) {
      /*current = bag->elements[i];*/ /* set current = head at index i */
    

    /* while(current != NULL) {*/
    free(bag->elements[i]->key);
    free(bag->elements[i]);
    }
      /*
      ele = current->next;
      free(current->key);
      current->key = NULL;
      current->occurrences = 0;

      free(current);
      current = NULL;
      current = ele;
      
    }
      */
    bag->elements[i] = NULL;
  }
  bag->bag_size = 0;
}

int remove_occurrence(Bag *bag, const char *element) {
  Element *current = NULL;
  int bsize = 0, found = 0, i=0, occur=0;

  if(bag == NULL || element == NULL) { return -1; }

  bsize = bag->bag_size;
  
  /** start FIND element ***/
  for(i=0; i < bsize; i++) {
      current = bag->elements[i];

      while(current != NULL) {
	/* if the current key equals the new key, it has been found */
	  
	if(strcmp(current->key, element) == 0) {
	  found = 1;
	  break;
	} else {
	  current = current->next;
	}
      }
  }
  /*** end FIND element ***/

  if(found == 0) { /* element not in bag */
    return -1;
  } else {         /* element is in bag */

    occur = bag->elements[i]->occurrences;
    if(occur == 1 || occur == 0) { /* occur needs to be > 1 */
      return -1;
    } else {
      /* occur > 1; no error; remove one occurence; */
      bag->elements[i]->occurrences--;
    }
    /*bag->elements[i].occurrences--;*/
  }

  return bag->elements[i]->occurrences;
}


int remove_from_bag(Bag *bag, const char *element) {
  Element *current = NULL;
  int bsize = 0, found = 0, i=0, occur=0, j=0;

  if(bag == NULL || element == NULL) { return -1; }

  bsize = bag->bag_size;
  
  /** start FIND element ***/
  for(i=0; i < bsize; i++) {
      current = bag->elements[i];

      while(current != NULL) {
	/* if the current key equals the new key, it has been found */
	  
	if(strcmp(current->key, element) == 0) {
	  found = 1;
	  break;
	} else {
	  current = current->next;
	}
      }
  }
  /*** end FIND element ***/
  if(found == 0) { /* element not in bag */
    return -1;
  } else { /* element is in bag */
    occur = bag->elements[i]->occurrences;
    for(j=0; j < occur; j++) {
      bag->elements[i]->occurrences--;
    }
    bag->elements[i]->key = NULL;
    bag->elements[i]->next = NULL;
    bag->elements[i] = NULL;
    bag->bag_size--;

    /*
    free(bag->elements[i]->key);
    free(bag->elements[i])
    */
  }
  return 0;
}


/* test if bag1 is a subset (sub-'bag') to bag2 */
int is_sub_bag(Bag bag1, Bag bag2) {
  Element *b1curr = NULL, *b2curr = NULL;
  int bsize1 = 0, bsize2 = 0, i = 0;

  /*if(bag1 == NULL || bag2 == NULL) { return 0; }*/ /* check this ?????? */

  bsize1 = bag1.bag_size;
  bsize2 = bag2.bag_size;

  if(bsize1 == 0) {
    return 1;
  } else if(bsize1 > bsize2) {
    return 0;
  } else {

    for(i=0; i < bsize2; i++) {
      b1curr = bag1.elements[i];
      b2curr = bag2.elements[i];

      while(b1curr != NULL || b2curr != NULL) {
	
	if(strcmp(b1curr->key, b2curr->key) != 0) {
	  /* different names */
	  return 0;
	} else if((strcmp(b1curr->key, b2curr->key) == 0)) {
	  /*element is in both bags */
	  if(b1curr->occurrences > b2curr->occurrences) {
	    /* bag1 has more occurrences of this element than bag2 */
	    return 0;
	  }
	} else {
	  b1curr = b1curr->next;
	  b2curr = b2curr->next;
	}
	
      }
    }
  }

  return 1;
}


Bag bag_union(Bag bag1, Bag bag2) {
  


  return bag1;
}





  /*


  if(bag->head == NULL) {
    bag->head = temp;
    bag->head->next = NULL;
    bag->bag_size++;
    printf("added %s at beginning\n", bag->head->key);    
  } else {
    
    




    if(found == 1) {
      printf("Element was found, update occurrence.\n");
      temp->occurrences++;
      current = temp;
    } else {
      printf("Element was not found, update bag.\n");
      temp->occurrences++;
      printf("temp string: %s\n", temp->key);
             
      current = temp;      
      
      current->next = NULL;
      bag->bag_size++;
      printf("added %s to end\n", current->key);
    }
  }


  printf("-----------------\n");
  */





/*
      current = current->next;
      prev->next = temp;
      current = NULL;
      temp->next = current;
      */

/*
while(temp != NULL) {
    if(bag->head == NULL) {      
      bag->head = temp;
      bag->head->occurrences++;
      bag->head->next = NULL;
    } else if(bag->head == temp) { 
      bag->head->occurrences++;
    } else {         
      temp->next = bag->head;
      bag->head = temp;
    }
  }
*/

  /*** start FIND element ***/
  /*
  while(current != NULL) {
    if(strcmp(current->key, element) == 0) {
      found = 1;
      break;
    } else {
      current = current->next;
    }
  }
  */
  /*** end FIND element ***/
  /*
  if(bag->head == NULL) {  
    bag->head = temp;
    bag->head->next = NULL;
  } else if(found == 1) {  
    temp->occurrences++;
    current = temp;
    current->next = temp->next;
    bag->head = current; 
  } else {                  
    current = temp;
    current->next = NULL;
    bag->head = current;
  }
  */ 



