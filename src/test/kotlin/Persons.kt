import com.github.kjetilv.whitebear.Validated
import com.github.kjetilv.whitebear.failureList
import com.github.kjetilv.whitebear.validated

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

val personIssues = failureList<PersonIssue>()

internal fun personX(name: String, age: Int, license: String?): Validated<Person, List<PersonIssue>> =
    validated(personIssues) {
        valid(name) map { it.trim() } validateThat {
            it.startsUppercased
        } elseInvalid {
            BadName("Name must start with uppercase: $it")
        } zipWith {
            validateThat(age) {
                it.realisticAge
            } elseInvalid {
                BadAge("Invalid age: $it")
            }
        } zipWith {
            validateThat(license) {
                it?.isNumeric
            } elseInvalid {
                BadLicense("Bad license: $it")
            }
        } flatMap { name: String, age: Int, license: String? ->
            validateThat(Person(name, age, license)) {
                it.licensedOk
            } elseInvalid {
                BadCombo("Too young to drive: $it")
            }
        } annotateInvalid {
            TheBadness("Person validation failed")
        }
    }

internal fun person0(name: String, age: Int, license: String?): Validated<Person, List<PersonIssue>> =
    validated(personIssues) {

        val inputValidations =
            collect(

                valid(name) validateThat {
                    it.toCharArray().firstOrNull()?.isUpperCase()
                } elseInvalid {
                    BadName("Name must start with uppercase: $name")
                },

                valid(age) validateThat {
                    it.realisticAge
                } elseInvalid {
                    BadAge("Invalid age: $age")
                },

                valid(license) validateThat {
                    it?.isNumeric
                } elseInvalid {
                    BadLicense("Bad license: $license")
                }
            )

        val validatedPerson =
            inputValidations ifValid {
                valid(Person(name, age, license))
            } validateThat {
                it.licensedOk
            } elseInvalid {
                BadCombo("Too young to drive: $it")
            }

        validatedPerson annotateInvalid {
            TheBadness("Person validation failed")
        }
    }

internal fun person1(name: String, age: Int, license: String?): Validated<Person, List<PersonIssue>> =
    validated(personIssues) {

        val validName = validateThat(name) {
            it.startsUppercased
        } elseInvalid {
            BadName("Name must start with uppercase: $it")
        }

        val validAge: Validated<Int, List<PersonIssue>> = validateThat(age) {
            it.realisticAge
        } elseInvalid {
            BadAge("Invalid age: $it")
        }

        val validLicense = validateThat(license) {
            it?.isNumeric
        } elseInvalid {
            BadLicense("Bad license: $it")
        }

        val zip = zip(validName, validAge, validLicense)

        zip map ::Person validateThat {
            it.licensedOk
        } elseInvalid {
            BadCombo("Too young to drive: $validName")
        } annotateInvalid {
            TheBadness("$validName: Person validation failed")
        }
    }

internal fun person1a(name: String, age: Int, license: String?): Validated<Person, List<PersonIssue>> =
    validated(personIssues) {

        val validatedPerson = (validateThat(name) {
            it.startsUppercased
        } elseInvalid {
            BadName("Name must start with uppercase: $it")
        }).zipWith(
            validateThat(age) {
                it.realisticAge
            } elseInvalid {
                BadAge("Invalid age: $it")
            },
            validateThat(license) {
                it?.isNumeric
            } elseInvalid {
                BadLicense("Bad license: $it")
            }
        ) map ::Person

        validatedPerson validateThat {
            it.licensedOk
        } elseInvalid {
            BadCombo("Too young to drive: $validatedPerson")
        } annotateInvalid {
            TheBadness("$validatedPerson: Person validation failed")
        }
    }

internal fun person2(name: String, age: Int, license: String?): Validated<Person, List<PersonIssue>> {
    return validated(personIssues) {

        validateThat(name) {
            it.startsUppercased
        } elseInvalid {
            BadName("Name must start with uppercase: $it")
        } zipWith {
            validateThat(age) {
                it.realisticAge
            } elseInvalid {
                BadAge("Invalid age: $it")
            }
        } zipWith {
            validateThat(license) {
                it?.isNumeric
            } elseInvalid {
                BadLicense("Bad license: $it")
            }
        } map ::Person validateThat {
            it.licensedOk
        } elseInvalid {
            BadCombo("Too young to drive: $it")
        } annotateInvalid {
            TheBadness("Person validation failed")
        }
    }
}

internal fun person3(name: String, age: Int, license: String?): Validated<Person, List<PersonIssue>> =
    validated(personIssues) {

        val nameVal = validateThat(name) {
            it.startsUppercased
        } elseInvalid {
            BadName("Name must start with uppercase: $it")
        }

        val ageVal = validateThat(age) {
            it.realisticAge
        } elseInvalid {
            BadAge("Invalid age: $it")
        }

        val licVal = validateThat(license) {
            it?.isNumeric
        } elseInvalid {
            BadLicense("Bad license: $it")
        }

        collect(nameVal, ageVal, licVal) ifValid {
            validateThat(Person(name, age, license)) {
                it.licensedOk
            } elseInvalid {
                BadCombo("Too young to drive: $it")
            }
        } annotateInvalid {
            TheBadness("Person validation failed")
        }
    }

internal fun person4(name: String, age: Int, license: String?): Validated<Person, List<PersonIssue>> {
    return validated(personIssues) {
        collect(
            validateThat(name) {
                it.startsUppercased
            } elseInvalid {
                BadName("Name must start with uppercase: $it")
            },
            validateThat(age) {
                it.realisticAge
            } elseInvalid {
                BadAge("Invalid age: $it")
            },
            validateThat(license) {
                it?.isNumeric
            } elseInvalid {
                BadLicense("Bad license: $it")
            }
        ) ifValid {
            validateThat(Person(name, age, license)) {
                it.licensedOk
            } elseInvalid {
                BadCombo("Too young to drive: $it")
            }
        } annotateInvalid {
            TheBadness("Person validation failed")
        }
    }
}
