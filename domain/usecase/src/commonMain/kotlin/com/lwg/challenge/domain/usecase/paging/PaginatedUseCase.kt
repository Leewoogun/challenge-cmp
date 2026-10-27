package com.lwg.challenge.domain.usecase.paging

abstract class PaginatedUseCase<K : Any, T>(
    protected val pagingSource: PagingSource<K, T> = PagingSource(),
) : Pageable by pagingSource
