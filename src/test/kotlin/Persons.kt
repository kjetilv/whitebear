import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.validate

const val name = "Kjetil"
const val nameX = "kjetil"
const val age = 49
const val ageX = 200
const val license = "12345"
const val licenseX = "foobar"
internal val String.startsUppercased: Boolean get() = toCharArray().firstOrNull()?.isUpperCase() ?: false
internal val String.isNumeric get() = chars().allMatch(Character::isDigit)
internal val Int.realisticAge get() = this in 0..120
internal val Person.licensedOk get() = age >= 18 || driversLicense == null

val personIssues = PersonIssuesErrorModel()

internal fun personX(name: String, age: Int, license: String?) =
    validate(personIssues) {
        valid(name) map { it.trim() } validateThat {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        } zipWith {
            validateThat(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it")
            }
        } zipWith {
            validateThat(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad license: $it")
            }
        } flatMap { name: String, age: Int, license: String? ->
            validateThat(Person(name, age, license)) {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it")
            }
        } annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }

internal fun person0(name: String, age: Int, license: String?) =
    validate(personIssues) {

        val inputValidations =
            collect(

                valid(name) validateThat {
                    it.toCharArray().firstOrNull()?.isUpperCase()
                } orInvalidate {
                    BadName("Name must start with uppercase: $name")
                },

                valid(age) validateThat {
                    it.realisticAge
                } orInvalidate {
                    BadAge("Invalid age: $age")
                },

                valid(license) validateThat {
                    it?.isNumeric
                } orInvalidate {
                    BadLicense("Bad license: $license")
                }
            )

        val validatedPerson =
            inputValidations flatMap  {
                valid(Person(name, age, license))
            } validateThat {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it")
            }

        validatedPerson annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }

internal fun person1(name: String, age: Int, license: String?) =
    validate(personIssues) {

        val validName = validateThat(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        }

        val validAge: Validated<Int, PersonIssues> = validateThat(age) {
            it.realisticAge
        } orInvalidate {
            BadAge("Invalid age: $it")
        }

        val validLicense = validateThat(license) {
            it?.isNumeric
        } orInvalidate {
            BadLicense("Bad license: $it")
        }

        val zipped = validName zipWith validAge zipWith validLicense

        zipped map ::Person validateThat {
            it.licensedOk
        } orInvalidate {
            BadCombo("Too young to drive: $validName")
        } annotateInvalidated {
            TheBadness("$validName: Person validation failed")
        }
    }

internal fun person1a(name: String, age: Int, license: String?) =
    validate(personIssues) {

        val validatedPerson = (validateThat(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        }).zipWith(
            validateThat(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it")
            },
            validateThat(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad license: $it")
            }
        ) map ::Person

        validatedPerson validateThat {
            it.licensedOk
        } orInvalidate {
            BadCombo("Too young to drive: $validatedPerson")
        } annotateInvalidated {
            TheBadness("$validatedPerson: Person validation failed")
        }
    }

internal fun person2(name: String, age: Int, license: String?) =
    validate(personIssues) {

        validateThat(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        } zipWith {
            validateThat(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it")
            }
        } zipWith {
            validateThat(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad license: $it")
            }
        } map ::Person validateThat {
            it.licensedOk
        } orInvalidate {
            BadCombo("Too young to drive: $it")
        } annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }

internal fun person3(name: String, age: Int, license: String?) =
    validate(personIssues) {

        val nameVal = validateThat(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        }

        val ageVal = validateThat(age) {
            it.realisticAge
        } orInvalidate {
            BadAge("Invalid age: $it")
        }

        val licVal = validateThat(license) {
            it?.isNumeric
        } orInvalidate {
            BadLicense("Bad license: $it")
        }

        collect(nameVal, ageVal, licVal) flatMap {
            validateThat(Person(name, age, license)) {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it")
            }
        } annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }

internal fun person4(name: String, age: Int, license: String?) =
    validate(personIssues) {
        collect(
            validateThat(name) {
                it.startsUppercased
            } orInvalidate {
                BadName("Name must start with uppercase: $it")
            },
            validateThat(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it")
            },
            validateThat(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad license: $it")
            }
        ) flatMap {
            validateThat(Person(name, age, license)) {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it")
            }
        } annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }
