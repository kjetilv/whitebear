import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.collectToT
import com.github.kjetilv.whitebear.validated
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BasicsTest {

    private fun invalidMap(): Validated<String, String> = validated {
        valid("str") map { it + "str" } validateThat { false } elseInvalid { "Oops" }
    }

    private fun validMap(): Validated<String, String> = validated {
        valid("str") map { it + "str" } validateThat { true } elseInvalid { "Oops" }
    }

    private fun invalidFlat(): Validated<String, String> = validated {
        valid("str") flatMap { valid(it + "str") } validateThat { false } elseInvalid { "Oops flat" }
    }

    private fun validFlat(): Validated<String, String> = validated {
        valid("str") flatMap { valid(it + "str") } validateThat { true } elseInvalid { "Oops flat" }
    }

    @Test
    fun map() {
        assertThat(invalidMap().valid).isFalse
        assertThat(invalidMap().errors).singleElement().satisfies(condition { assertThat(it).isEqualTo("Oops") })
    }

    @Test
    fun flatMap() {
        assertThat(invalidFlat().valid).isFalse
        assertThat(invalidFlat().errors).singleElement().satisfies(condition { assertThat(it).isEqualTo("Oops flat") })
    }

    @Test
    fun collectErrorsInvalis() {
        val errors = collectToT<String, String>(invalidMap(), invalidFlat())

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(2).anyMatch { "Oops" == it }.anyMatch { "Oops flat" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids0() {
        val errors = collectToT<String, String>(validMap(), invalidMap(), invalidFlat())

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(2).anyMatch { "Oops" == it }.anyMatch { "Oops flat" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids1() {
        val errors = collectToT<String, String>(invalidMap(), validMap(), invalidFlat())

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(2).anyMatch { "Oops" == it }.anyMatch { "Oops flat" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids2() {
        val errors = collectToT<String, String>(invalidMap(), invalidFlat(), validMap())

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(2).anyMatch { "Oops" == it }.anyMatch { "Oops flat" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids01() {
        val errors = collectToT<String, String>(validMap(), validMap(), invalidFlat())

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(1).anyMatch { "Oops flat" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids12() {
        val errors = collectToT<String, String>(invalidMap(), validMap(), validMap())

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(1).anyMatch { "Oops" == it }
    }

    @Test
    fun collectErrorsInvalidsAndValids02() {
        val errors = collectToT<String, String>(validMap(), invalidFlat(), validMap())

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(1).anyMatch { "Oops flat" == it }
    }

    @Test
    fun zipValids() {
        val validated = validMap() zipWith validFlat() map { s1, s2 -> s1 + s2 }

        assertThat(validated.valid).isTrue
        assertThat(validated.value).isEqualTo("strstr" + "strstr")
    }

    @Test
    fun zipZipValids() {
        val validated = validMap() zipWith validMap() zipWith validFlat() map { s1, s2, s3 -> s1 + s2 + s3 }

        assertThat(validated.valid).isTrue
        assertThat(validated.value).isEqualTo("strstr" + "strstr" + "strstr")
    }

    @Test
    fun zipInvalids() {
        val errors = validMap() zipWith invalidFlat() map { s1, s2 -> s1 + s2 }

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(1).anyMatch { "Oops flat" == it }
    }

    @Test
    fun zipZipInvalids02() {
        val errors = invalidFlat() zipWith validFlat() zipWith invalidMap() map { s1, s2, s3 -> s1 + s2 + s3 }

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(2).anyMatch { "Oops flat" == it }.anyMatch { "Oops" == it }
    }

    @Test
    fun zipZipInvalids01() {
        val errors = invalidFlat() zipWith invalidMap() zipWith validFlat() map { s1, s2, s3 -> s1 + s2 + s3 }

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(2).anyMatch { "Oops flat" == it }.anyMatch { "Oops" == it }
    }

    @Test
    fun zipZipInvalids12() {
        val errors = validMap() zipWith invalidFlat() zipWith invalidMap() map { s1, s2, s3 -> s1 + s2 + s3 }

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(2).anyMatch { "Oops flat" == it }.anyMatch { "Oops" == it }
    }

    @Test
    fun zipInvalidsFlip() {
        val errors = invalidMap() zipWith validFlat() map { s1, s2 -> s1 + s2 }

        assertThat(errors.valid).isFalse
        assertThat(errors.errors).hasSize(1).anyMatch { "Oops" == it }
    }
}
