package testdata

import com.github.kjetilv.whitebear.Errors

data class Person(val name: String, val age: Int, val driversLicense: String? = null)

data class PersonIssues(internal val personIssues: List<PersonIssue> = emptyList()) {

    internal val empty get() = personIssues.isEmpty()
}

class PersonIssuesErrors : Errors<PersonIssue, PersonIssues> {

    override val empty = PersonIssues()

    override fun add(aggregator: PersonIssues, error: PersonIssue) =
        PersonIssues(aggregator.personIssues + error)

    override fun isEmpty(aggregator: PersonIssues) = aggregator.empty

    override fun combine(aggregator1: PersonIssues, aggregator2: PersonIssues) =
        PersonIssues(aggregator1.personIssues + aggregator2.personIssues)
}

sealed interface PersonIssue

data class BadAge(val msg: String) : PersonIssue

data class BadName(val msg: String) : PersonIssue

data class BadLicense(val msg: String) : PersonIssue

data class BadCombo(val msg: String) : PersonIssue

data class TheBadness(val msg: String) : PersonIssue
