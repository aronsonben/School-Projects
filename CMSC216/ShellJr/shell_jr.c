/* Implement your shell here */
/* Name: Ben Aronson
   UID: 113548802
   ID: baronson
   CMSC 216 Section 0301
   ShellJr Exercise
*/

#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <sysexits.h>
#include <string.h>
#include <err.h>
#include <unistd.h>
#include <sys/types.h>

#define MAX 1024  /* max string length */


int main() {
  char *argv2[MAX + 1];
  char buffer[MAX + 1];
  char *buff2 = NULL;  
  /*int j=0 x=0, c=0,;*/
  int len=0, i=0, e=0;
  pid_t pid = 0;
  
  /* print for check */
  /*
    for(x=0; x < (c=2); x++) {   
    printf("Argv2: %s\n", argv2[x]);
    }
  */
  
    
  printf("shell_jr: ");
  fflush(stdout);
    
  
  while(fgets(buffer, MAX + 1, stdin) != NULL ) {


      
    /* taking in command and parsing */
    len = strlen(buffer);
    for( ; i < len; i++) {
      if(buffer[i] == '\n') {
	buffer[i] = '\0';
      }
    }
    i=0;
    /*strtok separates strings at each " " -couldn't get sscanf to work */
    buff2 = strtok(buffer, " ");
    while(buff2 != NULL) {
      argv2[i] = buff2;
      i++;
      buff2 = strtok(NULL, " ");
    }
      
    /* putting null at the end */
    if(argv2[1] != NULL) {
      /* ex: wc location.txt */
      argv2[2] = NULL;
    } else{
      /* ex: date */
      argv2[1] = NULL;
    }
    
  


   
    if(strcmp(argv2[0], "exit") == 0) {
      /* if argv[0] == exit */
      printf("See you\n");
      fflush(stdout);
      exit(0);
      
    } else if(strcmp(argv2[0], "hastalavista") == 0) {
      /* if argv[0] == exit */
      printf("See you\n");
      fflush(stdout);
      exit(0);
      
    } else if(strcmp(argv2[0], "cd") == 0) {  
      /* if argv[0] == cd */
      e = chdir(argv2[1]);
      if(e == -1) {
	err(EX_OSERR, "Cannot change to directory %s", argv2[1]);
	fflush(stdout);
      }
      
    } else {
      /* dealing with the rest of the commands by forking */
      if((pid = fork()) < 0) {
	err(EX_OSERR, "fork error");
      }
      if(pid) { /* parent */
	wait(NULL);
	
      } else { /* child */
	execvp(argv2[0], argv2);
	printf("Failed to execute %s\n", argv2[0]);
	fflush(stdout);
	exit(EX_OSERR);
      }
    }
    printf("shell_jr: ");
    fflush(stdout);

  }
  
  return 0;
}


/*errx(EX_OSERR, "Failed to execute %s", argv2[0]);*/

/* Release test 3 & 4:

   3: cat location.txt
      exit

   4: cat location.txt
      hastalavista

*/
