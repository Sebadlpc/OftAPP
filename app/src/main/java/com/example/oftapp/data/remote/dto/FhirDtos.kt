package com.example.oftapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Modelos de Transferencia de Datos (DTO) compatibles con el estándar HL7 FHIR R4.
 */
data class FhirBundleDto(
    @SerializedName("resourceType") val resourceType: String? = null,
    @SerializedName("total") val total: Int? = null,
    @SerializedName("entry") val entry: List<FhirEntryDto>? = null
)

data class FhirEntryDto(
    @SerializedName("fullUrl") val fullUrl: String? = null,
    @SerializedName("resource") val resource: FhirResourceDto? = null
)

data class FhirResourceDto(
    @SerializedName("resourceType") val resourceType: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("code") val code: FhirCodeableConceptDto? = null,
    @SerializedName("subject") val subject: FhirReferenceDto? = null,
    @SerializedName("effectiveDateTime") val effectiveDateTime: String? = null,
    @SerializedName("valueString") val valueString: String? = null,
    @SerializedName("valueQuantity") val valueQuantity: FhirQuantityDto? = null,
    @SerializedName("name") val name: List<FhirHumanNameDto>? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("birthDate") val birthDate: String? = null
)

data class FhirCodeableConceptDto(
    @SerializedName("text") val text: String? = null,
    @SerializedName("coding") val coding: List<FhirCodingDto>? = null
)

data class FhirCodingDto(
    @SerializedName("system") val system: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("display") val display: String? = null
)

data class FhirReferenceDto(
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("display") val display: String? = null
)

data class FhirQuantityDto(
    @SerializedName("value") val value: Double? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("system") val system: String? = null,
    @SerializedName("code") val code: String? = null
)

data class FhirHumanNameDto(
    @SerializedName("use") val use: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("family") val family: String? = null,
    @SerializedName("given") val given: List<String>? = null
)
