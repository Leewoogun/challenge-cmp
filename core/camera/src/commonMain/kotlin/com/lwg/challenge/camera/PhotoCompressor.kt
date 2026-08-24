package com.lwg.challenge.camera

/**
 * 촬영 원본을 [CompressionSpec] 에 맞춰 축소·재압축한다.
 *
 * 파일 경로를 받는 이유: 경로(String)는 플랫폼 공통 타입이라 경계를 그대로 넘고,
 * 각 플랫폼이 디코드 단계에서 바로 축소할 수 있어 원본 바이트를 메모리에 올리지 않아도 된다.
 */
interface PhotoCompressor {

    /** 실패 시 null — 파일 없음/디코드 불가. 호출부가 사용자에게 알린다. */
    suspend fun compress(filePath: String, spec: CompressionSpec): CapturedPhoto?
}

expect fun PhotoCompressor(): PhotoCompressor
