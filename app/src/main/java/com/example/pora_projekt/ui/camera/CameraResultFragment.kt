package com.example.pora_projekt.ui.camera

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pora_projekt.databinding.FragmentCameraResultBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class CameraResultFragment : Fragment() {

    private var _binding: FragmentCameraResultBinding? = null
    private val binding get() = _binding!!
    private var resultBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraResultBinding.inflate(inflater, container, false)

        resultBitmap = arguments?.getParcelable("resultBitmap")
        resultBitmap?.let {
            binding.resultImageView.setImageBitmap(it)
        }

        binding.downloadButton.setOnClickListener {
            saveImageToGallery()
        }
        binding.backToCameraButton.setOnClickListener {
            // previous page
            findNavController().navigateUp()
        }
        return binding.root
    }

    private fun saveImageToGallery() {
        val bitmap = resultBitmap ?: run {
            Toast.makeText(requireContext(), "No image to save", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val filename = "holds_${System.currentTimeMillis()}.jpg"
            var fos: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = requireContext().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(requireContext(), "Image saved to gallery", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
