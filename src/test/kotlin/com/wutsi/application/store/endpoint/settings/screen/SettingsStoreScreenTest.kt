package com.wutsi.application.store.endpoint.settings.screen

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.store.endpoint.AbstractEndpointTest
import com.wutsi.platform.tenant.entity.ToggleName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class SettingsStoreScreenTest : AbstractEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    private lateinit var url: String

    @BeforeEach
    override fun setUp() {
        super.setUp()

        url = "http://localhost:$port/settings/store"
    }

    @Test
    fun index() {
        assertEndpointEquals("/screens/settings/store/store.json", url)
    }

    @Test
    fun shippingEnabled() {
        doReturn(true).whenever(togglesProvider).isShippingEnabled()

        assertEndpointEquals("/screens/settings/store/store-shipping-enabled.json", url)
    }

    @Test
    fun statisticsEnabled() {
        doReturn(true).whenever(togglesProvider).isToggleEnabled(ToggleName.STORE_STATISTICS)

        assertEndpointEquals("/screens/settings/store/store-statistics-enabled.json", url)
    }

    @Test
    fun orderEnambled() {
        doReturn(true).whenever(togglesProvider).isToggleEnabled(ToggleName.ORDER)

        assertEndpointEquals("/screens/settings/store/store-order-enabled.json", url)
    }
}
