package com.wutsi.application.store.endpoint.product.screen

import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.model.AccountModel
import com.wutsi.application.shared.model.ProductModel
import com.wutsi.application.shared.service.PhoneUtil
import com.wutsi.application.shared.service.StringUtil
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.ProductActionProvider
import com.wutsi.application.shared.ui.ProductCardType
import com.wutsi.application.shared.ui.ProductGridView
import com.wutsi.application.shared.ui.ProfileListItem
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.ecommerce.catalog.dto.ProductSummary
import com.wutsi.ecommerce.catalog.dto.SearchProductRequest
import com.wutsi.ecommerce.catalog.entity.ProductSort
import com.wutsi.ecommerce.catalog.entity.ProductStatus
import com.wutsi.ecommerce.catalog.entity.ProductType
import com.wutsi.ecommerce.order.entity.AddressType
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.AspectRatio
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.CarouselSlider
import com.wutsi.flutter.sdui.Center
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.ExpandablePanel
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.Image
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.MoneyText
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisSize
import com.wutsi.flutter.sdui.enums.TextDecoration
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.tenant.dto.Tenant
import com.wutsi.platform.tenant.entity.ToggleName
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat
import java.util.UUID
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/product")
class ProductScreen(
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,
    private val tenantProvider: TenantProvider
) : ProductActionProvider, AbstractQuery() {
    override fun getAction(model: ProductModel): Action =
        gotoUrl(
            url = urlBuilder.build("/product?id=${model.id}")
        )

    override fun getAction(model: AccountModel): Action? =
        gotoUrl(
            url = urlBuilder.build(shellUrl, "/profile?id=${model.id}")
        )

    @PostMapping
    fun index(@RequestParam id: Long, request: HttpServletRequest): Widget {
        val product = catalogApi.getProduct(id).product
        val merchant = accountApi.getAccount(product.accountId).account
        val tenant = tenantProvider.get()
        val cart = getCart(merchant)

        val children = mutableListOf<WidgetAware>(
            Container(
                padding = 10.0,
                background = Theme.COLOR_WHITE,
                child = Text(
                    caption = StringUtil.capitalizeFirstLetter(product.title),
                    size = Theme.TEXT_SIZE_LARGE,
                    bold = true
                )
            )
        )

        // Pictures
        if (product.pictures.isNotEmpty())
            children.addAll(
                listOf(
                    toSectionWidget(
                        padding = null,
                        child = CarouselSlider(
                            viewportFraction = .9,
                            enableInfiniteScroll = false,
                            reverse = false,
                            height = 250.0,
                            children = product.pictures.map {
                                AspectRatio(
                                    aspectRatio = 8.0 / 10.0,
                                    child = Image(
                                        url = it.url,
                                        height = 300.0
                                    )
                                )
                            }
                        )
                    )
                )
            )

        children.add(
            toSectionWidget(
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    mainAxisSize = MainAxisSize.min,
                    children = listOfNotNull(
                        // Price
                        if (product.price != null)
                            toPriceWidget(product, tenant)
                        else
                            null,

                        // Summary
                        if (!product.summary.isNullOrEmpty())
                            Column(
                                children = listOf(
                                    Text(product.summary!!)
                                )
                            )
                        else
                            null,

                        // Availability
                        Container(padding = 10.0),
                        toAvailabilityWidget(product),

                        // Cart
                        toCartWidget(merchant, product, cart),
                        toBuyNowWidget(merchant, product)
                    )
                )
            )
        )

        // Product Details
        if (!product.description.isNullOrEmpty())
            children.add(
                Container(
                    padding = 10.0,
                    margin = 5.0,
                    border = 1.0,
                    borderColor = Theme.COLOR_GRAY_LIGHT,
                    background = Theme.COLOR_WHITE,
                    child = ExpandablePanel(
                        header = getText("page.product.product-details"),
                        expanded = Container(
                            padding = 10.0,
                            child = Text(product.description!!)
                        )
                    )
                )
            )

        val shareUrl = "${tenant.webappUrl}/product?id=$id"
        val whatsappUrl = PhoneUtil.toWhatsAppUrl(merchant.whatsapp, shareUrl)
        children.addAll(
            listOfNotNull(
                toVendorWidget(product, merchant, whatsappUrl),
                toSimilarProductsWidget(product, cart, tenant),
                toOtherProductsWidget(product, cart, tenant)
            )
        )

        try {
            // Screen
            return Screen(
                id = Page.PRODUCT,
                appBar = AppBar(
                    elevation = 0.0,
                    backgroundColor = Theme.COLOR_WHITE,
                    foregroundColor = Theme.COLOR_BLACK,
                    actions = titleBarActions(
                        productId = id,
                        merchantId = merchant.id,
                        shareUrl = shareUrl,
                        whatsappUrl = whatsappUrl,
                        cart = cart
                    )
                ),
                child = Container(
                    child = ListView(
                        children = children
                    )
                ),
                bottomNavigationBar = bottomNavigationBar(),
                backgroundColor = Theme.COLOR_GRAY_LIGHT
            ).toWidget()
        } finally {
            track(product, request)
        }
    }

    private fun track(product: Product, request: HttpServletRequest) {
        if (togglesProvider.isToggleEnabled(ToggleName.STORE_STATISTICS)) {
            track(
                correlationId = UUID.randomUUID().toString(),
                page = Page.PRODUCT,
                event = EventType.VIEW,
                productId = product.id,
                merchantId = product.accountId,
                value = null,
                request = request
            )
        }
    }

    private fun toCartWidget(merchant: Account, product: Product, cart: Cart?): WidgetAware? {
        /* Only physical product are added into the cart */
        if (!togglesProvider.isCartEnabled() || product.type != ProductType.PHYSICAL.name)
            return null

        val item = cart?.products?.find { it.productId == product.id }
        return if (item != null)
            Row(
                children = listOf(
                    Icon(
                        code = Theme.ICON_CART,
                        color = Theme.COLOR_PRIMARY,
                        size = 16.0
                    ),
                    Container(padding = 5.0),
                    Text(getText("page.product.in-cart", arrayOf(item.quantity.toString())))
                )
            )
        else if (product.quantity > 0)
            Button(
                padding = 10.0,
                caption = getText("page.product.button.add-to-cart"),
                action = executeCommand(
                    url = urlBuilder.build("commands/add-to-cart?product-id=${product.id}&merchant-id=${merchant.id}")
                )
            )
        else
            null
    }

    private fun toBuyNowWidget(merchant: Account, product: Product): WidgetAware? {
        val addressType = when (product.type.uppercase()) {
            ProductType.NUMERIC.name -> AddressType.EMAIL
            else -> return null
        }

        return Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = listOfNotNull<WidgetAware>(
                Button(
                    padding = 10.0,
                    caption = getText("page.product.button.buy-now"),
                    action = executeCommand(
                        url = urlBuilder.build("commands/buy-now?product-id=${product.id}&merchant-id=${merchant.id}&address-type=$addressType")
                    )
                ),
                Container(padding = 5.0),
                if (product.type == ProductType.NUMERIC.name)
                    Row(
                        children = listOf(
                            Icon(
                                code = Theme.ICON_EMAIL,
                                size = 16.0,
                                color = Theme.COLOR_PRIMARY
                            ),
                            Container(padding = 5.0),
                            Text(
                                caption = getText("page.product.deliver-by-email"),
                                color = Theme.COLOR_PRIMARY
                            )
                        )
                    )
                else
                    null
            )
        )
    }

    private fun toAvailabilityWidget(product: Product): WidgetAware =
        Row(
            children = listOf(
                Icon(
                    code = if (product.quantity > 0) Theme.ICON_CHECK else Theme.ICON_CANCEL,
                    color = if (product.quantity > 0) Theme.COLOR_SUCCESS else Theme.COLOR_DANGER,
                    size = 16.0
                ),
                Container(padding = 5.0),
                Text(
                    caption = if (product.quantity > 0)
                        getText("page.product.in-stock")
                    else
                        getText("page.product.out-of-stock")
                )
            )
        )

    private fun toPriceWidget(product: Product, tenant: Tenant): WidgetAware {
        val children = mutableListOf<WidgetAware>()
        val price = product.price!!
        val comparablePrice = product.comparablePrice ?: 0.0
        val savings = comparablePrice - price
        val percent = (100.0 * savings / comparablePrice).toInt()
        val fmt = DecimalFormat(tenant.monetaryFormat)

        // Price
        children.add(
            MoneyText(
                currency = tenant.currency,
                color = Theme.COLOR_PRIMARY,
                valueFontSize = Theme.TEXT_SIZE_X_LARGE,
                value = price,
                numberFormat = tenant.numberFormat
            )
        )

        if (savings > 0) {
            children.add(
                Text(
                    caption = fmt.format(comparablePrice),
                    decoration = TextDecoration.Strikethrough,
                    color = Theme.COLOR_GRAY
                )
            )
            if (percent >= 1)
                children.add(
                    Text(
                        caption = getText("page.product.savings-percent", arrayOf(percent.toString())),
                        color = Theme.COLOR_SUCCESS
                    )
                )
        }

        return Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = children
        )
    }

    private fun toVendorWidget(product: Product, merchant: Account, whatsappUrl: String?): WidgetAware =
        toSectionWidget(
            padding = null,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                mainAxisSize = MainAxisSize.min,
                children = listOfNotNull(
                    ProfileListItem(
                        model = sharedUIMapper.toAccountModel(merchant),
                        showAccountType = false,
                        showLocation = false,
                        action = gotoUrl(
                            urlBuilder.build(shellUrl, "/profile"),
                            parameters = mapOf(
                                "id" to merchant.id.toString()
                            )
                        )
                    ),
                    Divider(height = 1.0),
                    whatsappUrl?.let {
                        Center(
                            child = Button(
                                type = if (togglesProvider.isCartEnabled()) ButtonType.Outlined else ButtonType.Elevated,
                                stretched = false,
                                padding = 10.0,
                                caption = getText("page.product.write-to-merchant"),
                                action = Action(
                                    type = ActionType.Navigate,
                                    url = it,
                                    trackEvent = EventType.CHAT.name,
                                    trackProductId = product.id.toString()
                                )
                            )
                        )
                    }
                )
            )
        )

    private fun toSimilarProductsWidget(product: Product, cart: Cart?, tenant: Tenant): WidgetAware? {
        // Get products
        val cartProductIds = cart?.let { it.products.map { it.productId } } ?: emptyList()
        val products = catalogApi.searchProducts(
            request = SearchProductRequest(
                accountId = product.accountId,
                categoryIds = listOf(product.category.id, product.subCategory.id),
                status = ProductStatus.PUBLISHED.name,
                sortBy = ProductSort.RECOMMENDED.name,
                limit = 30
            )
        ).products.filter {
            it.id != product.id &&
                !cartProductIds.contains(it.id)
        }
        if (products.isEmpty())
            return null

        // Sort - ensure all products in the same sub-categories... and from other merchants
        val similar = mutableListOf<ProductSummary>()
        similar.addAll(products.filter { it.subCategoryId == product.subCategory.id })
        similar.addAll(products.filter { it.subCategoryId != product.subCategory.id })

        // Component
        return toSectionWidget(
            padding = null,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(getText("page.product.similar-products"), bold = true)
                    ),
                    ProductGridView(
                        spacing = 5.0,
                        productsPerRow = 2,
                        models = similar.take(4)
                            .map { sharedUIMapper.toProductModel(it, tenant, null) },
                        actionProvider = this,
                        type = ProductCardType.SUMMARY
                    )
                )
            )
        )
    }

    private fun toOtherProductsWidget(product: Product, cart: Cart?, tenant: Tenant): WidgetAware? {
        // Get products
        val cartProductIds = cart?.let { it.products.map { it.productId } } ?: emptyList()
        val categoryIds = listOf(product.category.id, product.subCategory.id)
        val products = catalogApi.searchProducts(
            request = SearchProductRequest(
                accountId = product.accountId,
                status = ProductStatus.PUBLISHED.name,
                sortBy = ProductSort.RECOMMENDED.name,
                limit = 30
            )
        ).products.filter {
            it.id != product.id &&
                !cartProductIds.contains(it.id) &&
                !categoryIds.contains(it.subCategoryId) &&
                !categoryIds.contains(it.categoryId)
        }
        if (products.isEmpty())
            return null

        // Component
        return toSectionWidget(
            padding = null,
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOfNotNull(
                    Container(
                        padding = 10.0,
                        child = Text(getText("page.product.merchant-products"), bold = true)
                    ),
                    ProductGridView(
                        spacing = 5.0,
                        productsPerRow = 2,
                        models = products.take(4)
                            .map { sharedUIMapper.toProductModel(it, tenant, null) },
                        actionProvider = this,
                        type = ProductCardType.SUMMARY
                    ),
                    if (products.size > 4)
                        Center(
                            child = Container(
                                padding = 10.0,
                                alignment = Alignment.Center,
                                child = Button(
                                    caption = getText("page.product.button.more-products"),
                                    padding = 10.0,
                                    type = ButtonType.Outlined,
                                    stretched = false,
                                    action = gotoUrl(
                                        url = urlBuilder.build(shellUrl, "/profile?id=${product.accountId}&tab=store")
                                    )
                                )
                            )
                        )
                    else
                        null
                )
            )
        )
    }
}
