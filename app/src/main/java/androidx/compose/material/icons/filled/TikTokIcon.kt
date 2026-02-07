/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2019-2024 The Bootstrap Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **/
package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Filled.TikTokIcon: ImageVector
    get() {
        if (tiktok != null) return tiktok!!

        tiktok = ImageVector.Builder(
                name = "tiktok",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 16f,
                viewportHeight = 16f
            ).apply {
                path(
                    fill = SolidColor(Color.Black)
                ) {
                    moveTo(9f, 0f)
                    horizontalLineToRelative(1.98f)
                    curveToRelative(0.144f, 0.715f, 0.54f, 1.617f, 1.235f, 2.512f)
                    curveTo(12.895f, 3.389f, 13.797f, 4f, 15f, 4f)
                    verticalLineToRelative(2f)
                    curveToRelative(-1.753f, 0f, -3.07f, -0.814f, -4f, -1.829f)
                    verticalLineTo(11f)
                    arcToRelative(5f, 5f, 0f, true, true, -5f, -5f)
                    verticalLineToRelative(2f)
                    arcToRelative(3f, 3f, 0f, true, false, 3f, 3f)
                    close()
                }
            }.build()

        return tiktok!!
    }

private var tiktok: ImageVector? = null

