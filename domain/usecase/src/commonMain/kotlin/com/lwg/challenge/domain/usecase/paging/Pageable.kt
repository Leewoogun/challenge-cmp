package com.lwg.challenge.domain.usecase.paging

interface Pageable {
    fun nextPage()
    fun refresh()
}
