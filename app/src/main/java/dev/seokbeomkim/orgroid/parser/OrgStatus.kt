package dev.seokbeomkim.orgroid.parser

/**
 * A class to manage the status of Org node.
 *
 * By default, the status list contains "TODO", "IN-PROGRESS", "DONE".
 * But user can add or remove the status.
 */
class OrgStatus {
    companion object {
        private var statusList: MutableList<String> = mutableListOf("TODO", "IN-PROGRESS", "DONE")

        fun checkValidation(status: String): Boolean {
            return statusList.contains(status)
        }

        fun addStatus(status: String): Boolean {
            if (statusList.contains(status)) {
                statusList.add(status)
                return true
            }
            return false
        }

        fun removeStatus(status: String) {
            if (statusList.contains(status)) {
                statusList.remove(status)
            }
        }
    }
}