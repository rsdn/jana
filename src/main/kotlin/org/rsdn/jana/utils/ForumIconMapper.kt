package org.rsdn.jana.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.LiveHelp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.rsdn.jana.ui.models.Forum

/**
 * Функция-расширение, которая возвращает иконку (ImageVector) на основе кода форума.
 */
@Composable
fun Forum.getIcon(): ImageVector {
    return when (this.code) {
        // --- Сайт и Журнал ---
        "rsdn" -> Icons.Default.Web
        "mag" -> Icons.AutoMirrored.Filled.MenuBook
        "news" -> Icons.Default.Campaign

        // --- Программирование :: Microsoft Windows ---
        "winapi" -> Icons.Default.Window
        "com" -> Icons.Default.Layers
        "mfc" -> Icons.Default.SportsEsports // Старое, игровое
        "atl" -> Icons.Default.BuildCircle

        // --- Программирование :: .NET ---
        "dotnet" -> Icons.Default.Code
        "dotnet.web" -> Icons.Default.CloudSync
        "dotnet.gui" -> Icons.Default.DesktopWindows
        "nemerle" -> Icons.Default.IntegrationInstructions

        // --- Программирование :: C++ ---
        "cpp" -> Icons.Default.Terminal
        "cpp.applied" -> Icons.Default.Extension
        "cpp.qt" -> Icons.Default.DashboardCustomize

        // --- Программирование :: Разное ---
        "db" -> Icons.Default.Storage
        "java" -> Icons.Default.Coffee
        "network" -> Icons.Default.Router
        "web" -> Icons.Default.Html
        "xml" -> Icons.Default.DataObject
        "alg" -> Icons.Default.Psychology
        "unix" -> Icons.Default.DeveloperBoard
        "asm" -> Icons.Default.Memory
        "media" -> Icons.Default.MusicVideo
        "game" -> Icons.Default.SportsEsports
        "pda" -> Icons.Default.Phonelink
        "setup" -> Icons.Default.SettingsApplications
        "hardware" -> Icons.Default.Hardware
        "decl" -> Icons.Default.Hive
        "dynamic" -> Icons.Default.Category
        "cloud" -> Icons.Default.CloudCircle
        "blockchain" -> Icons.Default.VpnKey
        "ai" -> Icons.Default.AutoAwesome

        // --- Программирование :: Работа ---
        "job", "job.offers", "job.offers.ea" -> Icons.Default.WorkHistory
        "job.search" -> Icons.Default.PersonSearch
        "shareware" -> Icons.Default.MonetizationOn

        // --- Авторские форумы ---
        "src" -> Icons.Default.Source
        "faq" -> Icons.AutoMirrored.Filled.LiveHelp
        "etude" -> Icons.Default.EmojiObjects

        // --- Открытые проекты ---
        "janus", "nntp", "prj", "prj.rfd", "prj.nemerle", "prj.codejam" -> Icons.Default.GroupWork

        // --- Offtopic ---
        "humour" -> Icons.Default.SentimentSatisfiedAlt
        "life" -> Icons.Default.SelfImprovement
        "life.cook" -> Icons.Default.Restaurant
        "education" -> Icons.Default.School
        "auto" -> Icons.Default.DirectionsCar
        "diy" -> Icons.Default.Handyman
        "abroad" -> Icons.Default.FlightTakeoff
        "flame", "flame.comp", "flame.politics" -> Icons.Default.LocalFireDepartment

        // --- Программирование :: Общие вопросы ---
        "design" -> Icons.Default.Architecture
        "philosophy" -> Icons.Default.RecordVoiceOver
        "tools" -> Icons.Default.AppRegistration
        "dictionary" -> Icons.Default.Translate
        "management" -> Icons.Default.AccountTree
        "usability" -> Icons.Default.TouchApp
        "apptesting" -> Icons.Default.VerifiedUser
        "jetbrains" -> Icons.AutoMirrored.Filled.TextSnippet
        "htmlayout" -> Icons.Default.BorderColor
        "security" -> Icons.Default.Security

        // --- Сервисные и тесты ---
        "info" -> Icons.Default.Info
        "test", "test2", "blogs" -> Icons.Default.BugReport

        // Иконка по умолчанию для новых или неизвестных кодов
        else -> Icons.AutoMirrored.Filled.Label
    }
}