package testdata

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

val personalIssues = PersonalIssuesErrorProcessor()

internal fun personX(name: String, age: Int, license: String?) =
    validate(personalIssues) {
        valid(name) map {
            it.trim()
        } validIf {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        } zipWith {
            valid(age) validIf {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it")
            }
        } zipWith {
            validIf(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad test.license: $it")
            }
        } flatMap { name: String, age: Int, license: String? ->
            validIf(Person(name, age, license)) {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it")
            }
        } annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }

internal fun person0(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        val inputValidations =
            collect(

                valid(name) validIf {
                    it.toCharArray().firstOrNull()?.isUpperCase()
                } orInvalidate {
                    BadName("Name must start with uppercase: $name")
                },

                valid(age) validIf {
                    it.realisticAge
                } orInvalidate {
                    BadAge("Invalid age: $age")
                },

                valid(license) validIf {
                    it?.isNumeric
                } orInvalidate {
                    BadLicense("Bad test.license: $license")
                }
            )

        val validatedPerson =
            inputValidations ifValid {
                valid(Person(name, age, license))
            } validIf {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it")
            }

        validatedPerson annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }

internal fun person1(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        val validName = validIf(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        }

        val validAge: Validated<Int, PersonalIssues> = validIf(age) {
            it.realisticAge
        } orInvalidate {
            BadAge("Invalid age: $it")
        }

        val validLicense = validIf(license) {
            it?.isNumeric
        } orInvalidate {
            BadLicense("Bad test.license: $it")
        }

        val zipped = validName zipWith validAge zipWith validLicense

        zipped map ::Person validIf {
            it.licensedOk
        } orInvalidate {
            BadCombo("Too young to drive: $validName")
        } annotateInvalidated {
            TheBadness("$validName: Person validation failed")
        }
    }

internal fun person1a(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        val validatedPerson = (validIf(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        }).zipWith(
            validIf(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it")
            },
            validIf(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad test.license: $it")
            }
        ) map ::Person

        validatedPerson validIf {
            it.licensedOk
        } orInvalidate {
            BadCombo("Too young to drive: $validatedPerson")
        } annotateInvalidated {
            TheBadness("$validatedPerson: Person validation failed")
        }
    }

internal fun person2(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        validIf(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        } zipWith {
            validIf(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it")
            }
        } zipWith {
            validIf(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad test.license: $it")
            }
        } map ::Person validIf {
            it.licensedOk
        } orInvalidate {
            BadCombo("Too young to drive: $it")
        } annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }

internal fun person3(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        val nameVal = validIf(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it")
        }

        val ageVal = validIf(age) {
            it.realisticAge
        } orInvalidate {
            BadAge("Invalid age: $it")
        }

        val licVal = validIf(license) {
            it?.isNumeric
        } orInvalidate {
            BadLicense("Bad test.license: $it")
        }

        collect(nameVal, ageVal, licVal) ifValid {
            valid(Person(name, age, license)) validIf {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it")
            }
        } annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }

internal fun person4(name: String, age: Int, license: String?) =
    validate(personalIssues) {
        collect(
            validIf(name) {
                it.startsUppercased
            } orInvalidate {
                BadName("Name must start with uppercase: $it")
            },
            validIf(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it")
            },
            validIf(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad test.license: $it")
            }
        ) ifValid {
            validIf(Person(name, age, license)) {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it")
            }
        } annotateInvalidated {
            TheBadness("Person validation failed")
        }
    }
