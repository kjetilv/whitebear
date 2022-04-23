@file:Suppress("SameParameterValue")

package com.github.kjetilv.whitebear.test

import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.errorList
import com.github.kjetilv.whitebear.simpleErrors
import com.github.kjetilv.whitebear.validate
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

internal val errorStrings = errorList<String>()

class BasicsTest {

    private fun invalidMap(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") map {
            it + "str"
        } validIf {
            false
        } orInvalidate {
            error
        }
    }

    private fun validMap(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") map { it + "str" } validIf { true } orInvalidate { error }
    }

    private fun invalidFlat(error: String): Validated<String, List<String>> =
        validate(errorStrings) { ->
            valid("str") flatMap {
                valid(it + "str")
            } validIf {
                false
            } orInvalidate {
                error
            }
        }

    private fun validFlat(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") flatMap { valid(it + "str") } validIf { true } orInvalidate { error }
    }

    @Test
    fun map() {
        assertFalse { invalidMap("Oops, it was valid!").valid }
        assertContains(invalidMap("Oops").error, "Oops")
    }

    @Test
    fun flatMap() {
        assertFalse { invalidFlat("Oops flat").valid }
        assertContains(invalidFlat("Oops flat").error, "Oops flat")
    }

    @Test
    fun collectErrorsInvalis() {
        val errors: Validated<Any, List<String>> = validate(errorStrings) {
            collect(invalidMap("0"), invalidFlat("1"))
        }

        assertFalse { errors.valid }
        assertEquals(2, errors.error.size)
        assertContains(errors.error, "0")
        assertContains(errors.error, "1")
    }

    @Test
    fun collectErrorsInvalidsAndValids0() {
        val errors = validate(errorStrings) {
            collect(validMap("0"), invalidMap("1"), invalidFlat("2"))
        }

        assertFalse { errors.valid }
        assertEquals(2, errors.error.size)
        assertContains(errors.error, "1")
        assertContains(errors.error, "2")
    }

    @Test
    fun collectErrorsInvalidsAndValids1() {
        val errors = validate(errorStrings) {
            collect(invalidMap("Oops"), validMap("Oops"), invalidFlat("Oops flat"))
        }

        assertFalse { errors.valid }
        assertEquals(2, errors.error.size)
        assertContains(errors.error, "Oops")
        assertContains(errors.error, "Oops flat")
    }

    @Test
    fun collectErrorsInvalidsAndValids2() {
        val errors = validate(errorStrings) {
            collect(invalidMap("Oops"), invalidFlat("Oops flat"), validMap("Oops"))
        }

        assertFalse { errors.valid }
        assertEquals(2, errors.error.size)
        assertContains(errors.error, "Oops")
        assertContains(errors.error, "Oops flat")
    }

    @Test
    fun collectErrorsInvalidsAndValids01() {
        val errors = validate(errorStrings) {
            collect(validMap("Oops"), validMap("Oops"), invalidFlat("Oops flat"))
        }

        assertFalse { errors.valid }
        assertEquals(1, errors.error.size)
        assertContains(errors.error, "Oops flat")
    }

    @Test
    fun collectErrorsInvalidsAndValids12() {
        val errors = validate(errorStrings) {
            collect(invalidMap("Oops"), validMap("Oops"), validMap("Oops"))
        }

        assertFalse { errors.valid }
        assertEquals(1, errors.error.size)
        assertContains(errors.error, "Oops")
    }

    @Test
    fun collectErrorsInvalidsAndValids02() {
        val errors = validate(errorStrings) {
            collect(validMap("Oops"), invalidFlat("Oops flat"), validMap("Oops"))
        }

        assertFalse { errors.valid }
        assertEquals(1, errors.error.size)
        assertContains(errors.error, "Oops flat")
    }

    @Test
    fun zipValids() {
        val validated = validMap("Oops") zipWith validFlat("Oops flat") map { s1, s2 -> s1 + s2 }

        assertTrue { validated.valid }
        assertEquals("strstr" + "strstr", validated.value)
    }

    @Test
    fun zipZipValids() {
        val validated =
            validMap("Oops") zipWith validMap("Oops") zipWith validFlat("Oops flat") map { s1, s2, s3 -> s1 + s2 + s3 }

        assertTrue { validated.valid }
        assertEquals("strstr" + "strstr" + "strstr", validated.value)
    }

    @Test
    fun zipInvalids() {
        val errors = validMap("Oops") zipWith invalidFlat("Oops flat") map { s1, s2 -> s1 + s2 }

        assertFalse { errors.valid }
        assertEquals(errors.error.size, 1)
        assertContains(errors.error, "Oops flat")
    }

    @Test
    fun zipBothInvalids() {
        val errors = invalidMap("Oops") zipWith invalidFlat("Oops flat") map { s1, s2 -> s1 + s2 }

        assertFalse { errors.valid }
        assertEquals(errors.error.size, 2)
        assertContains(errors.error, "Oops flat")
        assertContains(errors.error, "Oops")
    }

    @Test
    fun zipZipInvalids02() {
        val errors =
            invalidFlat("Oops flat") zipWith validFlat("Oops flat") zipWith invalidMap("Oops") map { s1, s2, s3 -> s1 + s2 + s3 }

        assertFalse { errors.valid }
        assertEquals(errors.error.size, 2)
        assertContains(errors.error, "Oops flat")
        assertContains(errors.error, "Oops")
    }

    @Test
    fun zipZipInvalids01() {
        val errors =
            (invalidFlat("Oops flat") zipWith invalidMap("Oops") zipWith validFlat("Oops flat")) map { s1, s2, s3 ->
                s1 + s2 + s3
            }

        assertFalse { errors.valid }
        assertEquals(errors.error.size, 2)
        assertContains(errors.error, "Oops flat")
        assertContains(errors.error, "Oops")
    }

    @Test
    fun zipZipInvalids12() {
        val errors =
            validMap("Oops") zipWith invalidFlat("Oops flat") zipWith invalidMap("Oops") map { s1, s2, s3 -> s1 + s2 + s3 }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(2).anyMatch { "Oops flat" == it }.anyMatch { "Oops" == it }
    }

    @Test
    fun zipZipInvalids012() {
        val zipper1 = invalidMap("0") zipWith invalidFlat("1")
        val zipper2 = zipper1 zipWith invalidMap("2")
        val errors = zipper2 map { s1, s2, s3 ->
            s1 + s2 + s3
        }
        assertFalse { errors.valid }
        assertEquals(errors.error.size, 3)
        assertContains(errors.error, "0")
        assertContains(errors.error, "1")
        assertContains(errors.error, "2")

    }

    @Test
    fun zipInvalidsFlip() {
        val errors = invalidMap("Oops") zipWith validFlat("Oops flat") map { s1, s2 -> s1 + s2 }

        assertFalse { errors.valid }
        assertEquals(errors.error.size, 1)
        assertContains(errors.error, "Oops")
    }

    @Test
    fun twoyFour() {
        val validThree = validate(simpleErrors("") { s1, s2 ->
            s1 + s2
        }) {
            valid("foo") zipWith {
                valid(16)
            }
        } map ::StringAndInt
        assertTrue { validThree.valid }
    }

    @Test
    fun twoByFourAgain() {
        val validFour = validate(simpleErrors("") { s1, s2 ->
            s1 + s2
        }) {
            valid("foo") zipWith valid(16)
        } map ::StringAndInt
        assertTrue { validFour.valid }
    }

    @Test
    fun threeByFour() {
        val validThree = validate(simpleErrors("") { s1, s2 ->
            s1 + s2
        }) {
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
        val validFour = validate(simpleErrors("") { s1, s2 ->
            s1 + s2
        }) {
            valid("foo").zipWith(
                valid(16),
                valid(true),
            )
        } map ::StringIntAndBool
        assertTrue { validFour.valid }
    }

    @Test
    fun fourbyFour() {
        val errors = simpleErrors(empty = "") { s1, s2 ->
            s1 + s2
        }
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
        val errors = simpleErrors(empty = "") { s1, s2 ->
            s1 + s2
        }
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
        val errors = simpleErrors("") { s1, s2 ->
            s1 + s2
        }
        val validFour = validate(errors) {
            valid("foo").zipWith(valid(16), valid(true), valid(System.currentTimeMillis()))
        } map ::StringIntBoolAndLong
        assertTrue { validFour.valid }
    }

    @Test
    fun fiveByFive() {
        val validFour = validate(simpleErrors("") { s1, s2 ->
            s1 + s2
        }) {
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
        val validFour = validate(simpleErrors("") { s1, s2 ->
            s1 + s2
        }) {
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
        val validFour = validate(simpleErrors("") { s1, s2 ->
            s1 + s2
        }) {
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
        val validFour = validate(simpleErrors("") { s1, s2 ->
            s1 + s2
        }) {
            valid("foo").zipWith(valid(16),
                valid(true),
                valid(System.currentTimeMillis()),
                valid('c')) map ::LotsOfStuff
        }

        assertTrue { validFour.valid }
    }
}
