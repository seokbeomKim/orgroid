package dev.seokbeomkim.orgroid.parser

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