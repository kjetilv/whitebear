package testdata

import com.github.kjetilv.whitebear.ErrorProcessor

data class Person(val name: String, val age: Int, val driversLicense: String? = null)

data class PersonalIssues(internal val personalIssues: List<PersonalIssue> = emptyList()) {

    internal val empty get() = personalIssues.isEmpty()
}

class PersonalIssuesErrorProcessor : ErrorProcessor<PersonalIssue, PersonalIssues> {

    override val empty = PersonalIssues()

    override fun wrap(error: PersonalIssue) = PersonalIssues(listOf(error))

    override fun isEmpty(aggregator: PersonalIssues) = aggregator.empty

    override fun combine(aggregator1: PersonalIssues, aggregator2: PersonalIssues) =
        PersonalIssues(aggregator1.personalIssues + aggregator2.personalIssues)
}

sealed interface PersonalIssue

data class BadAge(val msg: String) : PersonalIssue

data class BadName(val msg: String) : PersonalIssue

data class BadLicense(val msg: String) : PersonalIssue

data class BadCombo(val msg: String) : PersonalIssue

data class TheBadness(val msg: String) : PersonalIssue
