/* Name: Ben Aronson
 * UID: 113548802
 * ID: baronson
 * CMSC216
 * Project 3
 */


#include <stdio.h>
#include <sysexits.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include "hashtable.h"

#define MAX 1024


static int check_char(char line[]) {
  int i = 0, length = 0, nonspace = 0;
 
  if(line != NULL) {
    length = strlen(line);
    for(i = 0; i < length-1; i++) {
      if(line[i] != ' ') {
	nonspace = 1;
      }
    }
  }
  return nonspace;
}

static void reset_char(char cmd[], char key[], char val[]) {
  cmd[0] = '\0';
  key[0] = '\0';
  val[0] = '\0';
}


int main(int argc, char *argv[]) {
  FILE *input;
  char line[MAX + 1], key[MAX + 1], val[MAX + 1], cmd[MAX + 1], s_val[MAX+1];
  int len = 0, passfail = 0, i = 0;
  Table tab;
  
  init_table(&tab);

  /* Deciding where input will come from */
  if(argc == 1) {
   
    input = stdin; /* standard input */
    
  } else if(argc == 2) {
    /* read data from that file */
    input = fopen(argv[1], "r");
    
    if(input == NULL) {
      /* might need to change this */
      fprintf(stderr, "File %s open failed. Error: %s\n",
	      argv[1], strerror(errno));

      return EX_OSERR;
    }
  } else {
    /* More than 2 args - might need to delete this stuff */
    fprintf(stderr, "Usage: a.out\n");
    fprintf(stderr, "Usage: aout <filename>\n");
    return EX_USAGE; 
  }

    
    while( fgets(line, MAX + 1, input) != NULL ) {
      len = strlen(line);
      
      if(len < (MAX + 1) ) {
	sscanf(line, "%s %s %s", cmd, key, val);


	/*printf("CMD: %s\n KEY: %s\n VAL: %s\n", cmd, key, val);*/
	if( check_char(line) == 0) {
	  /* if line of white space */
		  
	} else if( strcmp(cmd, "#")  == 0) {
	  /* if line is comment */
	  
	} else if( strcmp(cmd, "insert") == 0 ) {
	  /* insert command */
	  

	  if(key[0] == '\0' || val[0] == '\0') {
	    fprintf(stderr, "Not enough arguments.\n");
	    return EX_DATAERR;
	  }
	 
	  
	  passfail = insert(&tab, key, val);
	  if(passfail == FAILURE) {
	    printf("Insertion of %s => %s failed.\n", key, val);
	  } else {
	    printf("Insertion of %s => %s succeeded.\n", key, val);
	  }
	} else if( strcmp(cmd, "search") == 0) {
	  /* search command */

	  if(key[0] == '\0') {
	    fprintf(stderr, "Not enough arguments.\n");
	    return EX_DATAERR;
	  }

	  passfail = search(&tab, key, s_val);
	  if(passfail == FAILURE) {
	    printf("Search for %s failed.\n", key);
	  } else {
	    printf("Search for %s succeeded (%s).\n", key, s_val);
	  }
	} else if( strcmp(cmd, "delete") == 0 ) {
	  /* delete command */

	  if(key[0] == '\0') {
	    fprintf(stderr, "Not enough arguments.\n");
	    return EX_DATAERR;
	  }
	  
	  passfail = delete(&tab, key);
	  if(passfail == FAILURE) {
	    printf("Deletion of %s failed.\n", key);
	  } else {
	    printf("Deletion of %s succeeded.\n", key);
	  }
	} else if( strcmp(cmd, "reset") == 0 ) {
	  /* reset command */

	  reset_table(&tab);

	  printf("Table reset.\n");
	} else if( strcmp(cmd, "display") == 0) {
	  
	  if(key[0] == '\0') {
	    fprintf(stderr, "Not enough arguments.\n");
	    return EX_DATAERR;
	  }
	  
	  if( strcmp(key, "key_count") == 0 ) {
	    printf("Key count: %d\n", tab.key_ct);

	  } else  {
	    /* when the command is "display table" */

	    
	    for(i = 0; i < NUM_BUCKETS; i++) {
	      
	      if(tab.buckets[i].state == EMPTY) {
		printf("Bucket %d: EMPTY\n", i);
		
	      } else if(tab.buckets[i].state == DELETED) {
		printf("Bucket %d: DELETED\n", i);
		
	      } else {
		/* When it is FULL */
		printf("Bucket %d: FULL ", i);
		printf("(%s => %s)\n", tab.buckets[i].data.key,
		       tab.buckets[i].data.value);
	      } 
	    }
	  }
	} else {
	  /* print an error or "invalid command or something" */
	  fprintf(stderr, "Invalid command '%s'. Error %s\n", cmd,
		  strerror(errno));
	  
	}
	  
      }  else {

	/* line is longer than 1024 */

	fprintf(stderr, "Invalid line. Error %s\n", strerror(errno));
	return EX_DATAERR;
      }
	

    
      reset_char(cmd, key, val);
    
    }
 



  
    /* close the stream */
    fclose(input);
  

    return 0;
}
