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
import java.text.DecimalFormat

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
        val hasNoDecimal = tenant.monetaryFormat.indexOf(".") == -1
        return Input(
            name = "value",
            type = InputType.Number,
            caption = getText("page.settings.store.product.attribute.${getAttributeName()}"),
            suffix = tenant.currencySymbol,

            value = if (hasNoDecimal)
                product.price?.let { DecimalFormat("#0").format(it) } ?: ""
            else
                product.price?.toString() ?: "",

            inputFormatterRegex = if (hasNoDecimal)
                "[0-9]"
            else
                null
        )
    }
}
