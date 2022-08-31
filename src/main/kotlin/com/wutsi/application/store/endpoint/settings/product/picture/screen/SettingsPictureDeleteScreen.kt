package com.wutsi.application.store.endpoint.settings.product.picture.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.PictureSummary
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/picture/delete")
class SettingsPictureDeleteScreen(
    private val catalogApi: WutsiCatalogApi
) : AbstractQuery() {
    companion object {
        const val IMAGE_WIDTH = 150.0
        const val IMAGE_HEIGHT = 150.0
    }

    @PostMapping
    fun index(
        @RequestParam(name = "product-id") productId: Long,
        @RequestParam("picture-id") pictureId: Long
    ): Widget {
        val product = catalogApi.getProduct(productId).product
        val picture = product.pictures.find { it.id == pictureId }!!

        return Screen(
            id = Page.SETTINGS_STORE_PICTURE_DELETE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.picture.delete.app-bar.title")
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = toPictureWidget(picture)
                    ),
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = Text(
                            caption = getText("page.settings.store.picture.delete.confirm")
                        )
                    ),
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = Button(
                            caption = getText("page.settings.store.picture.delete.button.yes"),
                            action = executeCommand(
                                url = urlBuilder.build("commands/delete-picture?picture-id=$pictureId")
                            )
                        )
                    ),
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = Button(
                            type = ButtonType.Text,
                            caption = getText("page.settings.store.picture.delete.button.no"),
                            action = gotoPreviousScreen()
                        )
                    )

                )
            )

        ).toWidget()
    }

    private fun toPictureWidget(picture: PictureSummary) = Container(
        width = IMAGE_WIDTH,
        height = IMAGE_HEIGHT,
        alignment = Alignment.Center,
        borderColor = Theme.COLOR_PRIMARY_LIGHT,
        border = 1.0,
        backgroundImageUrl = picture.url
    )
}
