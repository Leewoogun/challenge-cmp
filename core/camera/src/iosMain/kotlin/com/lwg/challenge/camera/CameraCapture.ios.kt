@file:OptIn(ExperimentalForeignApi::class)

package com.lwg.challenge.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.lwg.challenge.utils.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun rememberCameraCapture(
    spec: CompressionSpec,
    onResult: (CameraCaptureResult) -> Unit,
): CameraCapture {
    val scope = rememberCoroutineScope()
    // 재구성 뒤에도 최신 람다를 부르게 한다 — remember 에 갇힌 낡은 onResult 호출 방지
    val currentOnResult by rememberUpdatedState(onResult)
    val currentSpec by rememberUpdatedState(spec)

    return remember {
        object : CameraCapture {
            // UIKit 은 picker.delegate 를 weak 로 잡는다 — 지역 변수로 두면 present 직후
            // 해제돼 콜백이 오지 않으므로, 촬영이 끝날 때까지 여기서 강한 참조로 붙든다
            private var delegate: CameraDelegate? = null

            override fun capture() {
                // 시뮬레이터에는 카메라가 없어 여기 걸린다
                if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
                    currentOnResult(CameraCaptureResult.Failed(CameraCaptureFailure.NO_CAMERA))
                    return
                }

                val host = UIApplication.sharedApplication.keyWindow?.rootViewController
                if (host == null) {
                    currentOnResult(CameraCaptureResult.Failed(CameraCaptureFailure.IO))
                    return
                }

                val picker = UIImagePickerController()
                picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                val newDelegate = CameraDelegate(
                    onPicked = { image ->
                        delegate = null
                        if (image == null) {
                            currentOnResult(CameraCaptureResult.Failed(CameraCaptureFailure.DECODE_FAILED))
                        } else {
                            scope.launch { currentOnResult(compressCaptured(image, currentSpec)) }
                        }
                    },
                    onCanceled = {
                        delegate = null
                        currentOnResult(CameraCaptureResult.Canceled)
                    },
                )
                delegate = newDelegate
                picker.delegate = newDelegate
                host.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

private class CameraDelegate(
    private val onPicked: (UIImage?) -> Unit,
    private val onCanceled: () -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onPicked(didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onCanceled()
    }
}

/**
 * 촬영된 UIImage 를 임시 파일로 떨군 뒤 같은 [PhotoCompressor] 를 태운다 —
 * Android 와 압축 경로를 동일하게 유지하기 위해서다. 원본 임시 파일은 결과와 무관하게 지운다.
 */
private suspend fun compressCaptured(image: UIImage, spec: CompressionSpec): CameraCaptureResult {
    val data = UIImageJPEGRepresentation(image, 0.95)
    if (data == null) {
        Logger.e("촬영 원본 JPEG 인코딩 실패")
        return CameraCaptureResult.Failed(CameraCaptureFailure.DECODE_FAILED)
    }

    val path = NSTemporaryDirectory() + NSUUID().UUIDString + ".jpg"
    // NSData.writeToFile 은 K/N Foundation 바인딩에 노출되지 않아 NSFileManager 로 쓴다
    val created = NSFileManager.defaultManager.createFileAtPath(path, contents = data, attributes = null)
    if (!created) {
        Logger.e("촬영 원본 임시 파일 쓰기 실패: $path")
        return CameraCaptureResult.Failed(CameraCaptureFailure.IO)
    }

    val photo = try {
        PhotoCompressor().compress(path, spec)
    } finally {
        NSFileManager.defaultManager.removeItemAtPath(path, error = null)
    }
    return if (photo != null) {
        CameraCaptureResult.Success(photo)
    } else {
        CameraCaptureResult.Failed(CameraCaptureFailure.DECODE_FAILED)
    }
}
