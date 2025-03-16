package dev.seokbeomkim.orgroid.parser

/**
 * OrgItem: a class to represent an item in an org file.
 */
class OrgItem {
    var title: String = ""
    var status: String = ""
    var body: String = ""
    var priority: String = ""
    var properties: MutableMap<String, String> = mutableMapOf()
    var progress: String = ""
    var scheduled: OrgDate? = null
    var deadline: OrgDate? = null

    fun getStartDate(): OrgDate? {
        val cand1 = scheduled
        val cand2 = deadline

        return if (cand1 == null && cand2 == null) {
            null
        } else if (cand1 == null) {
            cand2
        } else if (cand2 == null) {
            cand1
        } else {
            if (cand1 < cand2) {
                cand1
            } else {
                cand2
            }
        }
    }

    fun getEndDate(): OrgDate? {
        val cand1 = scheduled
        val cand2 = deadline

        return if (cand1 == null && cand2 == null) {
            null
        } else if (cand1 == null) {
            cand2
        } else if (cand2 == null) {
            cand1
        } else {
            if (cand1 > cand2) {
                cand1
            } else {
                cand2
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