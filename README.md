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

# The good things

* You can control your object's memory layouts for native interop
* You are memory efficient, because you possibly get rid of a lot of Java objects
* The user can use struct objects as POJOs/POKOs
* You can copy parts of structs, structs or arrays of structs with a simple memcopy, no more object tree traversal
* You could use backing buffers as persistence layer and just write it to/read it from a file. Should be super fast.
* Simply add members to your struct to fulfill special layouts, like std430 from OpenGL etc.

# Restrictions - the bad things

* Inherently mutable. If you provide the backing buffer of a structure, it can be manipulated at will, no matter if your
  classes use val or var
* Possibly bypasses JIT optimizations. Compared to regular objects and usage of primitves/primitve arrays, the JIT compiler
  can't help you much when you use ByteBuffer instances. Although theoretically, a sliding window iteration over a fixed ByteBuffer
  instance should be extremely fast and super cache friendly, I found my approach to never be as fast as native JVM object usage. For     example [this article](https://dzone.com/articles/compact-heap-structurestuples) shows it can easily be way faster, but my experiments don't approve this. The project contains a benchmark project that can be run with JMH. Feel free to test on your machine. [This blogpost](http://hannosprogrammingblog.blogspot.com/2018/09/kind-of-structs-on-jvm-using-kotlins.html) contains some benchmark numbers, where I compared simple, non-abstracted approaches that operate directly on a ByteBuffer with succesively more abstracted approaches that peak in this libary's approach. For my use cases, iteration performance is not that crucial, because I benefit from being able to memcopy big object graphs with a triple buffer renderstate construct in my multithreaded game engine.
* Parent parameter passing. The current implementation needs parent parameters in order to allow nesting. This is damn ugly and forces a struct author to not forget about it, otherwise his structs can't be nested. Another implementation option here would be to make a struct's parent mutable. I have no idea about performance implications and problems with reassignments.
* Non-default values. In order to allow non-default values for struct properties, one has to assign the given value at property initialization, because there is simply no "hook" that could be used after the object is constructed. In order to avoid an "init" method, lazy buffer creation has to be dropped. This way, the allocated backing memory can be used when a property is initialized. See implementation details below.
* Very difficult to use inheritance and initialization logic, because it's easy to corrupt the usage of lazy properties for object's sizes and buffer creation
* Currently, delegated properties store delegate instances into your object. This could be eliminated with inline classes (in Kotlin 1.3), but my last try with a Kotlin preview build crashed with bytecode manipualtion errors.

# Implementation details
## Delegated properties
Delegated properties are the heart of this implementation. Kotlin allows you to hide your backing data structure from the user of your classe's instances. [Here's an example](https://kotlinlang.org/docs/reference/delegated-properties.html#storing-properties-in-a-map) for a map-backed class. The nice thing is, that your objects can be used as regular POJOs/POKOs. I use delegated properties of the form
```
class IntProperty(override var localByteOffset: Long):StructProperty {
    override val sizeInBytes = 4

    inline operator fun setValue(thisRef: Structable, property: KProperty<*>, value: Int) {
        putInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset, value)
    }
    inline operator fun getValue(thisRef: Structable, property: KProperty<*>) = StructProperty.Companion.getInt(thisRef.buffer, thisRef.baseByteOffset + localByteOffset)
}
```
to represent object properties. The _Structable_ interface contains some scoped extension functions like
```

operator fun Int.provideDelegate(thisRef: Structable, prop: KProperty<*>): IntProperty {
   return IntProperty(getCurrentLocalByteOffset())
      .apply { thisRef.register(this@apply) }
      .apply { if(this@provideDelegate != 0) this.setValue(thisRef, prop, this@provideDelegate) }
}
```

For convenience, there's an abstract base class, that provides necessary state that a struct always needs.
```
abstract class Struct(val parent: Structable? = null): Structable {
    override val memberStructs = mutableListOf<StructProperty>()
    override val sizeInBytes by lazy {
        memberStructs.sumBy { it.sizeInBytes }
    }
    var localByteOffset: Long = parent?.getCurrentLocalByteOffset() ?: 0

    final override val baseByteOffset: Long
        inline get() = localByteOffset + (parent?.baseByteOffset ?: 0)

    protected val ownBuffer by lazy { BufferUtils.createByteBuffer(sizeInBytes) }
    override val buffer by lazy { parent?.buffer ?: ownBuffer }
    fun usesOwnBuffer(): Boolean = ownBuffer === buffer
}
```
All member structs are registered and can be queried. That means the delegate instances can as well be used directly to do things. The sizeInBytes is determined by the sum of all nested struct properties...that means it can be calculated lazily. Downside: If you query this value somewhere in between object initialization, things can go wrong. This can be an issue when inheritance is used (which would work, but is difficult to use). The buffer can be created when all child structs are known, hence this can be calculated lazily as well here. If the struct is nested, the parent's buffer is used.

# Feedback

If you're interested in this project, if you do engine or graphics programming and want to talk about experience, please let me know :)

# Appendix

## Benchmarks
Some of the ebove mentioned benchmarks from a blog post of mine at http://hannosprogrammingblog.blogspot.com/2018/09/kind-of-structs-on-jvm-using-kotlins.html . Please understand that it's very complicated to write about what was tested, how the implementations differ from each other and so on. If you want to have more than a rough overview about what dimensions of performance differences we're talking here, take a look at the benchmarks module :)

Mode | Cnt   |   Score  |   Error | Units
| ------------ |:-----:| -------:|-----|----|
iterAndMutBufferDirect|                                          thrpt   12 | 90626,796| ± 303,407|  ops/s
iterAndMutKotlinDelegatedPropertySlidingWindowBuffer    |        thrpt   12 | 23695,594| ±  82,291|  ops/s
iterAndMutKotlinDelegatedPropertyUnsafeSimpleSlidingWindowBuffer|thrpt   12 | 27906,315| ±  52,382|  ops/s
iterAndMutKotlinDelegatedPropertyUnsafeSlidingWindowBuffer      |thrpt   12 | 25736,322| ± 904,017|  ops/s
iterAndMutKotlinSimpleSlidingWindowBuffer                        |thrpt   12 | 27416,212| ± 959,016|  ops/s
iterAndMutResizableStruct                                        |thrpt   12 | 10204,870| ± 189,237|  ops/s
iterAndMutSimpleSlidingWindowBuffer                              |thrpt   12 | 27627,217| ± 122,119|  ops/s
iterAndMutStructArray                                            |thrpt   12 | 12714,642| ±  51,275|  ops/s
iterAndMutStructArrayIndexed                                    |thrpt   12 | 11110,882| ±  26,910|  ops/s
iterAndMutVanilla                                                |thrpt   12 | 27111,335| ± 661,822|  ops/s
iterStruct                                                      |thrpt   12 | 13240,723| ±  40,612|  ops/s
iterVanilla                                                      |thrpt   12 | 21452,188| ±  46,380|  ops/s
