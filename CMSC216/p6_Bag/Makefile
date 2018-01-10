#Ben Aronson - ID: baronson - UID: 113548802
#Project 6 - Makefile

CC = gcc
CFLAGS = -ansi -Wall -g -O0 -Wwrite-strings -Wshadow -pedantic-errors \
	 -fstack-protector-all
PROGS = public01 public02 public03 public04 tester

.PHONY: all clean

all: $(PROGS)

clean:
	rm -f *.o $(PROGS) a.out

$(PROGS): bag.o my_memory_checker_216.o


public01: public01.o bag.o
	$(CC) -o public01 public01.o bag.o

public02: public02.o bag.o
	$(CC) -o public02 public02.o bag.o

public03: public03.o bag.o
	$(CC) -o public03 public03.o bag.o

public04: public04.o bag.o my_memory_checker_216.o 
	$(CC) -o public04 public04.o bag.o my_memory_checker_216.o 

public01.o: public01.c bag.h
	$(CC) $(CFLAGS) -c public01.c

public02.o: public02.c bag.h
	$(CC) $(CFLAGS) -c public02.c

public03.o: public03.c bag.h
	$(CC) $(CFLAGS) -c public03.c

public04.o: public04.c bag.h my_memory_checker_216.h
	$(CC) $(CFLAGS) -c public04.c my_memory_checker_216.h

my_memory_checker_216.o: my_memory_checker_216.c my_memory_checker_216.h
	$(CC) $(CFLAGS) -c my_memory_checker_216.c

bag.o: bag.c bag.h
	$(CC) $(CFLAGS) -c bag.c

tester: tester.o bag.o
	$(CC) -o tester tester.o bag.o

tester.o: tester.c bag.h
	$(CC) $(CFLAGS) -c tester.c

