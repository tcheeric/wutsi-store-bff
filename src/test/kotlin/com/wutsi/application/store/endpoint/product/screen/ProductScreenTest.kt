package com.wutsi.application.store.endpoint.product.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.analytics.tracking.WutsiTrackingApi
import com.wutsi.analytics.tracking.dto.PushTrackRequest
import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.shared.service.TogglesProvider
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.Page
import com.wutsi.application.store.endpoint.TrackingHttpRequestInterceptor
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.cart.dto.Cart
import com.wutsi.ecommerce.cart.dto.GetCartResponse
import com.wutsi.ecommerce.cart.dto.Product
import com.wutsi.ecommerce.catalog.dto.GetProductResponse
import com.wutsi.ecommerce.shipping.WutsiShippingApi
import com.wutsi.ecommerce.shipping.dto.SearchRateResponse
import com.wutsi.ecommerce.shipping.entity.ShippingType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ProductScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var togglesProvider: TogglesProvider

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    @MockBean
    private lateinit var trackingApi: WutsiTrackingApi

    @MockBean
    private lateinit var shippingApi: WutsiShippingApi

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/product?id=11"

        rest.interceptors.add(
            TrackingHttpRequestInterceptor(
                userAgent = "Android",
                referer = "https://www.google.com",
                ip = "10.0.2.2"
            )
        )

        val rates = listOf(
            createRateSummary(ShippingType.LOCAL_PICKUP),
            createRateSummary(ShippingType.LOCAL_DELIVERY, rate = 1000.0),
            createRateSummary(ShippingType.INTERNATIONAL_SHIPPING, rate = 2000.0)
        )
        doReturn(SearchRateResponse(rates)).whenever(shippingApi).searchRate(any())
    }

    @Test
    fun productWithImage() {
        val product = createProduct(true)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/product/product-with-image.json", url)
        assertTrackPushed(product)
    }

    @Test
    fun productWithoutImage() {
        val product = createProduct(false)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/product/product-without-image.json", url)
        assertTrackPushed(product)
    }

    @Test
    fun productWithCartEnabled() {
        doReturn(true).whenever(togglesProvider).isCartEnabled()

        val cart = Cart(
            products = listOf(
                Product(productId = 1L, quantity = 1),
                Product(productId = 2L, quantity = 3)
            )
        )
        doReturn(GetCartResponse(cart)).whenever(cartApi).getCart(any())

        val product = createProduct(true)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/product/product-with-cart-enabled.json", url)
        assertTrackPushed(product)
    }

    @Test
    fun productNoShipping() {
        doReturn(SearchRateResponse()).whenever(shippingApi).searchRate(any())

        val product = createProduct(true)
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        assertEndpointEquals("/screens/product/product-no-shipping.json", url)
        assertTrackPushed(product)
    }

    private fun assertTrackPushed(product: com.wutsi.ecommerce.catalog.dto.Product) {
        val request = argumentCaptor<PushTrackRequest>()
        verify(trackingApi).push(request.capture())

        val track = request.firstValue.track
        assertEquals(ACCOUNT_ID.toString(), track.accountId)
        assertEquals(product.accountId.toString(), track.merchantId)
        assertEquals(TENANT_ID, track.tenantId)
        assertEquals(DEVICE_ID, track.deviceId)
        assertNotNull(track.correlationId)
        assertEquals(product.id.toString(), track.productId)
        assertEquals(Page.PRODUCT, track.page)
        assertEquals(EventType.VIEW.name, track.event)
        assertEquals("https://www.google.com", track.referer)
        assertEquals("Android", track.ua)
        assertEquals("10.0.2.2", track.ip)
        assertFalse(track.bot)
        assertNull(track.url)
        assertNull(track.impressions)
        assertNull(track.lat)
        assertNull(track.long)
        assertNull(track.url)
        assertNull(track.value)
    }
}
