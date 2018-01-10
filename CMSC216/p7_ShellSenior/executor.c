/* Ben Aronson - baronson - 113548802 */
/* CMSC216 - 0301 - Project 7         */

#include <stdio.h>
#include <stdlib.h>
#include <sysexits.h>
#include <err.h>
#include <unistd.h>
#include <string.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include "command.h"
#include "executor.h"

/*static void print_tree(struct tree *t);*/
/*static void aux(char *input, char *output);*/

/* auxiliary method for the execute function. 
 * Note: ppifd/ppofd = parent_process_input(/output)_fd
 */
int execute_aux(struct tree *tree, int ppifd, int ppofd) {
  int fdin = -1, fdout = -1, status;
  pid_t pid1, pid2, pid3; /*pid2&3 used in PIPE */
  int pipe_fd[2];

  fdin = ppifd;
  fdout = ppofd;
  
  if(tree != NULL) {

    /* if the input isn't null, open fdin */
    if(tree->input != NULL) {
      if((fdin = open(tree->input, O_RDONLY)) < 0) {
	perror("error opening file");
	/*exit(EX_OSERR)*/
      }
    }
    /* if the output isn't null, open fdout */
    if(tree->output != NULL) {
      if((fdout = open(tree->output, O_WRONLY|O_TRUNC|O_CREAT, 0664)) < 0) {
	perror("error opening file");
	/*exit(EX_OSERR)*/
      }
    }

    if(tree->conjunction == NONE) {
      
      if(strcmp(tree->argv[0], "exit") == 0) {
	exit(0);
      } else if(strcmp(tree->argv[0], "cd") == 0) {
	
	if(tree->argv[1] == NULL) {

	  /*if argv[1] is NULL, change to home directory */
	  if((chdir(getenv("HOME"))) < 0) {
	    perror("Failed to change directory");
	    exit(EX_OSERR);
	  }
	  
	} else {

	  /* change to argv[1], unless it doesn't exit then it is invalid */
	  if((chdir(tree->argv[1])) < 0) {
	    perror("Invalid directory");
	    printf("%s\n", tree->argv[1]);
	    fflush(stdout);
	    exit(EX_OSERR);
	  }
	  
	}
      } else {
	/* if not 'exit' or 'cd' then fork like in shell_jr */
	if( (pid1 = fork()) < 0) {
	  perror("fork error");
	  exit(EX_OSERR);
	}
	if(pid1) { /* parent */
	  wait(&status);
	  

	  if( (WIFEXITED(status)) && WEXITSTATUS(status) == 0) {
	    return 0; 
	  } else {
	    return 1;
	  }
	} else { /* child */
	  /* if fdin is not the standard input, we will redirect it */
	  if(fdin != STDIN_FILENO) {
	   
	    /* redirect fdin to STDIN */
	    if(dup2(fdin, STDIN_FILENO) < 0) {
	      perror("dup2 (read) failed");
	      exit(EX_OSERR);
	    }
	    close(fdin);
	  }
	  /* close to prevent resource leak */
	  	  	 
	  /* if fdout is not the standard output, we will redirect it */
	  if(fdout != STDOUT_FILENO) {
	    
	    /* redirect fdout to STDOUT */
	    if(dup2(fdout, STDOUT_FILENO) < 0) {
	      perror("dup2 (write) failed");
	      exit(EX_OSERR);
	    }
	    close(fdout);	    
       	  }
	  
	  
	  /* now execute command - similar to shelljr */
	  execvp(tree->argv[0], (char * const *)tree->argv);
	  printf("Failed to execute %s\n", tree->argv[0]);
	  fflush(stdout);
	  exit(EX_OSERR);
	}

      }
    } else if(tree->conjunction == AND) {
      if(execute_aux(tree->left, fdin, fdout) == 0) {
	/* make sure execute returns correctly */
	if(execute_aux(tree->right, fdin, fdout) != 0) {
	  return 1;
	}
      }
    } else { /* PIPE */
      /* some ideas taken from EngToFre example */

      /* before the fork, we create the pipe */
      if(pipe(pipe_fd) < 0) {
	perror("pipe error");
	exit(EX_OSERR);
      }

      if( (pid2 = fork()) < 0) {
	perror("fork error");
	exit(EX_OSERR);
      }

      if(pid2 == 0) { /* CHILD #1 CODE ****************/
	close(pipe_fd[0]);  /* don't need read end */

	if(dup2(pipe_fd[1], STDOUT_FILENO) < 0) {
	  perror("dup2 error");
	  exit(EX_OSERR);
	}
	/* release resource */
	close(pipe_fd[1]);

	/* execute command */
	if(execute_aux(tree->left, fdin, fdout) != 0) {
	  printf("Execute failed \n");
	  fflush(stdout);
	  exit(EX_OSERR);
	}
	/*exit child code */
	exit(0);
      } else {         /* parent code */

	if( (pid3 = fork()) < 0) {
	  perror("fork error");
	  exit(EX_OSERR);
	}

	if(pid3 == 0) {  /* CHILD #2 CODE ************/
	  close(pipe_fd[1]);  /* don't need pipe's write end */

	  /* redirect standard input to pipe read end */
	  if(dup2(pipe_fd[0], STDIN_FILENO) < 0) {
	    perror("dup2 error");
	    exit(EX_OSERR);
	  }
	  /* releasing resource */
	  close(pipe_fd[0]);

	  /*printf("right: about to execute: %s\n", tree->right->argv[0]);*/
	  /* execute the command */
	  if(execute_aux(tree->right, fdin, fdout) != 0) {
	    printf("Execute failed \n");
	    fflush(stdout);
	    exit(EX_OSERR);
	  }
	  
	  /* exit child code */
	  exit(0);
	} else {       /* parent code */
	  /* parent has no need for pipe */
	  close(pipe_fd[0]);
	  close(pipe_fd[1]);

	  /* reap each child */
	  wait(NULL);
	  wait(NULL);
	}
      }
    }
  } /* if tree != null */

  return 0;
}

int execute(struct tree *t) {
  /*print_tree(t);*/
  return execute_aux(t, STDIN_FILENO, STDOUT_FILENO);
}

/*
static void print_tree(struct tree *t) {
   if (t != NULL) {
      print_tree(t->left);

      if (t->conjunction == NONE) {
         printf("NONE: %s, ", t->argv[0]);
      } else {
         printf("%s, ", conj[t->conjunction]);
      }
      printf("IR: %s, ", t->input);
      printf("OR: %s\n", t->output);

      print_tree(t->right);
   }
}
*/
