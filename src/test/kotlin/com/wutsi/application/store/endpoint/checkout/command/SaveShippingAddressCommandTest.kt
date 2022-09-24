package com.wutsi.application.store.endpoint.checkout.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.application.store.endpoint.checkout.dto.SaveShippingAddressRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.CreateAddressRequest
import com.wutsi.ecommerce.order.dto.CreateAddressResponse
import com.wutsi.ecommerce.order.dto.SetAddressRequest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SaveShippingAddressCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var orderApi: WutsiOrderApi

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/commands/save-shipping-address?order-id=111&country=CM&type=POSTAL"
    }

    @Test
    fun select() {
        // GIVEN
        val addressId = 1111L
        doReturn(CreateAddressResponse(id = addressId)).whenever(orderApi).createAddress(any())

        // WHEN
        val request = SaveShippingAddressRequest(
            firstName = "Ray",
            lastName = "Sponsible",
            email = "ray.sponsible@gmail.com",
            street = "4304039 Foo",
            cityId = 111L
        )
        val response = rest.postForEntity(url, request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<CreateAddressRequest>()
        verify(orderApi).createAddress(req.capture())
        assertEquals(request.firstName, req.firstValue.firstName)
        assertEquals(request.lastName, req.firstValue.lastName)
        assertEquals(request.email, req.firstValue.email)
        assertEquals(request.street, req.firstValue.street)
        assertEquals(request.cityId, req.firstValue.cityId)
        assertEquals("CM", req.firstValue.country)
        assertEquals("POSTAL", req.firstValue.type)

        verify(orderApi).setShippingAddress("111", SetAddressRequest(addressId = addressId))

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/checkout/shipping?order-id=111", action.url)
    }
}
