package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.order.dto.FilterOrderRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.OrderSummary
import com.wutsi.ecommerce.order.dto.SearchOrderRequest
import com.wutsi.platform.account.WutsiAccountApi
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/purchases")
class PurchasesScreen(
    accountApi: WutsiAccountApi,
    tenantProvider: TenantProvider,
    sharedUIMapper: SharedUIMapper,
    private val orderApi: WutsiOrderApi,
) : AbstractOrderListScreen(accountApi, tenantProvider, sharedUIMapper) {
    override fun getPageId() = Page.PURCHASES

    override fun getFilterUrl() = urlBuilder.build("/purchases")

    override fun getTitle() = getText("page.purchases.app-bar.title")

    override fun getOrders(request: FilterOrderRequest?) = orderApi.searchOrders(
        request = SearchOrderRequest(
            accountId = securityContext.currentAccountId(),
            limit = 100
        )
    ).orders

    override fun getAction(order: OrderSummary) = gotoUrl(
        url = urlBuilder.build("/order?id=${order.id}&hide-customer=true")
    )

    override fun getAccountId(order: OrderSummary) = order.merchantId

    override fun showFilter() = false
}