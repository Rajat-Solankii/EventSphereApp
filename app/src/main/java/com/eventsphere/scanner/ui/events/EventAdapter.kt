package com.eventsphere.scanner.ui.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eventsphere.scanner.R
import com.eventsphere.scanner.data.api.models.Event
import com.eventsphere.scanner.databinding.ItemEventBinding

class EventAdapter(private val onEventClick: (Event) -> Unit) :
    ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.tvEventTitle.text = event.title
            binding.tvEventDate.text = event.date
            binding.tvEventVenue.text = event.venue
            
            val totalAvailable = event.tiers?.sumOf { it.available } ?: 0
            binding.tvAvailableSlots.text = binding.root.context.getString(R.string.available_slots, totalAvailable)

            if (!event.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.ivEventImage)
                    .load(event.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_calendar_month) // Placeholder
                    .into(binding.ivEventImage)
            } else {
                binding.ivEventImage.setImageResource(R.drawable.ic_calendar_month)
            }

            binding.root.setOnClickListener { onEventClick(event) }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
            oldItem == newItem
    }
}
