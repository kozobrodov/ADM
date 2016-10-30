package ru.kozobrodov

import com.opencsv.CSVReader
import org.apache.commons.cli.*
import java.io.FileInputStream
import java.io.FileReader
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val (properties, line) = parseArgs(args)
    val members = parseMembersCsv(line)
    Collections.shuffle(members)

    // Send mails
    val mailer = Mailer(properties)
    members.forEachIndexed { index, from ->
        val to = members[(if (index == 0) members.size else index) - 1]
        mailer.sendMail(from.email, properties.getProperty("adm.message.subject"), """
Hi, ${from.name}!

After our toss you've got to present a gift to ${to.name} for New Year 2017 party!

The deadline is ${properties.getProperty("adm.deadline")}.

Have a good day!
        """)
    }
}

private fun parseMembersCsv(files: List<String>): MutableList<Member> {
    val result = ArrayList<Member>()
    for (file in files) {
        val reader = CSVReader(FileReader(file))
        var line = reader.readNext()
        while (line != null) {
            if (line.size != 2) {
                throw Exception("Invalid CSV line: ${line.joinToString()}")
            }
            result.add(Member(line[0], line[1]))
            line = reader.readNext()
        }
    }
    return result
}

private fun parseArgs(args: Array<String>): Pair<Properties, List<String>> {
    val line: CommandLine = try {
        DefaultParser().parse(getCLIOptions(), args)
    } catch (e: ParseException) {
        printHelpAndExit()
        exitProcess(1)
    }

    if (line.argList.isEmpty()) {
        printHelpAndExit()
        exitProcess(1)
    }

    val properties = loadDefaultProperties()
    if (line.hasOption('c')) {
        properties.putAll(loadPropertiesFromFile(line.getOptionValue('c')))
    }

    return properties to line.argList
}

private fun loadDefaultProperties() = build(Properties()) {
    ClassLoader.getSystemClassLoader().getResourceAsStream("defaults.properties").use {
        this.load(it)
    }
}

private fun loadPropertiesFromFile(path: String) = build(Properties()) {
    FileInputStream(path).use {
        this.load(it)
    }
}

private fun printHelpAndExit() {
    val formatter = HelpFormatter()
    formatter.printHelp("adm [options] <csv_members_list>", getCLIOptions())
}

private fun getCLIOptions(): Options = build(Options()) {
    addOption("c", "config", true, "Prop")
    addOption(
        Option.builder("c")
            .hasArg()
            .argName("file")
            .longOpt("config")
            .desc("Property file with configuration which overrides standard config")
            .build()
    )
}

fun <T> build(element:T, build: T.()->Unit) = element.apply { build() }

data class Member(val name: String, val email: String)