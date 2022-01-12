import com.github.kjetilv.whitebear.Invalid
import com.github.kjetilv.whitebear.Valid
import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.validated
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

abstract class ValidationExampleTestCase(val create: (String, Int, String?) -> Validated<Person, PersonIssue>) {

    @Test
    fun `good person`() {
        val goodPerson = create(name, age, license)
        assertThat(goodPerson.valid).isTrue
    }

    @Test
    fun `bad name`() {
        val badNamePerson = create(nameX, age, license)
        assertFalse(badNamePerson.valid)
        assertThat(badNamePerson.errors).hasSize(2)
            .anyMatch { it is BadName }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad name, age`() {
        val badNameAgePerson = create(nameX, ageX, license)
        assertFalse(badNameAgePerson.valid)
        assertThat(badNameAgePerson.errors).hasSize(3)
            .anyMatch { it is BadName }
            .anyMatch { it is BadAge }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad name, license`() {
        val badNameLicensePerson = create(nameX, age, licenseX)
        assertFalse(badNameLicensePerson.valid)
        assertThat(badNameLicensePerson.errors).hasSize(3)
            .anyMatch { it is BadName }
            .anyMatch { it is BadLicense }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad age`() {
        val badAgePerson = create(name, ageX, license)
        assertFalse(badAgePerson.valid)
        assertThat(badAgePerson.errors).hasSize(2)
            .anyMatch { it is BadAge }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad age, license`() {
        val badAgeLicensePerson = create(name, ageX, licenseX)
        assertFalse(badAgeLicensePerson.valid)
        assertThat(badAgeLicensePerson.errors).hasSize(3)
            .anyMatch { it is BadAge }
            .anyMatch { it is BadLicense }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad license`() {
        val badLicensePerson = create(name, age, licenseX)
        assertFalse(badLicensePerson.valid)
        assertThat(badLicensePerson.errors).hasSize(2)
            .anyMatch { it is BadLicense }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad state bad`() {
        val badLicensePerson = create(name, 16, license)
        assertFalse(badLicensePerson.valid)
        assertThat(badLicensePerson.errors).hasSize(2)
            .anyMatch { it is BadCombo }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `all bad`() {
        val badPerson = create(nameX, ageX, licenseX)
        assertFalse(badPerson.valid)
        assertThat(badPerson.errors).hasSize(4)
            .anyMatch { it is BadName }
            .anyMatch { it is BadAge }
            .anyMatch { it is BadLicense }
            .anyMatch { it is TheBadness }
    }

}

class RestTest {

    @Test
    fun `so it goes`() {
        println(RestResrouce().hello("biz", "zib"))

//        RestResrouce().hello("foo", "bar")
//        RestResrouce().hello("biz", "zibb")
    }
}

class Person0Test : ValidationExampleTestCase(
    { n, a, p -> person0(n, a, p) }
)

class Person1Test : ValidationExampleTestCase(
    { n, a, p -> person1(n, a, p) }
)

class Person1aTest : ValidationExampleTestCase(
    { n, a, p -> person1a(n, a, p) }
)

class Person2Test : ValidationExampleTestCase(
    { n, a, p -> person2(n, a, p) }
)

class Person3Test : ValidationExampleTestCase(
    { n, a, p -> person3(n, a, p) }
)

class Person4Test : ValidationExampleTestCase(
    { n, a, p -> person4(n, a, p) }
)

class PersonXTest : ValidationExampleTestCase(
    { n, a, p -> personX(n, a, p) }
)

class RestResrouce {

    private val serviceLayer = ServiceLayer()

    fun hello(world: String, greeting: String): String {
        return serviceLayer.greet(world, greeting) annotateInvalid {
            "REST layer failed: $world/$greeting "
        } validValueOr { errors ->
            throw IllegalStateException("Failed to greet: ${errors.joinToString(" => ") { it.trim() }}")
        }
    }
}

class ServiceLayer {

    fun greet(world: String, greeting: String) =
        getWorld(world)
            .flatMap { it.accept(greeting) }
            .validateThat { it != "ouch" }
            .elseInvalid { "Not a good response: $it" }
            .annotateInvalid { "World named $world and greeting $greeting was a no-go" }

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

class Bizarro(private val x: Int) : Wrodl() {
    override fun accept(greeting: String): Validated<String, String> =
        validated {
            when (greeting) {
                "zib" -> valid("ok: $x")
                "zibb" -> valid("ouch: $x")
                else -> invalid(
                    "$this $x does not respond to $greeting"
                )
            }
        }
}

class Collider(private val y: Int) : Wrodl() {
    override fun accept(greeting: String): Validated<String, String> =
        validated {
            when (greeting) {
                "col" -> valid("ok: $y")
                "coll" -> valid("ouch: $y")
                else -> invalid("$this $y does not respond to $greeting")
            }
        }
}
