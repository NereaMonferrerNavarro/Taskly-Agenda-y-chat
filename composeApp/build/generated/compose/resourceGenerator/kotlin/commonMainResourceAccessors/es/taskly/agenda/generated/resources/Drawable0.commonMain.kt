@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class)

package es.taskly.agenda.generated.resources

import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.InternalResourceApi

private object CommonMainDrawable0 {
  public val logo: DrawableResource by 
      lazy { init_logo() }
}

@InternalResourceApi
internal fun _collectCommonMainDrawable0Resources(map: MutableMap<String, DrawableResource>) {
  map.put("logo", CommonMainDrawable0.logo)
}

internal val Res.drawable.logo: DrawableResource
  get() = CommonMainDrawable0.logo

private fun init_logo(): DrawableResource = org.jetbrains.compose.resources.DrawableResource(
  "drawable:logo",
    setOf(
      org.jetbrains.compose.resources.ResourceItem(setOf(),
    "composeResources/es.taskly.agenda.generated.resources/drawable/logo.png", -1, -1),
    )
)
