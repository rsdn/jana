package org.rsdn.jana.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.ui.models.Forum
import org.rsdn.jana.resources.Res
import org.rsdn.jana.resources.*

/**
 * Возвращает Painter (иконку) для форума на основе его кода.
 * Все иконки должны лежать в src/commonMain/composeResources/drawable/
 */
@Composable
fun Forum.getIconPainter(): Painter {
    return painterResource(
        when (this.code) {
            "rsdn" -> Res.drawable.ic_web
            "mag" -> Res.drawable.ic_mag
            "news" -> Res.drawable.ic_news

            "dotnet", "dotnet.web", "dotnet.gui" -> Res.drawable.ic_dotnet
            "cpp", "cpp.applied", "cpp.qt" -> Res.drawable.ic_cpp

            "db" -> Res.drawable.ic_db
            "java" -> Res.drawable.ic_java

            "job", "job.offers", "job.search" -> Res.drawable.ic_job

            "humour" -> Res.drawable.ic_joke
            "life", "life.cook" -> Res.drawable.ic_life
            "flame", "flame.comp", "flame.politics" -> Res.drawable.ic_flame

            "faq" -> Res.drawable.ic_faq

            else -> Res.drawable.ic_default
        }
    )
}