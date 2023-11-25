package spread.sheet.reader

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader

open class SpreadSheetReader : DefaultTask() {
    @Input
    lateinit var spreadSheetId: String

    @Input @Optional
    var spreadSheetRange: String? = null

    @TaskAction
    fun execute() {
        println("Hello from the 'spread-sheet-reader' plugin")
        println("spreadSheetId: $spreadSheetId")
        println("spreadSheetRange: $spreadSheetRange")
        println()

        executeSpreadSheet(
            spreadSheetId = spreadSheetId,
            range = spreadSheetRange ?: run {
                println("The argument \"spreadSheetRange\" is not set, so it will be set to the default value.")
                println("The default value is \"A3:A1000\".")
                "A3:A1000"
            }
        )
    }

    private val APPLICATION_NAME = "Google API Kotlin Quickstart"

    private val JSON_FACTORY = GsonFactory.getDefaultInstance()

    private val TOKENS_DIRECTORY_PATH = "tokens"

    private val SCOPE_CALENDAR = listOf(CalendarScopes.CALENDAR_READONLY)

    private val SCOPE_SPREAD_SHEET = listOf(SheetsScopes.SPREADSHEETS_READONLY)

    private val CREDENTIALS_FILE_PATH = "/credentials.json"

    private fun executeSpreadSheet(spreadSheetId: String, range: String) {
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val service = Sheets.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            getCredentials(HTTP_TRANSPORT, SCOPE_SPREAD_SHEET)
        )
            .setApplicationName(APPLICATION_NAME)
            .build()

        val spreadSheet = service.Spreadsheets().get(spreadSheetId).execute()
        val sheetTitleList = mutableListOf<String>()
        spreadSheet.sheets.forEach { sheet ->
            println("Sheet: ${sheet.properties.title}")
            sheetTitleList.add(sheet.properties.title)
        }
        sheetTitleList.forEach {
            val values = service.Spreadsheets().Values().get(spreadSheetId, "$it!$range").execute()
            println("$it values: $values")
        }
    }

    private fun executeCalendar() {
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val service = Calendar.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            getCredentials(HTTP_TRANSPORT, SCOPE_CALENDAR)
        )
            .setApplicationName(APPLICATION_NAME)
            .build()

        val now = DateTime(System.currentTimeMillis())
        val events = service.events().list("primary")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute()

        val items = events.items
        if (items.isEmpty()) {
            println("No upcoming events found.")
        } else {
            println("Upcoming events.")
            for (event in items) {
                var start = event.start.dateTime
                if (start == null) {
                    start = event.start.date
                }
                println("${event.summary} (${start})\n")
            }
        }
    }

    private fun getCredentials(httpTransport: NetHttpTransport, scopes: List<String>): Credential {
        // Load client secrets.
        val inputStream = SpreadSheetReaderPlugin::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
            ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, JSON_FACTORY, clientSecrets, scopes
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
}