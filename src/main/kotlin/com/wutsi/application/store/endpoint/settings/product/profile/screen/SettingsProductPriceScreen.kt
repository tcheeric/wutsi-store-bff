package com.wutsi.application.store.endpoint.settings.product.profile.screen

import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.InputType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/store/product/price")
class SettingsProductPriceScreen(
    catalogApi: WutsiCatalogApi,
    private val tenantProvider: TenantProvider
) : AbstractSettingsProductAttributeScreen(catalogApi) {
    override fun getAttributeName() = "price"

    override fun getPageId() = Page.SETTINGS_STORE_PRODUCT_PRICE

    override fun getInputWidget(product: Product): WidgetAware {
        val tenant = tenantProvider.get()
        return Input(
            name = "value",
            value = product.price?.toString() ?: "",
            type = InputType.Number,
            caption = getText("page.settings.store.product.attribute.${getAttributeName()}"),
            suffix = tenant.currencySymbol,
            inputFormatterRegex = if (tenant.monetaryFormat.indexOf(".") == -1)
                "[0-9]"
            else
                null
        )
    }

}
