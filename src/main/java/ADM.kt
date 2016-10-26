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
    for (index in members.indices) {
        val from = members[index]
        val to = members[(if (index == 0) members.size else index) - 1]
        mailer.sendMail(from.email, properties.getProperty("adm.message.subject"), """
Hi, ${from.name}!

After our toss you've got to present a gift to ${to.name} for New Year 2017 party!

The deadline is ${properties.getProperty("adm.deadline")}.

Have a good day!
        """)
    }
}

fun parseMembersCsv(files: List<String>): MutableList<Member> {
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

fun parseArgs(args: Array<String>): Pair<Properties, List<String>> {
    val parser = DefaultParser()
    val line: CommandLine
    try {
        line = parser.parse(getCLIOptions(), args)
    } catch (e: ParseException) {
        printHelpAndExit()
        exitProcess(1)
    }

    val list = listOf<String>()

    if (line.argList.isEmpty()) {
        printHelpAndExit()
        exitProcess(1)
    }
    val properties = loadDefaultProperties()
    if (line.hasOption('c')) {
        properties + loadPropertiesFromFile(line.getOptionValue('c'))
    }

    return properties to list
}

fun loadDefaultProperties(): Properties {
    val properties = Properties()
    ClassLoader.getSystemClassLoader().getResourceAsStream("defaults.properties").use {
        properties.load(it)
    }
    return properties
}

fun loadPropertiesFromFile(path: String): Properties {
    val properties = Properties()
    FileInputStream(path).use {
        properties.load(it)
    }
    return properties
}

fun printHelpAndExit() {
    val formatter = HelpFormatter()
    formatter.printHelp("adm [options] <csv_members_list>", getCLIOptions())
}

fun getCLIOptions(): Options {
    val opts = Options()
    opts.addOption("c", "config", true, "Prop")
    opts.addOption(
        Option.builder("c")
            .hasArg()
            .argName("file")
            .longOpt("config")
            .desc("Property file with configuration which overrides standard config")
            .build()
    )
    return opts
}

operator fun Properties.plus(other: Properties) {
    for (propName in other.propertyNames()) {
        if (propName is String)
            setProperty(propName, other.getProperty(propName))
    }
}

data class Member(val name: String, val email: String)