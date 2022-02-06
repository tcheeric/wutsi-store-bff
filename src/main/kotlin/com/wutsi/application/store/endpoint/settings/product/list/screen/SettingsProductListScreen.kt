package com.wutsi.application.store.endpoint.settings.product.list.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.SecurityContext
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.service.URLBuilder
import com.wutsi.application.shared.ui.ProductListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.settings.product.list.dto.FilterProductRequest
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.dto.SearchCategoryRequest
import com.wutsi.platform.catalog.dto.SearchProductRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/products")
class SettingsProductListScreen(
    private val urlBuilder: URLBuilder,
    private val catalogApi: WutsiCatalogApi,
    private val securityContext: SecurityContext,
    private val sharedUIMapper: SharedUIMapper,
    private val tenantProvider: TenantProvider,
) : AbstractQuery() {
    companion object {
        const val DEFAULT_CATEGORY_ID = -1L
    }

    @PostMapping
    fun index(@RequestBody request: FilterProductRequest?): Widget {
        val tenant = tenantProvider.get()
        val accountId = securityContext.currentAccountId()
        val products = catalogApi.searchProducts(
            request = SearchProductRequest(
                accountId = accountId,
                categoryIds = if (request?.categoryId == DEFAULT_CATEGORY_ID || request?.categoryId == null)
                    emptyList()
                else
                    listOf(request.categoryId),
                limit = 100
            )
        ).products

        return Screen(
            id = Page.SETTINGS_STORE_PRODUCT_LIST,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.store.product.list.app-bar.title"),
            ),
            floatingActionButton = Button(
                type = ButtonType.Floatable,
                icon = Theme.ICON_ADD,
                stretched = false,
                iconColor = Theme.COLOR_WHITE,
                action = gotoUrl(
                    url = urlBuilder.build("settings/store/product/add")
                ),
            ),
            child = Column(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = DropdownButton(
                            name = "categoryId",
                            value = request?.categoryId?.toString() ?: DEFAULT_CATEGORY_ID.toString(),
                            children = categoriesListItems(accountId),
                            action = gotoUrl(
                                url = urlBuilder.build("settings/store/products"),
                                replacement = true
                            )
                        )
                    ),
                    Text(
                        caption = getText("page.settings.store.product.list.count", arrayOf(products.size)),
                        alignment = TextAlignment.Center
                    ),
                    Divider(color = Theme.COLOR_DIVIDER),
                    Flexible(
                        child = ListView(
                            separator = true,
                            separatorColor = Theme.COLOR_DIVIDER,
                            children = products.map {
                                ProductListItem(
                                    model = sharedUIMapper.toProductModel(it, tenant, messages),
                                    action = gotoUrl(
                                        url = urlBuilder.build("/settings/store/product?id=${it.id}")
                                    )
                                )
                            }
                        )
                    )
                )
            ),
        ).toWidget()
    }

    private fun categoriesListItems(accountId: Long): List<DropdownMenuItem> {
        val result = mutableListOf<DropdownMenuItem>()
        val categories = catalogApi.searchCategories(
            request = SearchCategoryRequest(
                accountId = accountId,
            )
        ).categories.sortedBy { it.title }
        result.add(
            DropdownMenuItem(
                caption = getText("page.settings.store.product.list.all-products"),
                value = DEFAULT_CATEGORY_ID.toString()
            )
        )
        result.addAll(
            categories.map {
                DropdownMenuItem(
                    caption = it.title,
                    value = it.id.toString()
                )
            }
        )
        return result
    }
}