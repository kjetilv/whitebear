# White Bear

We are tired of validating outside state. Bad user inputs are the norm.  and bad
data, especially testdata data, must be assumed. 

However, we must. But we don't have to add to the pain by validating with exceptions.

How many times have you written an exception handler that needs to figure out which
exceptions are actual errors, and which are 'agreed-upon' exceptions from 
some internal logic down in your tech stack detecting an illegal argument?

It doesn't have to be this way, and there are many  ways to go about it. First and
foremost, we shoud realize that bad input is not exceptional. Hence, we don't 
want to model it as an exception. Here's one way:

## Basics

Here is a valid piece of data:

```kotlin
val validAge: Validated<Int, String> = valid(16)
```

To enforce some age limit, you can go:

```kotlin
val beerAge = validAge validateThat { it >= 18 } elseInvalid { "$it is too young"}
```
Another way to get the same result:
```
val invalidAge: Validate<Int, String> = invalid("16 is too young")
```
Then map it to a legal birth year:
```kotlin
val birthYear = beerAge map { currentYear() - it } 
```
At this point, it can be unpacked:
```kotlin
val okYear = birthYear validValueOr { msgs ->
    throw new MyBadInputError(msgs.joinToString(", "))
}
```

## Something real-like

Assuming some data class:
```kotlin
data class Person(val name: String, val age: Int)
```

Validate with a monadic (alright, monad-ish) flow:

```kotlin
fun person(name: String, age: Int): Validated<Person, String> =
    validateThat(name) {
        it.isRegistered
    } elseInvalid {
        "Invalid name: $it"
    } zipWith validateThat(age) {
        it.isAllowed
    } elseInvalid {
        BadAge("Invalid age: $it")
    } map ::Person annotateInvalid {
        "Failed to validate person data"
    }
```
This produces zero, two or three errors. Assuming you have a:
```kotlin
interface PersonDao {

    fun <T> store(validated: Validate<T, String>): Validated<Stored<T>, String>
}
```
You can go:
```
val stored = person flatMap personDao::store 
```
This way, `personDao` only gets hit with valid data, and it can returns its 
own invalid state.

At the end:
```kotlin
return result validValueOr { messages -> 
    throw IllegalArgumentException("Invalid data: ${messages.joinToString(", ")}")    
}
```
This is a work in progress and is currently in the ideas-gathering/gelling stage.
