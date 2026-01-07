package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Filled.TikTok: ImageVector
    get() {
        if (tiktok != null) return tiktok!!

        tiktok = materialIcon(
            name = "Filled.TikTok"
        ){
            materialPath {
                moveTo(9f, 0f)
                horizontalLineToRelative(1.98f)
                curveToRelative(0.144f, 0.715f, 0.54f, 1.617f, 1.235f, 2.512f)
                curveTo(12.895f, 3.389f, 13.797f, 4f, 15f, 4f)
                verticalLineToRelative(2f)
                curveToRelative(-1.753f, 0f, -3.07f, -0.814f, -4f, -1.829f)
                verticalLineTo(11f)
                arcToRelative(5f, 5f, 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -5f,
                    dy1 = -5f
                )
                verticalLineToRelative(2f)
                arcToRelative(3f, 3f, 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 3f,
                    dy1 = 3f
                )
                close()
            }
        }

        return tiktok!!
    }

private var tiktok: ImageVector? = null

