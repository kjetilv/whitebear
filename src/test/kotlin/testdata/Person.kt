package testdata

import com.github.kjetilv.whitebear.ErrorProcessor

data class Person(val name: String, val age: Int, val driversLicense: String? = null)

data class PersonalIssues(internal val personalIssues: List<PersonalIssue> = emptyList()) {
    constructor(vararg personalIssues: PersonalIssue) : this(personalIssues.toList())

    internal val empty get() = personalIssues.isEmpty()
}

class PersonalIssuesErrorProcessor : ErrorProcessor<PersonalIssues> {

    override val empty = PersonalIssues()

    fun isEmpty(error: PersonalIssues) = error.empty

    override fun combine(error1: PersonalIssues, error2: PersonalIssues) =
        PersonalIssues(error1.personalIssues + error2.personalIssues)
}

sealed interface PersonalIssue {

    fun issues() = PersonalIssues(this)
}

data class BadAge(val msg: String) : PersonalIssue

data class BadName(val msg: String) : PersonalIssue

data class BadLicense(val msg: String) : PersonalIssue

data class BadCombo(val msg: String) : PersonalIssue

data class TheBadness(val msg: String) : PersonalIssue
