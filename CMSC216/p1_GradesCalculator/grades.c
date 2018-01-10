/* Name: Ben Aronson
 * UID: 113548802
 * Directory ID: baronson
 */
#include <stdio.h>
#include <math.h>

#define MAX_ASSN 50;


static int stats_bool(char stat) {
  if(stat == 'y' || stat == 'Y') {
    return 1;
  } else {
    return 0;
  }
}

static double  mean_calc(int tot_grade, int no_of_assn) {
  double  mean = 0;
  mean = (tot_grade / no_of_assn);
  return mean;
}

/*calculate variance */
static double variance(int gr_arr[], double  mean, int no_of_assn) {
  int i = 0;
  double var1 = -1.0;
  double var1sq = -1.0;
  double var1mean = 0;
  double tot_vartop = 0;
  double avg  = 0;

  for(i = 0; i < no_of_assn; i++) {
    var1 = gr_arr[i];
    var1mean = var1 - mean;
    var1sq = var1mean * var1mean;
    tot_vartop = tot_vartop + var1sq;
  }
  avg = tot_vartop / no_of_assn;
  return avg;
}

static float stdev_calc(double var) {
  double root = 0;
  root = sqrt(var);
  return root;
}

static int penalty_calc(int grade, int days_late, int penalty) {
  int tot_penalty = -1;
  int new_grade = -1;
  tot_penalty = days_late * penalty;
  new_grade = grade - tot_penalty;
  if(new_grade < 0) {
    new_grade = 0; 
  }
  return new_grade;
}

static double num_score_calc(int grade, double weight) {
  double ns = 0;
  ns = grade * weight;
  return ns; 
}

static double numeric_score(int num_assn, int grades[], int  wts[], int drop_amnt) {
  double temp; double wt_d; int i; double score;
  double total_wt;
  
  /*go in here if dropping something */
  if(drop_amnt > 0) {
    /* calculate total of weight used */
    for(i = 0; i < num_assn-drop_amnt; i++) {
      total_wt = total_wt + wts[i];
    }
    total_wt = total_wt * (0.01);
    
    for(i = 0; i < num_assn-drop_amnt; i++) {
      wt_d = wts[i] * (0.01);
      temp = num_score_calc(grades[i], wt_d);
      score = temp + score;
    }
    score = score / total_wt;

    /* otherwise go in here */
  } else {
    /* calculate total of weight used */
    for(i = 0; i < num_assn; i++) {
      total_wt = total_wt + wts[i];
    }
    total_wt = total_wt * (0.01);
    
    for(i = 0; i < num_assn; i++) {
      wt_d = wts[i] * (0.01);
      temp = num_score_calc(grades[i], wt_d);
      score = temp + score;
    }
    score = score / total_wt;
  }
  return score; 
}


int same_val(int no_of_assn, int val_arr[], int assn_arr[]) {
  int tmp, i, j;  
      
  for(i = 0; i < no_of_assn; i++) {
    tmp = val_arr[i];
    for(j = 0; j < no_of_assn; j++) {
      if(tmp == val_arr[j]) {
	if(assn_arr[i] != assn_arr[j]) {
	  return 1;
	}
      }
    }
  }
  return 0; 
}


static int qsort_partition(int value_array[], int ldex, int rdex, int no_of_assn,
			   int assn_arr[], int gr_arr[], int wt_arr[], int late_arr[]) {
  int l = ldex; int r = rdex; int temp;
  int mid = (l + r) / 2; 
  int piv = value_array[mid];

  
  while( l <= r ) {
    if(value_array[l] < piv) {
      l++;
    }
    if(value_array[r] > piv) {
      if(r < no_of_assn-1) {
	r++;
      }
    }
    if(l <= r) {

      if(same_val(no_of_assn, value_array, assn_arr)==1) {
	
	temp = value_array[l];
	value_array[l] = value_array[r];
	value_array[r] = temp;
      }

      
      temp = assn_arr[l];
      assn_arr[l] = assn_arr[r];
      assn_arr[r] = temp;

      temp = gr_arr[l];
      gr_arr[l] = gr_arr[r];
      gr_arr[r] = temp;

      temp = wt_arr[l];
      wt_arr[l] = wt_arr[r];
      wt_arr[r] = temp;

      temp = late_arr[l];
      late_arr[l] = late_arr[r];
      late_arr[r] = temp; 
   
      
      l++; r--;
    }
  }
  return l;
}


