CC = gcc
CFLAGS = -ansi -Wall -g -O0 -Wwrite-strings -Wshadow -DNUM_BUCKETS=5 \
-pedantic-errors -fstack-protector-all 
PROGS = ster public01 public02 public03 public04 public05

.PHONY: all clean

all: $(PROGS)

clean:
	rm -f *.o $(PROGS)

$(PROGS): hashtable.o
public%: public%.o

hashtable.o: hashtable.h
public%.o: hashtable.h
