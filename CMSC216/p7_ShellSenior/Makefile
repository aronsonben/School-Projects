# Name: Ben Aronson - ID: baronson - UID: 113548802
# CMSC216 - Section 0301 - Project 7
# "Makefile"

CC = gcc
CFLAGS = -ansi -Wall -g -O0 -Wwrite-strings -Wshadow -pedantic-errors \
	 -fstack-protector-all
PROGS = d8sh

.PHONY = all clean

all: $(PROGS)

clean:
	rm -f *.o $(PROGS) a.out

d8sh.o: d8sh.c executor.h lexer.h
	$(CC) $(CFLAGS) -c d8sh.c

lexer.o: lexer.c parser.tab.h
	$(CC) $(CFLAGS) -c lexer.c

parser.tab.o: parser.tab.c command.h
	$(CC) $(CFLAGS) -c parser.tab.c

executor.o: executor.c command.h executor.h
	$(CC) $(CFLAGS) -c executor.c

d8sh: d8sh.o executor.o lexer.o parser.tab.o
	$(CC) -o d8sh d8sh.o lexer.o parser.tab.o executor.o
