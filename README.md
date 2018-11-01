# kotlin-structs
Struct-like data structures on the JVM, powered by Kotlin

The JVM doesn't give us structs or value type support. This is especially painful, when dealing with game or graphics development,
because being garbage-free during runtime is most often a requirement in order to avoid gc hickups or stuttering.
Additionally, memory sharing between cpu and gpu is required - this raises demand for control over memory layouts. The Java object memory
layout isn't helpful at all here, so complex copy actions to ByteBuffers becomes necessary. Kotlin's delegated properties
offer a way to implement strict memory layout for objects that otherwise look like regular Java/Kotlin objects. Allocated
memory in form of ByteBuffer instances can be reused, copied and sent to the GPU without further necessities or complex object
graph traversals.

This library is an experiment. Please do not use it in production, because this approach - besides the ebove mentioned
advantages - has someserious downsides, when compared to regular JVM/Java/Kotlin programming. Further information, see below the examples.

# Usages

## Using a single simple class with a defined memory layout and a backing buffer, for example for native interop:

``` kotlin

// This class defines an instance to be backed by a ByteBuffer holding an int, a float and an int (that's a boolean)
/* resembles a C struct like
struct MyStruct {
   int   myInt;
   float myFloat;
   bool  myBoolean;
};
*/

// defines a class that doesn't allow nesting in any way and uses default values only
class MyStruct: Struct() {
    val myInt by 0
    var myMutableFloat by 0.0f
    var myMutableBoolean by false
}

val myStruct = MyStruct()
myStruct.myMutableFloat = 5.0 // this triggers the buffer to be initialized, because it's lazy
myStruct.myMutableBoolean = true
assertEquals(true, myStruct.myMutableBoolean)

val backingBuffer = myStruct.buffer
assertEquals(0, backingBuffer.getInt())
assertEquals(5.0f, backingBuffer.getFloat())
assertEquals(1, backingBuffer.getInt())
```
## Using nested custom structs

```
// defines a class that allows nesting by having an optional parent

/* resembles a C struct like
struct NestedStruct {
   int   myInt;
};
*/
class NestedStruct(parent: Structable? = null) : Struct(parent) {
    var myMutableInt by 4 // not the default int value, complicated, look at restrictions section below
}

/* resembles a C struct like
struct MyStruct {
   NestedStruct   nestedStruct;
   float          myFloat;
};
*/
class MyStruct: Struct() {
    // Creating the backing buffer eagerly, overriding the lazy behaviour, see restrictions section why
    override val buffer = BufferUtils.createByteBuffer(8)
    val nestedStruct by NestedStruct(this) // passing this activates nesting
    var myMutableFloat by 4.0f
}

val myStruct = MyStruct()
Assert.assertEquals(4, myStruct.nestedStruct.myMutableInt)
myStruct.nestedStruct.myMutableInt = 99
Assert.assertEquals(99, myStruct.nestedStruct.myMutableInt)
Assert.assertEquals(4.0f, myStruct.myMutableFloat)
```

## Using arrays
I implemented two kinds of arrays: buffer focused arrays and object focused arrays.

### Efficient buffer oriented arrays
The first kind is a wrapped backing buffer that holds memory for n instances of a struct class.
It can be used with a sliding-window that can point to arbitrary indices of the array.
This is very useful, if it's not necessary to use instance references on the cpu side.
This can be the case when one needs to only iterate with a single thread over the array
and use it mostly on the native or gpu side of things. It's perfectly possible to use
multiple sliding windows over a shared buffer, but I don't have use cases for this. This
approach is the most memory friendly one, because it only creates a compact block of memory
and a single instance of your class.

```
class MyStruct(parent: Structable? = null) : Struct(parent) {
    var myMutableInt by 0
}
// passes factory that is used to create the sliding window
val structArray = StaticStructArray(10) { MyStruct(it) }

structArray.forEachIndexed { index, current ->
    current.myMutableInt = index
}
// -> buffer containing 0,1,2,3,4,5,6,7,8,9
```
### Convenient object oriented arrays
This array implementation has a backing buffer, providing memory for n instances of a struct class,
as well as a backing array of objects. This requires a lot more space for the objects on the Java heap,
but provides all the convenience one got used to on the JVM, because the array can be used as a regular
JVM collection structure. For example, it could be used for multithreaded iteration without problems.

The reason why I implemented this approach: Sometimes your objects contain data, that mustn't be shared
with the native or GPU side of things. Those attributes are useful on the JVM side, but would waste memory
in the shared backing buffer.

```
class StructObject(parent: Struct?): Struct(parent) {
    var a by 0
    val aString = "aString" // won't be part of the backing buffer
}
val array = StaticStructObjectArray(size = 10, factory = { struct -> StructObject(struct) })
for(i in 0 until array.size) {
    array[i].a = i
}

Assert.assertEquals(10, array.size)
Assert.assertEquals(10, array.backingList.size)
for(i in 0 until array.size) {
    Assert.assertEquals(i, array[i].a)
    Assert.assertEquals("aString", array[i].aString)
}
```
