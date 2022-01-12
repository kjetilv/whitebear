data class Person(val name: String, val age: Int, val driversLicense: String? = null)
sealed interface PersonIssue
data class BadAge(val msg: String) : PersonIssue
data class BadName(val msg: String) : PersonIssue
data class BadLicense(val msg: String) : PersonIssue
data class BadCombo(val msg: String) : PersonIssue
data class TheBadness(val msg: String) : PersonIssue
