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

    override fun toString(): String {
        var rvalue = "Title: $title\n"
        rvalue += "Status: $status\n"
        rvalue += "Body: $body\n"
        rvalue += "Schedule: $scheduled\n"
        rvalue += "Deadline: $deadline"
        return rvalue
    }
}