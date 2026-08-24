package com.lwg.challenge.camera

/**
 * 업로드 준비가 끝난 인증 사진. 원본이 아니라 축소·재압축된 JPEG 다 —
 * 원본을 공통 계층으로 올리면 수 MB 가 그대로 힙에 뜬다.
 *
 * data class 가 아닌 이유: ByteArray 필드의 equals/hashCode 가 참조 비교라 함정이 된다.
 */
class CapturedPhoto(
    val jpegBytes: ByteArray,
    val width: Int,
    val height: Int,
) {

    val byteSize: Int get() = jpegBytes.size
}
