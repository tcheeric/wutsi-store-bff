package com.wutsi.application.store.endpoint.settings.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListItemSwitch
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.tenant.entity.ToggleName
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store")
class SettingsStoreScreen : AbstractQuery() {
    @PostMapping
    fun index(): Widget {
        return Screen(
            id = Page.SETTINGS_STORE,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.app-bar.title")
            ),
            child = Column(
                children = listOf(
                    Container(
                        padding = 10.0,
                        alignment = Alignment.CenterLeft,
                        child = Text(
                            caption = getText("page.settings.store.message"),
                            alignment = TextAlignment.Left
                        )
                    ),
                    Divider(color = Theme.COLOR_DIVIDER, height = 2.0),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = listOfNotNull(
                                ListItem(
                                    caption = getText("page.settings.store.sections"),
                                    leading = Icon(code = Theme.ICON_CATEGORY, color = Theme.COLOR_PRIMARY),
                                    trailing = Icon(code = Theme.ICON_CHEVRON_RIGHT),
                                    action = gotoUrl(
                                        urlBuilder.build("settings/store/sections")
                                    )
                                ),
                                ListItem(
                                    caption = getText("page.settings.store.products"),
                                    leading = Icon(code = Theme.ICON_SHOPPING_BAG, color = Theme.COLOR_PRIMARY),
                                    trailing = Icon(code = Theme.ICON_CHEVRON_RIGHT),
                                    action = gotoUrl(
                                        urlBuilder.build("settings/store/products")
                                    )
                                ),

                                if (togglesProvider.isToggleEnabled(ToggleName.ORDER))
                                    ListItem(
                                        caption = getText("page.settings.store.orders"),
                                        leading = Icon(code = Theme.ICON_ORDERS, color = Theme.COLOR_PRIMARY),
                                        trailing = Icon(code = Theme.ICON_CHEVRON_RIGHT),
                                        action = gotoUrl(
                                            urlBuilder.build("orders?merchant=true")
                                        )
                                    )
                                else
                                    null,

                                if (togglesProvider.isShippingEnabled())
                                    ListItem(
                                        caption = getText("page.settings.store.shipping"),
                                        leading = Icon(code = Theme.ICON_SHIPPING, color = Theme.COLOR_PRIMARY),
                                        trailing = Icon(code = Theme.ICON_CHEVRON_RIGHT),
                                        action = gotoUrl(
                                            urlBuilder.build("settings/store/shipping")
                                        )
                                    )
                                else
                                    null,

                                if (togglesProvider.isToggleEnabled(ToggleName.STORE_STATISTICS))
                                    ListItem(
                                        caption = getText("page.settings.store.statistics"),
                                        leading = Icon(code = Theme.ICON_BAR_CHART, color = Theme.COLOR_PRIMARY),
                                        trailing = Icon(code = Theme.ICON_CHEVRON_RIGHT),
                                        action = gotoUrl(
                                            urlBuilder.build("settings/store/statistics")
                                        )
                                    )
                                else
                                    null,

                                Container(
                                    padding = 20.0
                                ),
                                ListItemSwitch(
                                    name = "value",
                                    caption = getText("page.settings.store.active"),
                                    selected = true,
                                    action = executeCommand(
                                        urlBuilder.build("/commands/disable-store")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).toWidget()
    }
}
