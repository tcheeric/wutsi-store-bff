package com.wutsi.application.store.endpoint.settings.product.profile.command

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class UpdateProductAttributeCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private val productId = 777L

    @Test
    fun title() {
        // WHEN
        val request = com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest(
            value = "this is the title"
        )
        val response = rest.postForEntity(url("title"), request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<com.wutsi.ecommerce.catalog.dto.UpdateProductAttributeRequest>()
        verify(catalogApi).updateProductAttribute(eq(productId), eq("title"), req.capture())
        assertEquals(request.value, req.firstValue.value)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/..", action.url)
    }

    @Test
    fun visible() {
        // WHEN
        val request = com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest(
            value = "false"
        )
        val response = rest.postForEntity(url("visible"), request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val req = argumentCaptor<com.wutsi.ecommerce.catalog.dto.UpdateProductAttributeRequest>()
        verify(catalogApi).updateProductAttribute(eq(productId), eq("visible"), req.capture())
        assertEquals(request.value, req.firstValue.value)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("http://localhost:0/settings/store/product?id=$productId", action.url)
    }

    @Test
    fun negativePrice() {
        // WHEN
        val request = com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest(
            value = "-1"
        )
        val response = rest.postForEntity(url("price"), request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(getText("error.product.negative-price"), action.prompt?.attributes?.get("message"))
    }

    @Test
    fun negativeComparablePrice() {
        // WHEN
        val request = com.wutsi.application.store.endpoint.settings.product.profile.dto.UpdateProductAttributeRequest(
            value = "-1"
        )
        val response = rest.postForEntity(url("comparable-price"), request, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(getText("error.product.negative-price"), action.prompt?.attributes?.get("message"))
    }

    private fun url(attribute: String): String =
        "http://localhost:$port/commands/update-product-attribute?id=$productId&name=$attribute"
}