static void my_qsort(int value_array[], int l, int r, int no_of_assn,int assn_arr[],
		     int gr_arr[], int wt_arr[],int late_arr[]) {
 
  int k = qsort_partition(value_array, l, r, no_of_assn, assn_arr, gr_arr,
			  wt_arr, late_arr);
  
  if(l < r) {
   
    my_qsort(value_array, l, k-1, no_of_assn, assn_arr, gr_arr, wt_arr,
	     late_arr); /*left side*/
    
    my_qsort(value_array, k+1, r, no_of_assn, assn_arr, gr_arr, wt_arr,
	     late_arr);/* right side*/
  }
}

static void after_drop_calc(int drop, int num_assn, int up_arr[], int after[]) {
  int i;

  for(i = drop; i < num_assn; i++) {
    after[i-drop] = up_arr[i];
  }
}
/*
static void after_drop_calc2(int drop, int num_assn, int up_arr[], int after[], int assn[]) {
  int i;
  
    for(i = drop; i < num_assn; i++) {
      after[i-drop] = up_arr[i];
    }
  
}
*/

int main() {
  int penalty_points = 0, drop_amnt = -1.0;
  int no_of_assn = -1;
  double score = 0.0;
  char stats = 'o';
  int weight = -1, day_late = 0, grade = 0, assn_no = -1;
  int wt_arr[50]; int wt_after[50]; int wt_arr2[50];
  int late_arr[50]; int late_after[50]; int late_arr2[50];
  int gr_arr[50]; int gr_after[50]; int gr_arr3[50]; int gr_droppen_arr[50];
  int assn_arr[50]; int assn_after[50]; int assn_arr2[50];
  int gr_arr2[50];
  /* char st_ans = 'o';*/
  int full_wt = 0;
  double tot_grade = 0;
  double  mean = -1.0;
  double varianc = -1.0;
  double stdev = -1.0;
  int i; int g; int w; int val;
  int val_arr[50]; int val_after[50];

  
  scanf(" %d %d %c", &penalty_points, &drop_amnt, &stats);
  scanf(" %d", &no_of_assn);

  
  /* taking care of assignments */
  for(i = 0; i < no_of_assn; i++) {
    scanf("%d, %d, %d, %d", &assn_no, &grade, &weight, &day_late);
    
    assn_arr[assn_no-1] = assn_no;
    gr_arr[assn_no-1] = grade;
    wt_arr[assn_no-1] = weight;
    late_arr[assn_no-1] = day_late;
    
    full_wt = full_wt + weight;
  }
  
  /*calling function to check for 100% weight accounted for*/
  if(full_wt != 100) {
    printf("ERROR: Invalid values provided\n");
  } else {

 
  
    /************************* drop assignments *************************/

    if(drop_amnt > 0) {
      /*computing assignment values */
      for(i = 0; i < no_of_assn; i++) {
	g = gr_arr[i];
	w = wt_arr[i];
	val = g * w;
	val_arr[i] = val;
      }

      /*make copies of all the arrays*/
      for(i = 0; i < no_of_assn; i++) {
	assn_arr2[i] = assn_arr[i];
      }
      for(i = 0; i < no_of_assn; i++) {
	gr_arr3[i] = gr_arr[i];
      }
      for(i = 0; i < no_of_assn; i++) {
	wt_arr2[i] = wt_arr[i];
      }
      for(i = 0; i < no_of_assn; i++) {
	late_arr2[i] = late_arr[i];
      }


      /*sorting values (and everything else) */
      my_qsort(val_arr, 0, no_of_assn-1, no_of_assn, assn_arr2, gr_arr3, wt_arr2, late_arr2);
      /* All arrays now sorted */

     
      /***** Finding Numeric Value for Dropped Cases *****/

      if(same_val(no_of_assn, val_arr, assn_arr) == 1) {
	/*one with lowest assn no. can be dropped */
	
      }
      
      /*Make a new array without the dropped cases */
  

      /* assembling arrays of only the NON dropped assignments*/

      /*
      after_drop_calc2(drop_amnt, no_of_assn, val_arr, val_after, assn_arr);
      after_drop_calc(drop_amnt, no_of_assn, assn_arr2, assn_after);
      after_drop_calc(drop_amnt, no_of_assn, gr_arr3, gr_after);
      after_drop_calc(drop_amnt, no_of_assn, wt_arr2, wt_after);
      after_drop_calc(drop_amnt, no_of_assn, late_arr2, late_after);
      */
      
      after_drop_calc(drop_amnt, no_of_assn, val_arr, val_after);
      after_drop_calc(drop_amnt, no_of_assn, assn_arr2, assn_after);
      after_drop_calc(drop_amnt, no_of_assn, gr_arr3, gr_after);
      after_drop_calc(drop_amnt, no_of_assn, wt_arr2, wt_after);
      after_drop_calc(drop_amnt, no_of_assn, late_arr2, late_after);
      
      
    }

  
    /****************************************************************/

    /******* Days Late and Penalty *********/

    if(drop_amnt > 0) {
      for(i = 0; i < no_of_assn-drop_amnt; i++) {
	for(i = 0; i < no_of_assn-drop_amnt; i++) {
	  /* initializing GRADE ARRAY AFTER DROP HERE */
	  gr_droppen_arr[i] = penalty_calc(gr_after[i], late_after[i], penalty_points);
	}
      }
    } else{
      for(i = 0; i < no_of_assn; i++) {
	/* printf("Grade at i: %d\n", gr_arr[i]); delete this later */

	/* initializing GRADE ARRAY 2 HERE */
	gr_arr2[i] = penalty_calc(gr_arr[i], late_arr[i], penalty_points);
    
      }
    }
 


  
    /********* Numeric Score ************/
    if(drop_amnt > 0) {
      /*
	for(i = 0; i < no_of_assn-drop_amnt; i++) {
	gr_after2[i] = gr_after[i]; 
	}
	for(i = 0; i < no_of_assn-drop_amnt; i++) {
	printf("Grade After Array 2: %5.4f\n", gr_after2[i]);
	}
	score = numeric_score(no_of_assn, gr_after2, wt_after, drop_amnt);
      */
   
      score = numeric_score(no_of_assn, gr_droppen_arr, wt_after, drop_amnt);
    } else {    
      score = numeric_score(no_of_assn, gr_arr2, wt_arr, drop_amnt);
    }
    /*
      for(i = 0; i < no_of_assn; i++) {
      grade_percent = (gr_arr2[i] * .01);
      wt_d = wt_arr[i];
      temp = num_score_calc(grade_percent, wt_d);
      score = temp + score;
      }
    */

    /***** Output ****/
    if(drop_amnt > 0) {
      printf("Numeric Score: %5.4f\n", score);

      printf("Points Penalty Per Day Late: %d\n", penalty_points);
 
      printf("Number of Assignments Dropped: %d\n", drop_amnt);
  
      printf("Values Provided: \n");

      /*printing assignment data */
      printf("Assignment, Score, Weight, Days Late\n");
      for(i = 0; i < no_of_assn; i++) {
	printf("%d, %d, %d, %d\n", assn_arr[i], gr_arr[i], wt_arr[i], late_arr[i]);
      }
    } else {
    
      /*Print this stuff when not dropping anything (used for variable differences) */
      printf("Numeric Score: %5.4f\n", score);

      printf("Points Penalty Per Day Late: %d\n", penalty_points);
 
      printf("Number of Assignments Dropped: %d\n", drop_amnt);
  
      printf("Values Provided: \n");

      /*printing assignment data */
      printf("Assignment, Score, Weight, Days Late\n");
      for(i = 0; i < no_of_assn; i++) {
	printf("%d, %d, %d, %d\n", assn_arr[i], gr_arr[i], wt_arr[i], late_arr[i]);
      }
    }
    
  
    /********** STATS ************/
  
    if(drop_amnt > 0) {
      for(i = 0; i < no_of_assn; i++) {
        gr_arr2[i] = penalty_calc(gr_arr[i], late_arr[i], penalty_points);
      }
    }
    /*first, find total grade divided by # assignments AFTER penalty */
    for(i = 0; i < no_of_assn; i++) {
      tot_grade = tot_grade + gr_arr2[i];
    }
    
    /*calculating the mean */
    mean = mean_calc(tot_grade, no_of_assn);

    /*for(i = 0; i < no_of_assn; i++) { 
      printf(" %d ", gr_arr[i]); 
      }*/
 
  
    /*calculate the variance (used for stdev_calc) */
    varianc = variance(gr_arr2, mean, no_of_assn);

    /* calculating the standard deviation */
    stdev = stdev_calc(varianc);
  

    if(stats_bool(stats) == 1) {
      /* final output */
      printf("Mean: %5.4f, Standard Deviation: %5.4f\n", mean, stdev);
    }
  }
  return 0;
}
