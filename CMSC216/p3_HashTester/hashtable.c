/* Name: Ben Aronson
 * UID: 113548802
 * Date: 3/1/2016
 * Section: CMSC216 0301
 * Assignment: Project 2
 */

#include <stdio.h>
#include <string.h>
#include "hashtable.h"



/* Initialize table */
void init_table(Table *table) {
  int i;
  if(table != NULL) { 
    table->key_ct = 0;
    /* Assume capacity is NUM_BUCKETS */

    for(i = 0; i < NUM_BUCKETS; i++) {
      table->buckets[i].state = EMPTY;
    }
  }
  /* if table is NULL, nothing will happen */
}



/* Reset table to empty state */
void reset_table(Table *table) {
  
  if(table != NULL) {
    /* "...functionality of this function and init_table are the same." */
    init_table(table);
  }
  /* if table is NULL, nothing will happen */
}



/* Insert a key/value pair */
int insert(Table *table, const char *key, const char *val) {
  int i, pos = 0;
  char *p = NULL;
  
  if( (table == NULL) || (key == NULL) || (val == NULL) ) {
    return FAILURE;
  } else if( (strlen(key)  > MAX_STR_SIZE) || ( strlen(val) > MAX_STR_SIZE) ) {
    return FAILURE;
  } else {
    /*searching for key */

    if( search(table, key, p) == SUCCESS ) {
      /* overwrite value at pos of found key */
      /* find the position of they */
      for(i = hash_code(key) % NUM_BUCKETS; i < NUM_BUCKETS; i++) {
	if( strcmp(table->buckets[i].data.key, key) == 0) {
	  if( table->buckets[i].state == FULL) {
	    pos = i;
	  }
	}
      }
    
      for(i = 0; i < hash_code(key) % NUM_BUCKETS; i++) {
	if( strcmp(table->buckets[i].data.key, key) == 0) {
	  if( table->buckets[i].state == FULL) {
	   pos = i;
	  }
	}
      }
       
      strcpy(table->buckets[pos].data.value, val);
      return SUCCESS;
    } else {
      /* key is not in table */

      /* look for an EMPTY or DELETED bucket to place key in */
      for(i = hash_code(key) % NUM_BUCKETS; i < NUM_BUCKETS; i++) {
	if( table->buckets[i].state == EMPTY ||
	    table->buckets[i].state == DELETED ) {
	  
	  /* inserting key/value pair */
	  strcpy( table->buckets[i].data.key, key);
	  strcpy( table->buckets[i].data.value, val);
	  table->buckets[i].state = FULL;
	  table->key_ct++;
	  return SUCCESS;
	}
      }
      for(i = 0; i < hash_code(key) % NUM_BUCKETS; i++) {
	if( table->buckets[i].state == EMPTY ||
	    table->buckets[i].state == DELETED ) {
	  
	  /* inserting key/value pair */
	  strcpy( table->buckets[i].data.key, key);
	  strcpy( table->buckets[i].data.value, val);
	  table->buckets[i].state = FULL;
	  table->key_ct++;
	  return SUCCESS;
	}
      }
    }
  }
  return FAILURE;
}



int delete(Table *table, const char *key) {
  int pos = 0, i = 0, c = 0;
  char *p = NULL;

  if(table == NULL || key == NULL) {
    return FAILURE;
  } else {
    /* search for key */
    for(i = hash_code(key) % NUM_BUCKETS; i < NUM_BUCKETS; i++) {
      if( strcmp(table->buckets[i].data.key, key) == 0) {
     	if( table->buckets[i].state == FULL) {
	  /* use this to indicate if key is present ? */
	  c++;
	  pos = i;
	}
      }
    }
    
    for(i = 0; i < hash_code(key) % NUM_BUCKETS; i++) {
      if( strcmp(table->buckets[i].data.key, key) == 0) {
     	if( table->buckets[i].state == FULL) {
	  /* use this to indicate if key is present ? */
	  c++;
	  pos = i;
	}
      }
    }
    
    /* key was not found */
    if( search(table, key, p) == FAILURE ) {
      return FAILURE;
    } else {
      table->buckets[pos].state = DELETED;
      table->key_ct--;
      return SUCCESS;
    }
  }

  return FAILURE;
}





/* Search for a key in the table */
int search(Table *table, const char *key, char *val) {
  int i, c = 0, pos = 0;

  if(table == NULL || key == NULL) {
    return FAILURE;
  } else {
    for(i = hash_code(key) % NUM_BUCKETS; i < NUM_BUCKETS; i++) {
      if( strcmp(table->buckets[i].data.key, key) == 0) {
     	if( table->buckets[i].state == FULL) {
	  /* use this to indicate if key is present ? */
	  c++;
	  pos = i;
	}
      }
    }
    
    for(i = 0; i < hash_code(key) % NUM_BUCKETS; i++) {
      if( strcmp(table->buckets[i].data.key, key) == 0) {
     	if( table->buckets[i].state == FULL) {
	  /* use this to indicate if key is present ? */
	  c++;
	  pos = i;
	}
      }
    }
    /* if c > 0 - then key is found */
    if(val != NULL && c > 0) {
      /* key is present and val is not null ... copy) */
      strcpy(val, table->buckets[pos].data.value);
      return SUCCESS;
    } else if(val == NULL && c > 0) {
      /* "key is present and val is NULL...not copy... return 0" */
      return SUCCESS;
    } else {
      /* key is not found */
      return FAILURE;
    }  
  }
  return FAILURE; /*this may not be correct */
}




/* Return # of keys in table */
int key_count(Table *table) {
  if(table == NULL) {
    return FAILURE;
  } else {
    return table->key_ct;
  }
}




/* Return the number of buckets the table has */
int bucket_count(Table *table) {
  if(table == NULL) {
    return FAILURE;
  } else {
    return NUM_BUCKETS;
  }
}




unsigned long hash_code_aux(const char *str, int index) {

  if(index == 0) {
    return *str;
  }
  return (hash_code_aux(str, index - 1) * 65599) + (unsigned long)(str[index]);
  
}


/* Use hashcode algorithm to calculate hashcode */
unsigned long hash_code(const char *str) {
  /* index is end of the string (minus the 'null' later on) */
  int index = 0;
  
  if(str == NULL) {
    return SUCCESS;
  } else if(strlen(str) == 0) {
    return 0;
  } else {
    index = strlen(str); 
    return hash_code_aux(str, index-1);
  }
  
}
