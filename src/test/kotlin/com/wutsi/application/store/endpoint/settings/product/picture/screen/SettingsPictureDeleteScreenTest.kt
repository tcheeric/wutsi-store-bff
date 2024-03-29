package com.wutsi.application.store.endpoint.settings.product.picture.screen

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.ecommerce.catalog.dto.GetProductResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsPictureDeleteScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @Test
    fun index() {
        val product = createProduct()
        doReturn(GetProductResponse(product)).whenever(catalogApi).getProduct(any())

        val url =
            "http://localhost:$port/settings/store/picture/delete?product-id=777&picture-id=${product.thumbnail?.id}"
        assertEndpointEquals("/screens/settings/picture/delete.json", url)
    }
}
