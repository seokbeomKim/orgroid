package dev.seokbeomkim.orgroid.parser

/**
 * OrgItem: a class to represent an item in an org file.
 */
class OrgItem {
    var title: String = ""
    var status: String = ""
    var body: String = ""
    var priority: String = ""
    var progress: String = ""
    var scheduled: OrgDate? = null
    var deadline: OrgDate? = null

    fun getStartDate(): OrgDate? {
        val candidate1 = scheduled
        val candidate2 = deadline

        return if (candidate1 == null && candidate2 == null) {
            null
        } else if (candidate1 == null) {
            candidate2
        } else if (candidate2 == null) {
            candidate1
        } else {
            if (candidate1 < candidate2) {
                candidate1
            } else {
                candidate2
            }
        }
    }

    fun getEndDate(): OrgDate? {
        val candidate1 = scheduled
        val candidate2 = deadline

        return if (candidate1 == null && candidate2 == null) {
            null
        } else if (candidate1 == null) {
            candidate2
        } else if (candidate2 == null) {
            candidate1
        } else {
            if (candidate1 > candidate2) {
                candidate1
            } else {
                candidate2
            }
        }
    }

    override fun toString(): String {
        var rvalue = "Title: $title\n"
        rvalue += "Status: $status\n"
        rvalue += "Body: $body\n"
        rvalue += "Schedule: $scheduled\n"
        rvalue += "Deadline: $deadline"
        return rvalue
    }
}