package com.github.kjetilv.whitebear.test

import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.errorList
import com.github.kjetilv.whitebear.validate
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import testdata.BadAge
import testdata.BadCombo
import testdata.BadLicense
import testdata.BadName
import testdata.Person
import testdata.PersonalIssues
import testdata.TheBadness
import testdata.age
import testdata.ageX
import testdata.license
import testdata.licenseX
import testdata.name
import testdata.nameX
import testdata.person0
import testdata.person1
import testdata.person1a
import testdata.person2
import testdata.person3
import testdata.person4
import testdata.personX
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class ValidationExampleTestCase(val create: (String, Int, String?) -> Validated<Person, PersonalIssues>) {

    @Test
    fun `good person`() {
        val goodPerson = create(name, age, license)
        assertTrue { goodPerson.valid }
    }

    @Test
    fun `bad name`() {
        val badNamePerson = create(nameX, age, license)

        assertFalse { badNamePerson.valid }
        assertEquals(2, badNamePerson.error.personalIssues.size)

        assertThat(badNamePerson.error.personalIssues)
            .anyMatch { it is BadName }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad name, age`() {
        val badNameAgePerson = create(nameX, ageX, license)
        assertFalse(badNameAgePerson.valid)
        assertThat(badNameAgePerson.error.personalIssues).hasSize(3)
            .anyMatch { it is BadName }
            .anyMatch { it is BadAge }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad name, license`() {
        val badNameLicensePerson = create(nameX, age, licenseX)

        assertFalse { badNameLicensePerson.valid }
        assertThat(badNameLicensePerson.error.personalIssues).hasSize(3)
            .anyMatch { it is BadName }
            .anyMatch { it is BadLicense }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad age`() {
        val badAgePerson = create(name, ageX, license)

        assertFalse { badAgePerson.valid }
        assertThat(badAgePerson.error.personalIssues).hasSize(2)
            .anyMatch { it is BadAge }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad age, license`() {
        val badAgeLicensePerson = create(name, ageX, licenseX)
        assertFalse(badAgeLicensePerson.valid)
        assertThat(badAgeLicensePerson.error.personalIssues).hasSize(3)
            .anyMatch { it is BadAge }
            .anyMatch { it is BadLicense }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad license`() {
        val badLicensePerson = create(name, age, licenseX)
        assertFalse(badLicensePerson.valid)
        assertThat(badLicensePerson.error.personalIssues).hasSize(2)
            .anyMatch { it is BadLicense }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `bad state bad`() {
        val badLicensePerson = create(name, 16, license)
        assertFalse(badLicensePerson.valid)
        assertThat(badLicensePerson.error.personalIssues).hasSize(2)
            .anyMatch { it is BadCombo }
            .anyMatch { it is TheBadness }
    }

    @Test
    fun `all bad`() {
        val badPerson = create(nameX, ageX, licenseX)
        assertFalse(badPerson.valid)
        assertThat(badPerson.error.personalIssues).hasSize(4)
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

//        test.RestResrouce().hello("foo", "bar")
//        test.RestResrouce().hello("biz", "zibb")
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

    fun hello(world: String, greeting: String): String =
        validate(errorList<String>()) {
            serviceLayer.greet(world, greeting) annotateInvalidated {
                "REST layer failed: $world/$greeting "
            } valueOr { errors ->
                throw IllegalStateException("Failed to greet: ${errors.joinToString(" => ") { it.trim() }}")
            }
        }
}

class ServiceLayer {

    fun greet(world: String, greeting: String) =
        validate(errorStrings) {
            getWorld(world) flatMap {
                it.accept(greeting)
            } validIf {
                it != "ouch"
            } orInvalidate {
                "Not a good response: $it"
            } annotateInvalidated {
                "World named $world and greeting $greeting was a no-go"
            }
        }

    private fun getWorld(world: String): Validated<Wrodl, List<String>> =
        validate(errorStrings) {
            when (world) {
                "biz" -> valid(Bizarro(1))
                "col" -> valid(Collider(2))
                else -> invalid("No world known as $world")
            }
        }
}

sealed class Wrodl {

    abstract fun accept(greeting: String): Validated<String, List<String>>
}

class Bizarro(private val x: Int) : Wrodl() {
    override fun accept(greeting: String): Validated<String, List<String>> =
        validate(errorStrings) {
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
    override fun accept(greeting: String): Validated<String, List<String>> =
        validate(errorStrings) {
            when (greeting) {
                "col" -> valid("ok: $y")
                "coll" -> valid("ouch: $y")
                else -> invalid("$this $y does not respond to $greeting")
            }
        }
}
