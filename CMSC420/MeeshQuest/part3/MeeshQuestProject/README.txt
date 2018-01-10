Benjamin Aronson
113548802

====================================================
= Citations ========================================
====================================================


***** Part 3 *****
******************

** AvlG Tree Credit **************************

Avl Delete inspiration/implementation:
http://www.geeksforgeeks.org/avl-tree-set-2-deletion/
http://users.cis.fiu.edu/~weiss/dsaajava3/code/AvlTree.java


PrivateEntryIterator "remove()" code from Java TreeMap source code:
http://grepcode.com/file_/repository.grepcode.com/java/root/jdk/openjdk/6-b14/java/util/TreeMap.java/?v=source










***** Part 2 *****
******************

AVL Tree Java implementation from: geeksforgeeks.com

TreeMap source code inspiration for SortedMap interface:
-grepcode.com TreeMap source code (http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b14/java/util/TreeMap.java)

SkipList source code inspiration for SortedMap interface implementation:
-SortedMapExample.jar file from class website

SortedMap interface implementation help:
-Java 7 APIs for SortedMap, TreeMap, AbstractMap

Inspiration for the following SortedMap methods from above grepcode.com TreeMap source code:
-containsKey(), successor(), predecessor(), getFirstEntry(), EntrySet.*, getEntry()
-- subMap(), SubMap.*, SubMap.containsValue()

Used TreeMap source code classes:
-PrivateEntryIterator, changed Node class to be similar to Entry<K, V>

Inspiration and some code borrowed from GitHub user "jamwjam" for testing AvlGTree.java:
https://github.com/jamwjam/MeeshQuestPart2/blob/master/src/cmsc420/sortedmap/AvlGSortedTests.java
--NOTE: While I did find these inspirational for my own testing, I didn't end up using very much of it

How to round a Float to 3 decimal points:
https://stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java

Help finding directions for shortestPath:
https://stackoverflow.com/questions/3365171/calculating-the-angle-between-two-lines-without-having-to-calculate-the-slope
https://stackoverflow.com/questions/9970281/java-calculating-the-angle-between-two-points-in-degrees
https://stackoverflow.com/questions/26076656/calculating-angle-between-two-points-java
https://math.stackexchange.com/questions/1201337/finding-the-angle-between-two-points

Function "round()" used to round decimal values in shortest path (when finding length).
- credit due to StackOverflow user "Jonik", here:
https://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places

**** PMQuadtree stuff ****
Some helper methods in PMQuadtree taken from PRQuadtree Part1 Canonical (setRange, getSpatial*, etc.)

Inspiration for "processMapRoad()" (and other similar process*() methods) taken from
part 1 canonical process commands. Also error checking in those methods and the ones that
they call, like add() in the PMQuadtree, taken from PRQuadtree part 1 canonical solution.

All Node classes inspired by PRQuadtree part 1 canonical Node classes (such as using type flags
to identify type of Node). -- Used the PRQuadtree part 1 canonical constructor code
heavily in creation of GrayNode constructor.

Dijkstra's Algorithm inspiration:
http://math.mit.edu/~rothvoss/18.304.3PM/Presentations/1-Melissa.pdf
http://www.geeksforgeeks.org/greedy-algorithms-set-6-dijkstras-shortest-path-algorithm/


***** Part 1 *****
******************
--Some guidance on how to implement a PRQuadtree (no code copied, just used to get an idea of how to get started)--
PRQuadtree notes: http://courses.cs.vt.edu/~cs3114/Spring10/Notes/T06.PRQuadTrees.pdf
PRQuadtree implementation notes: http://courses.cs.vt.edu/~cs3114/Spring10/Notes/T07.PRQuadTreeImplementation.pdf
PRQuadtree implementation example: http://algs4.cs.princeton.edu/92search/QuadTree.java.html

-User TA notes/suggestions from class to work with insert and remove from PRQuadtree.
-Samet's algorithm idea for the nearestCity function
