package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
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
@RequestMapping("/order/close")
class OrderCloseScreen : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam id: String): Widget {
        val xid = id.uppercase().takeLast(4)
        return Screen(
            id = Page.ORDER_CLOSE,
            backgroundColor = Theme.COLOR_WHITE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_PRIMARY,
                foregroundColor = Theme.COLOR_WHITE,
                title = getText("page.order.close.app-bar.title", arrayOf(xid))
            ),
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.center,
                children = listOfNotNull(
                    Container(padding = 30.0),
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = Text(
                            caption = getText("page.order.close.title"),
                            size = Theme.TEXT_SIZE_LARGE,
                            bold = true,
                            color = Theme.COLOR_PRIMARY
                        )
                    ),
                    Container(
                        padding = 10.0,
                        alignment = Alignment.Center,
                        child = Text(
                            caption = getText("page.order.close.sub-title")
                        )
                    ),

                    Container(padding = 10.0),
                    Container(
                        padding = 10.0,
                        child = Button(
                            caption = getText("page.order.close.button.submit"),
                            action = executeCommand(urlBuilder.build("commands/close-order?id=$id"))
                        )
                    ),
                    Button(
                        type = ButtonType.Text,
                        caption = getText("page.order.close.button.not-now"),
                        action = gotoPreviousScreen()
                    )
                )
            )
        ).toWidget()
    }
}
