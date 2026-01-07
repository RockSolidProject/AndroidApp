package com.example.pora_projekt.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.pora_projekt.databinding.FragmentCommentBinding

class CommentFragment : Fragment() {

    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageTypes = arrayOf("Nevarnost", "Ponovno postavljanje stene")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, messageTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMessageType.adapter = adapter

        binding.buttonSend.setOnClickListener {
            val selectedType = binding.spinnerMessageType.selectedItem.toString()
            val message = binding.editMessage.text.toString()

            if (message.isBlank()) {
                binding.textStatus.text = "Sporočilo ne sme biti prazno"
                return@setOnClickListener
            }

            binding.buttonSend.isEnabled = false
            binding.textStatus.text = "Pošiljam..."

            // TODO pošiljanje na strežnik
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}