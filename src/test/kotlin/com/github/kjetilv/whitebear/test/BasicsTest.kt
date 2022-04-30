@file:Suppress("SameParameterValue")

package com.github.kjetilv.whitebear.test

import com.github.kjetilv.whitebear.Invalid
import com.github.kjetilv.whitebear.Valid
import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.errorList
import com.github.kjetilv.whitebear.errors
import com.github.kjetilv.whitebear.validate
import org.assertj.core.api.Assertions.ARRAY_2D
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testdata.LotsOfStuff
import testdata.StringAndInt
import testdata.StringIntAndBool
import testdata.StringIntBoolAndLong
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

val errorStrings = errorList<String>()

class BasicsTest {

    private fun invalidMap(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") map {
            it + "str"
        } validIf {
            false
        } orInvalidate {
            listOf(error)
        }
    }

    private fun validMap(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") map { it + "str" } validIf { true } orInvalidate { listOf(error) }
    }

    private fun invalidFlat(error: String): Validated<String, List<String>> =
        validate(errorStrings) { ->
            valid("str") flatMap {
                valid(it + "str")
            } validIf {
                false
            } orInvalidate {
                listOf(error)
            }
        }

    private fun validFlat(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") flatMap { valid(it + "str") } validIf { true } orInvalidate { listOf(error) }
    }

    @Test
    fun map() {
        val invalidMap = invalidMap("Oops")
        when (invalidMap) {
            is Invalid<String, List<String>> ->
                assertContains(invalidMap.error, "Oops")
            else ->
                fail(invalidMap.toString())
        }
    }

    @Test
    fun flatMap() {
        assertFalse { invalidFlat("Oops flat").valid }
        when (val it = invalidFlat("Oops flat")) {
            is Invalid<*, List<String>> -> {
                assertContains(it.error, "Oops flat")
            }
            else ->
                fail("$it")
        }
    }

    @Test
    fun collectErrorsInvalis() {
        val errors: Validated<Any, List<String>> = validate(errorStrings) {
            collect(invalidMap("0"), invalidFlat("1"))
        }
        when (errors) {
            is Invalid<Any, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(2, errors.error.size)
                assertContains(errors.error, "0")
                assertContains(errors.error, "1")
            }
            else -> fail("$errors")
        }
    }

    @Test
    fun collectErrorsInvalidsAndValids0() {
        val errors = validate(errorStrings) {
            collect(validMap("0"), invalidMap("1"), invalidFlat("2"))
        }

        when (errors) {
            is Invalid<Any, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(2, errors.error.size)
                assertContains(errors.error,
                    "1")
                assertContains(errors.error, "2")
            }
            else ->
                fail(errors.toString())
        }
    }

    @Test
    fun collectErrorsInvalidsAndValids1() {
        val errors = validate(errorStrings) {
            collect(invalidMap("Oops"), validMap("Oops"), invalidFlat("Oops flat"))
        }

        when (errors) {
            is Invalid<Any, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(2, errors.error.size)
                assertContains(errors.error, "Oops")
                assertContains(errors.error, "Oops flat")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun collectErrorsInvalidsAndValids2() {
        val errors = validate(errorStrings) {
            collect(invalidMap("Oops"), invalidFlat("Oops flat"), validMap("Oops"))
        }

        when (errors) {
            is Invalid<Any, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(2, errors.error.size)
                assertContains(errors.error, "Oops")
                assertContains(errors.error, "Oops flat")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun collectErrorsInvalidsAndValids01() {
        val errors = validate(errorStrings) {
            collect(validMap("Oops"), validMap("Oops"), invalidFlat("Oops flat"))
        }
        when (errors) {
            is Invalid<Any, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(1, errors.error.size)
                assertContains(errors.error, "Oops flat")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun collectErrorsInvalidsAndValids12() {
        val errors = validate(errorStrings) {
            collect(invalidMap("Oops"), validMap("Oops"), validMap("Oops"))
        }

        when (errors) {
            is Invalid<Any, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(1, errors.error.size)
                assertContains(errors.error, "Oops")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun collectErrorsInvalidsAndValids02() {
        val errors = validate(errorStrings) {
            collect(validMap("Oops"), invalidFlat("Oops flat"), validMap("Oops"))
        }

        when (errors) {
            is Invalid<Any, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(1, errors.error.size)
                assertContains(errors.error, "Oops flat")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun zipValids() {
        val validated = validMap("Oops") zipWith validFlat("Oops flat") map { s1, s2 -> s1 + s2 }
        when (validated) {
            is Valid<String, List<String>> -> {
                assertTrue { validated.valid }
            }
            else ->
                fail("$validated")
        }
    }

    @Test
    fun zipZipValids() {
        val validated =
            validMap("Oops") zipWith validMap("Oops") zipWith validFlat("Oops flat") map { s1, s2, s3 -> s1 + s2 + s3 }

        when (validated) {
            is Valid<String, List<String>> -> {
                assertTrue { validated.valid }
            }
            else ->
                fail("$validated")
        }
    }

    @Test
    fun zipInvalids() {
        val errors = validMap("Oops") zipWith invalidFlat("Oops flat") map { s1, s2 -> s1 + s2 }

        when (errors) {
            is Invalid<String, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(errors.error.size, 1)
                assertContains(errors.error, "Oops flat")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun zipBothInvalids() {
        val errors =
            invalidMap("Oops") zipWith invalidFlat("Oops flat") map { s1, s2 ->
                listOf(s1, s2)
            }

        when (errors) {
            is Invalid<*, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(errors.error.size, 2)
                assertContains(errors.error, "Oops flat")
                assertContains(errors.error, "Oops")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun zipZipInvalids02() {
        val errors =
            invalidFlat("Oops flat") zipWith validFlat("Oops flat") zipWith invalidMap("Oops") map { s1, s2, s3 -> s1 + s2 + s3 }
        when (errors) {
            is Invalid<*, List<String>> -> {

                assertFalse { errors.valid }
                assertEquals(errors.error.size, 2)
                assertContains(errors.error, "Oops flat")
                assertContains(errors.error, "Oops")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun zipZipInvalids01() {
        val errors =
            (invalidFlat("Oops flat") zipWith invalidMap("Oops") zipWith validFlat("Oops flat")) map { s1, s2, s3 ->
                s1 + s2 + s3
            }
        when (errors) {
            is Invalid<*, List<String>> -> {

                assertFalse { errors.valid }
                assertEquals(errors.error.size, 2)
                assertContains(errors.error, "Oops flat")
                assertContains(errors.error, "Oops")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun zipZipInvalids12() {
        val errors =
            validMap("Oops") zipWith invalidFlat("Oops flat") zipWith invalidMap("Oops") map { s1, s2, s3 -> s1 + s2 + s3 }

        when (errors) {
            is Invalid<*, List<String>> -> {
                assertThat(errors.valid).isFalse
                assertThat(errors.error).hasSize(2).anyMatch { "Oops flat" == it }.anyMatch { "Oops" == it }
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun zipZipInvalids012() {
        val zipper1 = invalidMap("0") zipWith invalidFlat("1")
        val zipper2 = zipper1 zipWith invalidMap("2")
        val errors = zipper2 map { s1, s2, s3 ->
            s1 + s2 + s3
        }
        when (errors) {
            is Invalid<*, List<String>> -> {
                assertFalse { errors.valid }
                assertEquals(errors.error.size, 3)
                assertContains(errors.error, "0")
                assertContains(errors.error, "1")
                assertContains(errors.error, "2")
            }
            else ->
                fail("$errors")
        }

    }

    @Test
    fun zipInvalidsFlip() {
        val errors = invalidMap("Oops") zipWith validFlat("Oops flat") map { s1, s2 -> s1 + s2 }
        when (errors) {
            is Invalid<*, List<String>> -> {

                assertFalse { errors.valid }
                assertEquals(errors.error.size, 1)
                assertContains(errors.error, "Oops")
            }
            else ->
                fail("$errors")
        }
    }

    @Test
    fun twoyFour() {
        val validThree = validate(errorStrings) {
            valid("foo") zipWith {
                valid(16)
            }
        } map ::StringAndInt
        assertTrue { validThree.valid }
    }

    @Test
    fun twoByFourAgain() {
        val validFour = validate(errorStrings) {
            valid("foo") zipWith valid(16)
        } map ::StringAndInt
        assertTrue { validFour.valid }
    }

    @Test
    fun threeByFour() {
        val validThree = validate(errorStrings) {
            valid("foo") zipWith {
                valid(16)
            } zipWith {
                valid(true)
            }
        } map ::StringIntAndBool
        assertTrue { validThree.valid }
    }

    @Test
    fun threeByFourAgain() {
        val validFour = validate(errorStrings) {
            valid("foo").zipWith(
                valid(16),
                valid(true),
            )
        } map ::StringIntAndBool
        assertTrue { validFour.valid }
    }

    @Test
    fun fourbyFour() {
        val errors = errorStrings
        val validFour = validate(errors) {
            valid("foo") zipWith {
                valid(16)
            } zipWith {
                valid(true)
            } zipWith {
                valid(System.currentTimeMillis())
            }
        } map ::StringIntBoolAndLong
        assertTrue { validFour.valid }
    }

    @Test
    fun fourbyFourA() {
        val errors = errorStrings
        val validFour = validate(errors) {
            valid("foo") zipWith {
                valid(16)
            } zipWith {
                valid(true)
            } zipWith (valid(System.currentTimeMillis()))
        } map ::StringIntBoolAndLong
        assertTrue { validFour.valid }
    }

    @Test
    fun fourbyFourAgain() {
        val validFour = validate(errorStrings) {
            valid("foo").zipWith(valid(16), valid(true), valid(System.currentTimeMillis()))
        } map ::StringIntBoolAndLong
        assertTrue { validFour.valid }
    }

    @Test
    fun fiveByFive() {
        val validFour = validate(errorStrings) {
            valid("foo") zipWith {
                valid(16)
            } zipWith {
                valid(true)
            } zipWith {
                valid(System.currentTimeMillis())
            } zipWith {
                valid('c')
            } map ::LotsOfStuff
        }

        assertTrue { validFour.valid }
    }

    @Test
    fun fiveByFiveA() {
        val validFour = validate(errorStrings) {
            valid("foo") zipWith {
                valid(16)
            } zipWith {
                valid(true)
            } zipWith {
                valid(System.currentTimeMillis())
            } zipWith {
                valid('c')
            } map ::LotsOfStuff
        }

        assertTrue { validFour.valid }
    }

    @Test
    fun fiveByFiveB() {
        val validFour = validate(errorStrings) {
            valid("foo") zipWith {
                valid(16)
            } zipWith {
                valid(true)
            } zipWith {
                valid(System.currentTimeMillis())
            } zipWith (valid('c')) map ::LotsOfStuff
        }

        assertTrue { validFour.valid }
    }

    @Test
    fun fiveByFiveAgin() {
        val validFour = validate(errorStrings) {
            valid("foo").zipWith(valid(16),
                valid(true),
                valid(System.currentTimeMillis()),
                valid('c')) map ::LotsOfStuff
        }

        assertTrue { validFour.valid }
    }

    private fun errorString() = errors("") { s1, s2 -> s1 + s2 }
}

