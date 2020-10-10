package com.pujit.myapplication.request

import com.google.gson.annotations.SerializedName

data class PaymentRequest(

	@field:SerializedName("currency")
	val currency: String? = null,

	@field:SerializedName("source")
	val source: Source? = null
)

data class Source(

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("token")
	val token: String? = null
)
