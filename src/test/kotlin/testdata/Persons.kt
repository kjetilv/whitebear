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
            PersonalIssues(BadName("Name must start with uppercase: $it"))
        } zipWith {
            valid(age) validIf {
                it.realisticAge
            } orInvalidate {
                PersonalIssues(BadAge("Invalid age: $it"))
            }
        } zipWith {
            validIf(license) {
                it?.isNumeric
            } orInvalidate {
                PersonalIssues(BadLicense("Bad test.license: $it"))
            }
        } flatMap { name: String, age: Int, license: String? ->
            validIf(Person(name, age, license)) {
                it.licensedOk
            } orInvalidate {
                PersonalIssues(BadCombo("Too young to drive: $it"))
            }
        } annotateInvalidated {
            PersonalIssues(TheBadness("Person validation failed"))
        }
    }

internal fun person0(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        val inputValidations =
            collect(

                valid(name) validIf {
                    it.toCharArray().firstOrNull()?.isUpperCase()
                } orInvalidate {
                    BadName("Name must start with uppercase: $name").issues()
                },

                valid(age) validIf {
                    it.realisticAge
                } orInvalidate {
                    BadAge("Invalid age: $age").issues()
                },

                valid(license) validIf {
                    it?.isNumeric
                } orInvalidate {
                    BadLicense("Bad test.license: $license").issues()
                }
            )

        val validatedPerson =
            inputValidations ifValid {
                valid(Person(name, age, license))
            } validIf {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it").issues()
            }

        validatedPerson annotateInvalidated {
            TheBadness("Person validation failed").issues()
        }
    }

internal fun person1(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        val validName = validIf(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it").issues()
        }

        val validAge: Validated<Int, PersonalIssues> = validIf(age) {
            it.realisticAge
        } orInvalidate {
            BadAge("Invalid age: $it").issues()
        }

        val validLicense = validIf(license) {
            it?.isNumeric
        } orInvalidate {
            BadLicense("Bad test.license: $it").issues()
        }

        val zipped = validName zipWith validAge zipWith validLicense

        zipped map ::Person validIf {
            it.licensedOk
        } orInvalidate {
            BadCombo("Too young to drive: $validName").issues()
        } annotateInvalidated {
            TheBadness("$validName: Person validation failed").issues()
        }
    }

internal fun person1a(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        val validatedPerson = (validIf(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it").issues()
        }).zipWith(
            validIf(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it").issues()
            },
            validIf(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad test.license: $it").issues()
            }
        ) map ::Person

        validatedPerson validIf {
            it.licensedOk
        } orInvalidate {
            BadCombo("Too young to drive: $validatedPerson").issues()
        } annotateInvalidated {
            TheBadness("$validatedPerson: Person validation failed").issues()
        }
    }

internal fun person2(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        validIf(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it").issues()
        } zipWith {
            validIf(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it").issues()
            }
        } zipWith {
            validIf(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad test.license: $it").issues()
            }
        } map ::Person validIf {
            it.licensedOk
        } orInvalidate {
            BadCombo("Too young to drive: $it").issues()
        } annotateInvalidated {
            TheBadness("Person validation failed").issues()
        }
    }

internal fun person3(name: String, age: Int, license: String?) =
    validate(personalIssues) {

        val nameVal = validIf(name) {
            it.startsUppercased
        } orInvalidate {
            BadName("Name must start with uppercase: $it").issues()
        }

        val ageVal = validIf(age) {
            it.realisticAge
        } orInvalidate {
            BadAge("Invalid age: $it").issues()
        }

        val licVal = validIf(license) {
            it?.isNumeric
        } orInvalidate {
            BadLicense("Bad test.license: $it").issues()
        }

        collect(nameVal, ageVal, licVal) ifValid {
            valid(Person(name, age, license)) validIf {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it").issues()
            }
        } annotateInvalidated {
            TheBadness("Person validation failed").issues()
        }
    }

internal fun person4(name: String, age: Int, license: String?) =
    validate(personalIssues) {
        collect(
            validIf(name) {
                it.startsUppercased
            } orInvalidate {
                BadName("Name must start with uppercase: $it").issues()
            },
            validIf(age) {
                it.realisticAge
            } orInvalidate {
                BadAge("Invalid age: $it").issues()
            },
            validIf(license) {
                it?.isNumeric
            } orInvalidate {
                BadLicense("Bad test.license: $it").issues()
            }
        ) ifValid {
            validIf(Person(name, age, license)) {
                it.licensedOk
            } orInvalidate {
                BadCombo("Too young to drive: $it").issues()
            }
        } annotateInvalidated {
            TheBadness("Person validation failed").issues()
        }
    }
