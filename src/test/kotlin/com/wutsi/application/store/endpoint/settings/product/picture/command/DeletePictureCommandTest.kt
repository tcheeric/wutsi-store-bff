package com.wutsi.application.store.endpoint.settings.product.picture.command

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.error.ErrorURN
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class DeletePictureCommandTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        // WHEN
        val url = "http://localhost:$port/commands/delete-picture?picture-id=111"
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(catalogApi).deletePicture(111)

        val action = response.body!!
        assertEquals(ActionType.Route, action.type)
        assertEquals("route:/..", action.url)
    }

    @Test
    fun `delete all image from published products`() {
        // GIVEN
        val ex = createFeignException(ErrorURN.PUBLISHED_PRODUCT_SHOULD_HAVE_IMAGE.urn)
        doThrow(ex).whenever(catalogApi).deletePicture(any())

        // WHEN
        val url = "http://localhost:$port/commands/delete-picture?picture-id=111"
        val response = rest.postForEntity(url, null, Action::class.java)

        // THEN
        assertEquals(200, response.statusCodeValue)

        verify(catalogApi).deletePicture(111)

        val action = response.body!!
        assertEquals(ActionType.Prompt, action.type)
        assertEquals(
            getText("error.product.PUBLISHED_PRODUCT_SHOULD_HAVE_IMAGE"),
            action.prompt?.attributes?.get("message")
        )
    }
}
