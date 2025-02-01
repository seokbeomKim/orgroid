package dev.seokbeomkim.orgroid

import dev.seokbeomkim.orgtodo.parser.OrgItem
import dev.seokbeomkim.orgtodo.parser.OrgParser
import dev.seokbeomkim.orgtodo.parser.OrgProperty
import org.junit.Test

import org.junit.Assert.*
import java.io.BufferedReader

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class OrgParserUnitTest {
    @Test
    fun readFileFromResource() {
        val inputStream = this.javaClass.getResourceAsStream("/test.org")
        val reader = BufferedReader(inputStream?.reader() ?: null)

        while (reader.ready()) {
            reader.lines().forEach { x -> println(x) }
        }
    }

    @Test
    fun parseOrgHeader() {
        val fstream = this.javaClass.getResourceAsStream("/test.org")
        val parser = OrgParser()
        parser.open(fstream)
        parser.parse()
        parser.dumpItems()
    }

    @Test
    fun orgStringToDateInstance() {
        var test: OrgItem = OrgItem()
        test.setProperty(OrgProperty.SCHEDULED, "2025-01-30 Thu")
        test.setProperty(OrgProperty.DEADLINE, "2025-01-31 Fri")

        assertEquals(
            test.toDateFromOrgString(test.getProperty(OrgProperty.SCHEDULED).toString()).time,
            Date(2025, 1, 30).time
        )
        assertEquals(
            test.toDateFromOrgString(test.getProperty(OrgProperty.DEADLINE).toString()).time,
            Date(2025,1,31).time)
    }
}