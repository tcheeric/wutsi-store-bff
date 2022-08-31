package com.wutsi.application.store.endpoint.settings.product.picture.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.PhotoView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ButtonType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/picture")
class SettingsPictureScreen(
    private val catalogApi: WutsiCatalogApi
) : AbstractQuery() {
    @PostMapping
    fun index(
        @RequestParam(name = "product-id") productId: Long,
        @RequestParam("picture-id") pictureId: Long
    ): Widget {
        val product = catalogApi.getProduct(productId).product
        val picture = product.pictures.find { it.id == pictureId }!!

        return Screen(
            id = Page.SETTINGS_STORE_PICTURE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.picture.app-bar.title")
            ),
            child = PhotoView(url = picture.url),
            floatingActionButton = Button(
                type = ButtonType.Floatable,
                icon = Theme.ICON_DELETE,
                stretched = false,
                color = Theme.COLOR_WHITE,
                action = gotoUrl(
                    url = urlBuilder.build("settings/store/picture/delete?picture-id=$pictureId&product-id=$productId\""),
                    replacement = true
                )
            )
        ).toWidget()
    }
}
