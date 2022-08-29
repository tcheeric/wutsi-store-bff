package com.wutsi.application.store.endpoint.checkout.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.service.IdempotencyKeyGenerator
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.DropdownButton
import com.wutsi.flutter.sdui.DropdownMenuItem
import com.wutsi.flutter.sdui.Form
import com.wutsi.flutter.sdui.Input
import com.wutsi.flutter.sdui.MoneyText
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.InputType
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.PaymentMethodSummary
import com.wutsi.platform.payment.Capability
import com.wutsi.platform.payment.PaymentMethodProvider
import com.wutsi.platform.payment.PaymentMethodType
import com.wutsi.platform.tenant.dto.FinancialInstitution
import com.wutsi.platform.tenant.dto.MobileCarrier
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat

@RestController
@RequestMapping("/checkout/payment")
class CheckoutPaymentScreen(
    private val orderApi: WutsiOrderApi,
    private val accountApi: WutsiAccountApi,
    private val tenantProvider: TenantProvider,
    private val idempotencyKeyGenerator: IdempotencyKeyGenerator
) : AbstractQuery() {
    @PostMapping
    fun index(@RequestParam(name = "order-id") orderId: String): Widget {
        val tenant = tenantProvider.get()
        val order = orderApi.getOrder(orderId).order
        val merchant = accountApi.getAccount(order.merchantId).account
        val amountText = DecimalFormat(tenant.monetaryFormat).format(order.totalPrice)
        val idempotencyKey = idempotencyKeyGenerator.generate()
        val paymentMethods = accountApi.listPaymentMethods(securityContext.currentAccountId()).paymentMethods
            .filter { supportsPayment(it) }

        // Result
        return Screen(
            id = Page.CHECKOUT_PAYMENT,
            backgroundColor = Theme.COLOR_GRAY_LIGHT,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.checkout.payment.app-bar.title")
            ),
            child = SingleChildScrollView(
                child = Form(
                    children = listOf(
                        toSectionWidget(
                            padding = null,
                            child = ProfileListItem(
                                model = sharedUIMapper.toAccountModel(merchant),
                                showAccountType = false
                            )
                        ),
                        toSectionWidget(
                            Column(
                                children = listOf(
                                    DropdownButton(
                                        value = null,
                                        name = "paymentToken",
                                        required = true,
                                        hint = getText("page.checkout.payment.choose-account.hint"),
                                        children = toPaymentMethodWidgetList(paymentMethods, tenant)
                                    ),
                                    Container(padding = 10.0),
                                    Center(
                                        child = MoneyText(
                                            value = order.totalPrice,
                                            currency = order.currency,
                                            numberFormat = tenant.numberFormat,
                                            color = Theme.COLOR_PRIMARY
                                        )
                                    ),
                                    Container(padding = 10.0),
                                    Input(
                                        type = InputType.Submit,
                                        name = "submit",
                                        caption = getText("page.checkout.payment.button.submit", arrayOf(amountText)),
                                        action = executeCommand(
                                            urlBuilder.build("commands/authorize-order-payment?order-id=$orderId&idempotency-key=$idempotencyKey")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ).toWidget()
    }

    private fun supportsPayment(paymentMethod: PaymentMethodSummary): Boolean {
        val provider = PaymentMethodProvider.valueOf(paymentMethod.provider.uppercase())
        return provider.capabilities.contains(Capability.PAYMENT)
    }

    private fun toPaymentMethodWidgetList(
        paymentMethods: List<PaymentMethodSummary>,
        tenant: Tenant
    ): List<DropdownMenuItem> {
        val items = mutableListOf<DropdownMenuItem>()
        items.add(
            DropdownMenuItem(
                caption = getText("page.checkout.payment.dropdown-item-wallet"),
                value = "WALLET",
                icon = tenantProvider.logo(tenant)
            )
        )
        items.addAll(
            paymentMethods.map {
                DropdownMenuItem(
                    caption = it.maskedNumber,
                    value = it.token,
                    icon = getLogoUrl(tenant, it)
                )
            }
        )
        return items
    }

    private fun getLogoUrl(tenant: Tenant, paymentMethod: PaymentMethodSummary): String? {
        if (paymentMethod.type == PaymentMethodType.MOBILE.name) {
            val carrier = getMobileCarrier(paymentMethod.provider, tenant)
            if (carrier != null) {
                return tenantProvider.logo(carrier)
            }
        } else if (paymentMethod.type == PaymentMethodType.BANK.name) {
            val financialInstitution = getFinantialInstitution(paymentMethod.provider, tenant)
            if (financialInstitution != null) {
                return tenantProvider.logo(financialInstitution)
            }
        }
        return null
    }

    private fun getMobileCarrier(provider: String, tenant: Tenant): MobileCarrier? =
        tenant.mobileCarriers.findLast { it.code.equals(provider, true) }

    private fun getFinantialInstitution(provider: String, tenant: Tenant): FinancialInstitution? =
        tenant.financialInstitutions.findLast { it.code.equals(provider, true) }
}
