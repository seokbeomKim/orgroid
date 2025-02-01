package dev.seokbeomkim.orgtodo.parser

class OrgConfigurator {

    companion object {
        val instance = OrgConfigurator()

        var statusList: MutableList<String> = mutableListOf("TODO", "IN-PROGRESS", "DONE")
        var propertyList: MutableList<String> = mutableListOf("SCHEDULED", "DEADLINE")

        fun checkStatusExist(status: String): Boolean {
            return statusList.contains(status)
        }

        fun checkPropertyExist(property: String): Boolean {
            return propertyList.contains(property)
        }

        fun addStatus(status: String): Boolean {
            if (statusList.contains(status)) {
                statusList.add(status)
                return true
            }
            return false
        }

        fun addProperty(property: String) {
            if (propertyList.contains(property)) {
                propertyList.add(property);
            }
        }

        fun removeStatus(status: String) {
            if (statusList.contains(status)) {
                statusList.remove(status);
            }
        }

        fun removeProperty(property: String) {
            if (propertyList.contains(property)) {
                propertyList.remove(property);
            }
        }
    }
}