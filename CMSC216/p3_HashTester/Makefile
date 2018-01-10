CC = gcc
CFLAGS = -ansi -Wall -g -O0 -Wwrite-strings -Wshadow -DNUM_BUCKETS=5 \
         -pedantic-errors -fstack-protector-all 
PROGS = hashtester public01 public02 public03

.PHONY: all clean

all: $(PROGS)

clean:
	rm -f *.o $(PROGS)

$(PROGS): hashtable.o
hashtester: hashtester.o
public%: public%.o

hashtester.o: hashtable.h
hashtable.o: hashtable.h
public%.o: hashtable.h
