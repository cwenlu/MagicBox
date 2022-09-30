package com.cwl.data.dto

/**
 *
 * @param T
 * @property errorCode Int?
 * @property errorMsg String?
 * @property data T?
 * @constructor
 */
data class BaseDto<T>(
    val errorCode: Int?,
    val errorMsg: String?,
    val data: T?
)

data class ListAdapter<T>(
    val curPage: Int,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int,
    val datas: ArrayList<T>
)