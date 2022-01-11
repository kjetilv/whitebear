import ValidationExampleTest.PersonalProblem.*
import im.tanin.valimon.*
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.function.Consumer

class ValidationExampleTest {

    data class Person(val name: String, val age: Int, val pronoun: String) {
        fun canExist() = name != "Kjetil" || age >= 40
    }

    sealed class PersonalProblem(val msg: String) {

        class TooOld(msg: String) : PersonalProblem(msg)

        class Lowercasedname(msg: String) : PersonalProblem(msg)

        class UnknownPronoum(msg: String) : PersonalProblem(msg)

        class InvalidState(msg: String) : PersonalProblem(msg)

        class Summary(msg: String) : PersonalProblem(msg)
    }

    @Test
    fun `not so modern`() {
        val name = "Kjetil"
        val nameX = "kjetil"

        val age = 49
        val ageX = 200

        val gender = "M"
        val genderX = "X"

        val person = person1(name, age, gender)

        assertThat(person).satisfies(condition { it.validated })

        assertFalse(person1(nameX, age, gender).validated)
        assertFalse(person1(name, ageX, gender).validated)
        assertFalse(person1(name, age, genderX).validated)

        assertFalse(person1(nameX, ageX, gender).validated)
        assertFalse(person1(nameX, age, genderX).validated)

        assertFalse(person1(name, ageX, genderX).validated)
        assertFalse(person1(nameX, ageX, gender).validated)

        assertFalse(person1(nameX, ageX, genderX).validated)

        assertFalse(person1(name, 30, gender).validated)
    }

    private fun <T> condition(f: Consumer<T>) = f

    private fun String.startsUppercased() = toCharArray().firstOrNull()?.isUpperCase()

    private fun String.isPronoun() = setOf("he", "she", "it", "they").contains(this)

    private fun Int.tooOld() = this > 120

    private fun person1(name: String, age: Int, pronoun: String): Validated<Person, PersonalProblem> {
        return valid<String, PersonalProblem>(name)
            .validateThat { it.startsUppercased() }
            .orFail { Lowercasedname("Name must start with uppercase: $it") }
            .zipWith {
                valid<Int, PersonalProblem>(age)
                    .validateThat { !it.tooOld() }
                    .orFail { TooOld("Invalid age: $it") }
            }.zipWith {
                valid<String, PersonalProblem>(pronoun)
                    .validateThat { it.isPronoun() }
                    .orFail { UnknownPronoum("Unknown pronoun: $it") }
            }
            .combineTo(::Person)
            .validateThat { it.canExist() }
            .orFail { InvalidState("Not a real person: $name") }
            .whenInvalid { Summary("$name: Person validation failed") }
    }

    @Test
    fun `so it goes`() {
        println(RestResrouce().hello("biz", "zib"))

//        RestResrouce().hello("foo", "bar")
//        RestResrouce().hello("biz", "zibb")
    }

    class RestResrouce {

        private val serviceLayer = ServiceLayer()

        fun hello(world: String, greeting: String): String {
            return serviceLayer
                .greet(world, greeting)
                .whenInvalid { "REST layer failed: $world/$greeting " }
                .getOrElse { es ->
                    throw IllegalStateException("Failed to greet: ${es.joinToString(" => ") { it.trim() }}")
                }
        }
    }

    class ServiceLayer {

        fun greet(world: String, greeting: String) =
            getWorld(world)
                .flatMap { it.accept(greeting) }
                .validateThat { it != "ouch" }
                .orFail { "Not a good response: $it" }
                .whenInvalid { "World named $world and greeting $greeting was a no-go" }

        private fun getWorld(world: String): Validated<Wrodl, String> =
            when (world) {
                "biz" -> Valid(Bizarro(1))
                "col" -> Valid(Collider(2))
                else -> Invalid("No world known as $world")
            }
    }

    sealed class Wrodl {

        abstract fun accept(greeting: String): Validated<String, String>
    }

    class Bizarro(x: Int) : Wrodl() {
        override fun accept(greeting: String): Validated<String, String> =
            when (greeting) {
                "zib" -> valid("ok")
                "zibb" -> valid("ouch")
                else -> invalid("$this does not respond to $greeting")
            }
    }

    class Collider(y: Int) : Wrodl() {
        override fun accept(greeting: String): Validated<String, String> =
            when (greeting) {
                "col" -> valid("ok")
                "coll" -> valid("ouch")
                else -> invalid("$this does not respond to $greeting")
            }
    }
}
