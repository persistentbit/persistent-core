# persistentbit.com

## Java persistent datastructures.

Provides efficient persistent data structures for java including:
 Linked List,Set,Map and List.
These persistent structures are based on code used in Clojure. 
The Collections and streams can easily be converted to immutable java HashMap,HashSet,  ArrayLists or Iterables. 

## Persistent Streams.

Every Persisten Collections is a Persistent Stream(PStream) and every Iterable can be converted to a Persisten Stream. 
Every PStream has the following methods build in:

- limit(count): limits the number of elements in the stream
- dropLast(): drops the last item of the stream
- map(f): map all the elements in a stream
- filter(f): filter the elements using a predicate
- find(f) find an element using a predicate
- zip(stream) create 1 stream from 2 
- stream(): create a Java Stream
- sorted(c): sort a stream
- reversed(): reverse all the elements in a stream
- plusAll(iter): add all elements from an Iterable to this stream
- plus(value): add a single value
- contains(value): check if this stream contains a specific value
- containsAll(iter): check if this stream contains all the items in the Iterable
- groupBy(f): groups element in a map of lists
- fold(init,f): combine elements to create 1 resulting element
- with(init,f): returns the init value transformed using all the elements in the stream
- head(): get the fist element of the stream
- tail(): drop the first element of the stream
- max(c): calculate the maximum value of all the elements
- min(c): calculate the minimum value of all the elements
- toArray(): copy the elements of this stream in a new array
- plist(): convert this stream to a peristent list(PList)
- llist(): convert this stream to a persistent Linked list (LList)
- pset(): convert this stream to a persistent Set (PSet)
- distinct(): removes all duplicated elements
- list(): returns an immutable  Java List
- toList(): returns a new ArrayList 
- join(f): join all elements to 1 element
- flatten(): flatten the stream


## How to get it.

### Using maven:
    Add the following dependency:
```xml
        <dependency>
            <groupId>com.persistentbit</groupId>
            <artifactId>persistent-core</artifactId>
            <version>1.0.3</version>
        </dependency>
```