package com.eventsphere.scanner.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.eventsphere.scanner.R
import com.eventsphere.scanner.databinding.FragmentScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerFragment : Fragment(R.layout.fragment_scanner) {
    private val args: ScannerFragmentArgs by navArgs()
    private val viewModel: ScannerViewModel by viewModels()
    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private var lastScanTime = 0L
    private val scanDebounce = 2000L

    private var imageCapture: ImageCapture? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScannerBinding.bind(view)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        
        binding.btnCancelCapture.setOnClickListener {
            resetToScanMode()
        }
        
        binding.btnCapturePhoto.setOnClickListener {
            takeInAppPicture()
        }

        observeViewModel()
    }
    
    private fun resetToScanMode() {
        viewModel.pendingExitTicketId = null
        binding.captureControls.visibility = View.GONE
        binding.overlay.visibility = View.VISIBLE
        binding.statusText.text = "Align QR Code inside the box"
        viewModel.resetScan()
    }

    private fun observeViewModel() {
        viewModel.scanResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { scanResult ->
                vibrate()
                showResult(scanResult)
            }.onFailure { error ->
                Toast.makeText(context, "Scan failed: ${error.message}", Toast.LENGTH_LONG).show()
                viewModel.resetScan()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }
                
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime < scanDebounce || viewModel.isScanning.value == true || viewModel.pendingExitTicketId != null) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { uuid ->
                            lastScanTime = currentTime
                            viewModel.scan(uuid, args.eventId)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Barcode scanning failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
    
    private fun takeInAppPicture() {
        val imageCapture = imageCapture ?: return
        val ticketId = viewModel.pendingExitTicketId ?: return
        
        binding.btnCapturePhoto.isEnabled = false
        binding.btnCapturePhoto.text = "Capturing..."

        val imagePath = File(requireContext().cacheDir, "images")
        imagePath.mkdirs()
        val photoFile = File(imagePath, "temp_exit.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Failed to capture photo", Toast.LENGTH_SHORT).show()
                    binding.btnCapturePhoto.isEnabled = true
                    binding.btnCapturePhoto.text = "Take Photo"
                    resetToScanMode()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(photoFile.absolutePath, options)
                    options.inSampleSize = calculateInSampleSize(options, 800, 800)
                    options.inJustDecodeBounds = false
                    
                    val imageBitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)
                    if (imageBitmap != null) {
                        val baos = ByteArrayOutputStream()
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                        val base64Image = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                        viewModel.markTemporaryExit(ticketId, base64Image)
                    } else {
                        Toast.makeText(requireContext(), "Failed to process photo", Toast.LENGTH_SHORT).show()
                    }
                    
                    binding.btnCapturePhoto.isEnabled = true
                    binding.btnCapturePhoto.text = "Take Photo"
                    resetToScanMode()
                }
            }
        )
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun showResult(scanResult: com.eventsphere.scanner.data.api.models.ScanResult) {
        ScanResultBottomSheet.newInstance(
            result = scanResult,
            isScanMode = true,
            onTempExit = { ticketId ->
                viewModel.pendingExitTicketId = ticketId
                binding.captureControls.visibility = View.VISIBLE
                binding.overlay.visibility = View.GONE
                binding.statusText.text = "Point camera at attendee and capture"
            },
            onReEnter = { ticketId ->
                viewModel.markReEntry(ticketId)
            },
            onDismiss = {
                if (viewModel.pendingExitTicketId == null) {
                    viewModel.resetScan()
                }
            }
        ).show(parentFragmentManager, ScanResultBottomSheet.TAG)
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

    companion object {
        private const val TAG = "ScannerFragment"
    }
}
