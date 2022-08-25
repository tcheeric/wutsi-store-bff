package com.wutsi.application.store.endpoint.exception

class NegativePriceException(private val property: String, private val value: String) : Exception("$property=$value")
