import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Mailer {
    // Keys
    val AUTH = "mail.smtp.auth"
    val FROM = "adm.message.from"
    val USERNAME = "mail.username"
    val PASSWORD = "mail.password"
    val HOST = "mail.smtp.host"
    
    private val session: Session
    private val from: InternetAddress

    constructor(properties: Properties) {
        if (properties.containsKey(AUTH)) {
            val (username, password) = getCredentials(properties)
            session = Session.getDefaultInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })
        } else {
            session = Session.getDefaultInstance(properties)
        }
        from = InternetAddress(properties.getProperty(FROM))
    }

    @Suppress("UsePropertyAccessSyntax")
    fun sendMail(to: String, subject: String, message: String) {
        val mimeMessage = MimeMessage(session)

        mimeMessage.setFrom(from)
        mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(to))
        mimeMessage.setSubject(subject)
        mimeMessage.setText(message)

        try {
            Transport.send(mimeMessage)
        } catch (mex: MessagingException) {
            System.err.println("Cannot send message to $to")
        }
    }

    private fun getCredentials(properties: Properties): Pair<String, String> {
        if (properties.containsKey(USERNAME) && properties.containsKey(PASSWORD)) {
            return properties.getProperty(USERNAME) to properties.getProperty(PASSWORD)
        }
        return getCredentialsFromConsole(properties)
    }

    private fun getCredentialsFromConsole(properties: Properties): Pair<String, String> {
        val console = System.console() ?: throw Exception("Cannot get access to console for reading user credentials")
        console.printf("Set username for ${properties.getProperty(HOST)}: ")
        val username = console.readLine()
        console.printf("Set password for ${properties.getProperty(HOST)}: ")
        val password = String(console.readPassword())
        return username to password
    }
}
