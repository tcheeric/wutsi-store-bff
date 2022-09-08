package com.wutsi.application.store.endpoint.order.commands

import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.application.store.endpoint.order.dto.ChangeOrderStatusRequest
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.ChangeStatusRequest
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.Action
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/cancel-order")
class CancelOrderCommand(
    private val orderApi: WutsiOrderApi
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam id: String,
        @RequestParam(name = "return-home", required = false) returnHome: Boolean? = null,
        @RequestBody(required = false) request: ChangeOrderStatusRequest? = null
    ): Action {
        orderApi.changeStatus(
            id,
            ChangeStatusRequest(
                status = OrderStatus.CANCELLED.name,
                reason = request?.reason,
                comment = request?.comment
            )
        )
        return if (returnHome == true)
            gotoHomeScreen()
        else
            gotoPreviousScreen()
    }
}
