package dev.seokbeomkim.orgroid

import dev.seokbeomkim.orgroid.parser.OrgParser
import org.junit.Assert.assertEquals
import org.junit.Test

class OrgParserUnitTest {
    @Test
    fun testParseDump() {
        // Test Parser's dump method.
        // This testcase does not compare values but just print them.
        val stream = this.javaClass.getResourceAsStream("/test.org")
        val parser = OrgParser()
        if (stream != null) {
            parser.open(stream)
        }
        parser.parse(false, true)
        println(parser)
    }

    @Test
    fun parseOrgHeader() {
        val fstream = this.javaClass.getResourceAsStream("/test.org")
        val parser = OrgParser()
        if (fstream != null) {
            parser.open(fstream)
        }
        parser.parse()

        val shouldBe: List<String> = listOf(
            "Header",
            "Sub-header",
            "Sub-header",
            "Sub-header with priority",
            "Sub-header with B priority",
            "Sub-header",
            "Sub-header",
            "Sub-header",
            "Ranged timestamp",
            "Sub-header 2",
            "Sub-header 2",
            "Sub-header 3",
            "Sub-header 3",
            "Sub-header 4",
            "Test",
            "Sub-Task 1",
            "Sub-Sub-Task #1",
            "Sub-Sub-Task #2",
            "Task #1-2-1",
            "Task #1-2-2",
            "Task #1-2-3",
            "Finished Task",
            "Property Test",
        )
        parser.getItems().forEachIndexed({ i, x ->
            assertEquals("Checking a header of item $i", shouldBe[i], x.title)
        })
    }

    @Test
    fun parseOrgSchedule() {
        // Test SCHEDULED property of the node.
        val fstream = this.javaClass.getResourceAsStream("/test.org")
        val parser = OrgParser()
        if (fstream != null) {
            parser.open(fstream)
        }
        parser.parse()

        val shouldbe: List<String?> = listOf(
            "2025-01-27 Mon",
            null,
            null,
            null,
            null,
            "2025-02-16 Sun",
            null,
            null,
            null,
            "2025-01-30 Thu",
            null,
            "2025-02-16 Sun 08:00-09:00",
            null,
            "2025-02-16 Sun 10:00",
            null,
            "2025-02-24 Mon ++1m",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
        )
        parser.getItems().forEachIndexed({ i, x ->
            assertEquals(
                "Checking a header of item $i:",
                shouldbe[i], x.scheduled?.start
            )
        })
    }

    @Test
    fun parseOrgDeadline() {
        // Test DEADLINE property of the node.
        val fstream = this.javaClass.getResourceAsStream("/test.org")
        val parser = OrgParser()
        if (fstream != null) {
            parser.open(fstream)
        }
        parser.parse(false, false)

        val shouldbe: List<String?> = listOf(
            "2025-02-22 Sat",
            "2025-02-22 Sat",
            "2025-01-31 Fri",
            "2025-02-16 Sun 08:00-09:00",
            "2025-02-16 Sun 08:00-09:00",
            "2025-01-27 Mon +1m",
            "2025-02-24 Mon +1w",
            "2025-02-28 Fri ++1w",
            "2025-02-11 Tue .+1w",
            "2025-02-11 Tue ++2h",
        )
        parser.getItems().forEachIndexed({ i, x ->
//            assertEquals("Checking a header of item $i:",
//                shouldbe[i], x.getProperty(OrgProperty.DEADLINE_FROM))
            println("$i : $x")
        })
    }
}