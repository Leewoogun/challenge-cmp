package com.lwg.challenge.camera

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.lwg.challenge.utils.Logger
import java.io.File
import java.io.IOException
import kotlinx.coroutines.launch

@Composable
actual fun rememberCameraCapture(
    spec: CompressionSpec,
    onResult: (CameraCaptureResult) -> Unit,
): CameraCapture {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // 재구성 뒤에도 최신 람다를 부르게 한다 — remember 에 갇힌 낡은 onResult 호출 방지
    val currentOnResult by rememberUpdatedState(onResult)
    val currentSpec by rememberUpdatedState(spec)
    // remember 면 안 된다: 시스템 카메라가 떠 있는 동안 회전 등으로 Activity 가 재생성되면
    // launcher 콜백은 ActivityResultRegistry 가 복원해 주지만 remember 값은 사라져,
    // 촬영 결과가 로그 한 줄 없이 조용히 버려진다
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingPath?.let(::File) ?: return@rememberLauncherForActivityResult
        pendingPath = null
        if (!success) {
            file.delete()
            currentOnResult(CameraCaptureResult.Canceled)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val photo = PhotoCompressor().compress(file.absolutePath, currentSpec)
            // 원본은 축소본보다 몇 배 크다 — 캐시에 남으면 리사이즈의 의미가 절반 사라지므로 결과와 무관하게 지운다
            file.delete()
            currentOnResult(
                if (photo != null) {
                    CameraCaptureResult.Success(photo)
                } else {
                    CameraCaptureResult.Failed(CameraCaptureFailure.DECODE_FAILED)
                },
            )
        }
    }

    return remember {
        object : CameraCapture {
            override fun capture() {
                val file = try {
                    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
                    File.createTempFile("capture_", ".jpg", dir)
                } catch (e: IOException) {
                    Logger.e("촬영용 임시 파일 생성 실패", e)
                    currentOnResult(CameraCaptureResult.Failed(CameraCaptureFailure.IO))
                    return
                }

                pendingPath = file.absolutePath
                try {
                    launcher.launch(
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file),
                    )
                } catch (e: ActivityNotFoundException) {
                    pendingPath = null
                    file.delete()
                    Logger.e("카메라 앱 없음", e)
                    currentOnResult(CameraCaptureResult.Failed(CameraCaptureFailure.NO_CAMERA))
                }
            }
        }
    }
}
