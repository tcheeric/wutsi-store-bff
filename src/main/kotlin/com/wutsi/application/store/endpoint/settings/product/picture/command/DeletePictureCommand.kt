package com.wutsi.application.store.endpoint.settings.product.picture.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.application.store.endpoint.AbstractCommand
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.error.ErrorURN
import com.wutsi.flutter.sdui.Action
import com.wutsi.platform.core.error.ErrorResponse
import feign.FeignException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/commands/delete-picture")
class DeletePictureCommand(
    private val catalogApi: WutsiCatalogApi,
    private val objectMapper: ObjectMapper
) : AbstractCommand() {
    @PostMapping
    fun index(
        @RequestParam("picture-id") pictureId: Long
    ): Action {
        try {
            catalogApi.deletePicture(pictureId)
            return gotoPreviousScreen()
        } catch (ex: FeignException) {
            val response = objectMapper.readValue(ex.contentUTF8(), ErrorResponse::class.java)
            return if (response.error.code == ErrorURN.PUBLISHED_PRODUCT_SHOULD_HAVE_IMAGE.urn) {
                showError(getText("error.product.PUBLISHED_PRODUCT_SHOULD_HAVE_IMAGE"))
            } else {
                showError(getText("error.unexpected"))
            }
        }
    }
}
