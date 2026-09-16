package com.eventsphere.scanner.ui.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eventsphere.scanner.R
import com.eventsphere.scanner.data.api.models.Event
import com.eventsphere.scanner.databinding.ItemEventBinding

import com.eventsphere.scanner.utils.DateUtils

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
            binding.tvEventDate.text = DateUtils.formatEventDate(event.date)
            binding.tvEventVenue.text = event.venue
            
            // Hidden unsold slots
            binding.tvAvailableSlots.visibility = View.GONE

            // Event Status logic based on entry times
            val eventTime = DateUtils.getEventTimeMillis(event.date)
            if (eventTime > 0L) {
                binding.tvEventStatusBadge.visibility = View.VISIBLE
                val currentTime = System.currentTimeMillis()
                val threeHoursInMillis = 3 * 60 * 60 * 1000L
                val eventDurationInMillis = 4 * 60 * 60 * 1000L
                
                when {
                    currentTime < eventTime - threeHoursInMillis -> {
                        binding.tvEventStatusBadge.text = "UPCOMING"
                        binding.tvEventStatusBadge.setBackgroundColor(binding.root.context.getColor(R.color.secondary))
                    }
                    currentTime > eventTime + eventDurationInMillis -> {
                        binding.tvEventStatusBadge.text = "ENDED"
                        binding.tvEventStatusBadge.setBackgroundColor(binding.root.context.getColor(R.color.error))
                    }
                    else -> {
                        binding.tvEventStatusBadge.text = "LIVE"
                        binding.tvEventStatusBadge.setBackgroundColor(binding.root.context.getColor(R.color.success))
                    }
                }
            } else {
                binding.tvEventStatusBadge.visibility = View.GONE
            }

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
