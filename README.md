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

## Usages

Using a single simple Class with a defined memory layout and a backing buffer:

``` kotlin

// This class defines an instance to be backed by a ByteBuffer holding an int, a float and an int (that's a boolean)
class MyStruct: Struct() { // defines a class that doesn't allow nesting in any way
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
