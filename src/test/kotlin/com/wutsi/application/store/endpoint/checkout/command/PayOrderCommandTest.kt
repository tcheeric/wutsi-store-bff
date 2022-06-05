package com.wutsi.application.store.endpoint.checkout.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.cart.WutsiCartApi
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.ChangeStatusRequest
import com.wutsi.ecommerce.order.dto.GetOrderResponse
import com.wutsi.ecommerce.order.dto.Order
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.platform.payment.WutsiPaymentApi
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Status
import com.wutsi.platform.payment.dto.CreateChargeRequest
import com.wutsi.platform.payment.dto.CreateChargeResponse
import com.wutsi.platform.payment.dto.GetTransactionResponse
import com.wutsi.platform.payment.dto.Transaction
import com.wutsi.platform.payment.error.ErrorURN
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import java.net.URLEncoder
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class PayOrderCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    @MockBean
    private lateinit var paymentApi: WutsiPaymentApi

    @MockBean
    private lateinit var cartApi: WutsiCartApi

    private lateinit var url: String

    private val order = Order(
        id = "111",
        merchantId = 55L,
        totalPrice = 1000.0,
        currency = "XAF",
        status = OrderStatus.CREATED.name,
        reservationId = 777L,
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/pay-order?order-id=111&payment-token=xxx"
    }

    @Test
    fun success() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        doReturn(CreateChargeResponse("3039-f9009", Status.SUCCESSFUL.name)).whenever(paymentApi).createCharge(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val request = argumentCaptor<CreateChargeRequest>()
        verify(paymentApi).createCharge(request.capture())
        assertEquals(order.id, request.firstValue.orderId)
        assertEquals(order.currency, request.firstValue.currency)
        assertEquals(order.merchantId, request.firstValue.recipientId)
        assertEquals(order.totalPrice, request.firstValue.amount)
        assertEquals("xxx", request.firstValue.paymentMethodToken)
        assertNull(request.firstValue.description)

        verify(paymentApi, never()).getTransaction(any())
        verify(cartApi).emptyCart(order.merchantId)
        verify(orderApi).changeStatus("111", ChangeStatusRequest(status = OrderStatus.OPENED.name))

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}", action.url)
    }

    @Test
    fun pending() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val txId = "3039-f9009"
        doReturn(CreateChargeResponse(txId, Status.PENDING.name)).whenever(paymentApi).createCharge(any())

        val tx = Transaction(status = Status.PENDING.name)
        doReturn(GetTransactionResponse(tx)).whenever(paymentApi).getTransaction(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        Thread.sleep(1000L * PayOrderCommand.DELAY_SECONDS * PayOrderCommand.MAX_RETRIES)

        assertEquals(200, response.statusCodeValue)

        verify(paymentApi).createCharge(any())
        verify(paymentApi, times(PayOrderCommand.MAX_RETRIES)).getTransaction(txId)
        verify(cartApi, never()).emptyCart(order.merchantId)
        verify(orderApi, never()).changeStatus("111", ChangeStatusRequest(status = OrderStatus.OPENED.name))

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}", action.url)
    }

    @Test
    fun pendingThenSuccess() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val txId = "3039-f9009"
        doReturn(CreateChargeResponse(txId, Status.PENDING.name)).whenever(paymentApi).createCharge(any())

        val tx = Transaction(status = Status.SUCCESSFUL.name)
        doReturn(GetTransactionResponse(tx)).whenever(paymentApi).getTransaction(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        Thread.sleep(1000L * PayOrderCommand.DELAY_SECONDS)

        assertEquals(200, response.statusCodeValue)

        verify(paymentApi).createCharge(any())
        verify(paymentApi).getTransaction(txId)
        verify(cartApi).emptyCart(order.merchantId)
        verify(orderApi).changeStatus("111", ChangeStatusRequest(status = OrderStatus.OPENED.name))

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}", action.url)
    }

    @Test
    fun pendingThenError() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val txId = "3039-f9009"
        doReturn(CreateChargeResponse(txId, Status.PENDING.name)).whenever(paymentApi).createCharge(any())

        val tx = Transaction(status = Status.FAILED.name, errorCode = ErrorCode.NOT_ENOUGH_FUNDS.name)
        doReturn(GetTransactionResponse(tx)).whenever(paymentApi).getTransaction(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        Thread.sleep(1000L * PayOrderCommand.DELAY_SECONDS)

        assertEquals(200, response.statusCodeValue)

        verify(paymentApi).createCharge(any())
        verify(paymentApi).getTransaction(txId)
        verify(cartApi, never()).emptyCart(any())
        verify(orderApi, never()).changeStatus(any(), any())

        val message = URLEncoder.encode(getText("error.payment.NOT_ENOUGH_FUNDS"), "utf-8")
        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}&error=$message", action.url)
    }

    @Test
    fun notEnoughFunds() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val ex = createFeignException(ErrorURN.TRANSACTION_FAILED.urn, downstreamError = ErrorCode.NOT_ENOUGH_FUNDS)
        doThrow(ex).whenever(paymentApi).createCharge(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(paymentApi, never()).getTransaction(any())
        verify(cartApi, never()).emptyCart(any())
        verify(orderApi, never()).changeStatus(any(), any())

        val message = URLEncoder.encode(getText("error.payment.NOT_ENOUGH_FUNDS"), "utf-8")
        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}&error=$message", action.url)
    }

    @Test
    fun unexpectedError() {
        // GIVEN
        doReturn(GetOrderResponse(order)).whenever(orderApi).getOrder(any())

        val ex = createFeignException(ErrorURN.TRANSACTION_FAILED.urn, downstreamError = ErrorCode.UNEXPECTED_ERROR)
        doThrow(ex).whenever(paymentApi).createCharge(any())

        // WHEN
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(paymentApi, never()).getTransaction(any())
        verify(cartApi, never()).emptyCart(any())
        verify(orderApi, never()).changeStatus(any(), any())

        val message = URLEncoder.encode(getText("error.payment"), "utf-8")
        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/success?order-id=${order.id}&error=$message", action.url)
    }
}
