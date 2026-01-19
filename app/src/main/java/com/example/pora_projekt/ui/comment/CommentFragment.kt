package com.example.pora_projekt.ui.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.pora_projekt.databinding.FragmentCommentBinding
import com.example.pora_projekt.mqtt.MqttSender
import com.example.pora_projekt.service.LocationProvider

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

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val username = sharedPreferences.getString("username", "Unknown") ?: "Unknown"
            val timestamp = System.currentTimeMillis().toString()

            val currentLocation = LocationProvider(this.context!!).getLocation()

            val payload = buildString {
                append("{\"type\"=\"$selectedType\"")
                append(",\"message\"=\"$message\"")
                append(",\"username\"=\"$username\"")
                append(",\"timestamp\"=\"$timestamp\"")
                append(",\"latitude\"=${currentLocation.first}")
                append(",\"longitude\"=${currentLocation.second}")
                append("}")
            }

            MqttSender.publish("messages", payload)

            binding.textStatus.text = "Sporočilo poslano"
            binding.editMessage.setText("")
            binding.buttonSend.isEnabled = true

        // TODO pošiljanje na strežnik
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}