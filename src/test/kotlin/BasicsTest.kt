import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.failureList
import com.github.kjetilv.whitebear.validate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal val errorStrings = failureList<String>()

class BasicsTest {

    private fun invalidMap(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") map { it + "str" } validateThat { false } elseInvalid { error }
    }

    private fun validMap(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") map { it + "str" } validateThat { true } elseInvalid { error }
    }

    private fun invalidFlat(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") flatMap { valid(it + "str") } validateThat { false } elseInvalid { error }
    }

    private fun validFlat(error: String): Validated<String, List<String>> = validate(errorStrings) {
        valid("str") flatMap { valid(it + "str") } validateThat { true } elseInvalid { error }
    }

    @Test
    fun map() {
        assertThat(invalidMap("Oops").valid).isFalse
        assertThat(invalidMap("Oops").error).singleElement().satisfies(condition { assertThat(it).isEqualTo("Oops") })
    }

    @Test
    fun flatMap() {
        assertThat(invalidFlat("Oops flat").valid).isFalse
        assertThat(invalidFlat("Oops flat").error).singleElement().satisfies(condition { assertThat(it).isEqualTo("Oops flat") })
    }

    @Test
    fun collectErrorsInvalis() {
        val errors: Validated<Any, List<String>> = validate(errorStrings) {
            collect(invalidMap("0"), invalidFlat("1"))
        }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(2)
            .anyMatch { it == "0" }
            .anyMatch { it == "1" }
    }

    @Test
    fun collectErrorsInvalidsAndValids0() {
        val errors = validate(errorStrings) {
            collect(validMap("0"), invalidMap("1"), invalidFlat("2"))
        }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(2)
            .anyMatch { it == "1" }
            .anyMatch { it == "2" }
    }

    @Test
    fun collectErrorsInvalidsAndValids1() {
        val errors = validate(errorStrings) {
            collect(invalidMap("Oops"), validMap("Oops"), invalidFlat("Oops flat"))
        }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(2).anyMatch { "Oops" == it }.anyMatch { "Oops flat" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids2() {
        val errors = validate(errorStrings) {
            collect(invalidMap("Oops"), invalidFlat("Oops flat"), validMap("Oops"))
        }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(2).anyMatch { "Oops" == it }.anyMatch { "Oops flat" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids01() {
        val errors = validate(errorStrings) {
            collect(validMap("Oops"), validMap("Oops"), invalidFlat("Oops flat"))
        }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(1).anyMatch { "Oops flat" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids12() {
        val errors = validate(errorStrings) {
            collect(invalidMap("Oops"), validMap("Oops"), validMap("Oops"))
        }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(1).anyMatch { "Oops" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids02() {
        val errors = validate(errorStrings) {
            collect(validMap("Oops"), invalidFlat("Oops flat"), validMap("Oops"))
        }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(1).anyMatch { "Oops flat" == it }
    }

    @Test
    fun zipValids() {
        val validated = validMap("Oops") zipWith validFlat("Oops flat") map { s1, s2 -> s1 + s2 }

        assertThat(validated.valid).isTrue
        assertThat(validated.value).isEqualTo("strstr" + "strstr")
    }

    @Test
    fun zipZipValids() {
        val validated = validMap("Oops") zipWith validMap("Oops") zipWith validFlat("Oops flat") map { s1, s2, s3 -> s1 + s2 + s3 }

        assertThat(validated.valid).isTrue
        assertThat(validated.value).isEqualTo("strstr" + "strstr" + "strstr")
    }

    @Test
    fun zipInvalids() {
        val errors = validMap("Oops") zipWith invalidFlat("Oops flat") map { s1, s2 -> s1 + s2 }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(1).anyMatch { "Oops flat" == it }
    }

    @Test
    fun zipBothInvalids() {
        val errors = invalidMap("Oops") zipWith invalidFlat("Oops flat") map { s1, s2 -> s1 + s2 }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(2)
            .anyMatch { "Oops flat" == it }
            .anyMatch { "Oops" == it }
    }

    @Test
    fun zipZipInvalids02() {
        val errors = invalidFlat("Oops flat") zipWith validFlat("Oops flat") zipWith invalidMap("Oops") map { s1, s2, s3 -> s1 + s2 + s3 }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(2).anyMatch { "Oops flat" == it }.anyMatch { "Oops" == it }
    }

    @Test
    fun zipZipInvalids01() {
        val errors =
            (invalidFlat("Oops flat") zipWith invalidMap("Oops") zipWith validFlat("Oops flat")) map { s1, s2, s3 ->
                s1 + s2 + s3
            }

        assertThat(errors.valid).isFalse

        assertThat(errors.error).hasSize(2).anyMatch { "Oops flat" == it }.anyMatch { "Oops" == it }
    }

    @Test
    fun zipZipInvalids12() {
        val errors = validMap("Oops") zipWith invalidFlat("Oops flat") zipWith invalidMap("Oops") map { s1, s2, s3 -> s1 + s2 + s3 }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(2).anyMatch { "Oops flat" == it }.anyMatch { "Oops" == it }
    }

    @Test
    fun zipZipInvalids012() {
        val zipper1 = invalidMap("0") zipWith invalidFlat("1")
        val zipper2 = zipper1 zipWith invalidMap("2")
        val errors =
            zipper2 map { s1, s2, s3 ->
                s1 + s2 + s3
            }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(3)
            .anyMatch { it == "0" }
            .anyMatch { it == "1" }
            .anyMatch { it == "2" }
    }

    @Test
    fun zipInvalidsFlip() {
        val errors = invalidMap("Oops") zipWith validFlat("Oops flat") map { s1, s2 -> s1 + s2 }

        assertThat(errors.valid).isFalse
        assertThat(errors.error).hasSize(1).anyMatch { "Oops" == it }
    }
}
