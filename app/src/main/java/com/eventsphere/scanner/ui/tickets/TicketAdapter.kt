package com.eventsphere.scanner.ui.tickets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eventsphere.scanner.R
import com.eventsphere.scanner.data.api.models.Ticket
import com.eventsphere.scanner.databinding.ItemTicketBinding

class TicketAdapter(private val onTicketClick: (Ticket) -> Unit) : ListAdapter<Ticket, TicketAdapter.TicketViewHolder>(TicketDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding, onTicketClick)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TicketViewHolder(
        private val binding: ItemTicketBinding,
        private val onTicketClick: (Ticket) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(ticket: Ticket) {
            binding.tvAttendeeName.text = ticket.attendeeName ?: "Unknown Attendee"
            binding.tvAttendeeEmail.text = ticket.attendeeEmail ?: "No email"
            binding.tvTierName.text = ticket.tier?.let { "Tier: $it" } ?: "Tier: GA"
            binding.tvStatus.text = ticket.status

            val colorRes = when (ticket.status.uppercase()) {
                "INSIDE" -> R.color.success
                "OUTSIDE" -> R.color.warning
                else -> R.color.text_secondary
            }
            binding.tvStatus.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, colorRes)

            binding.root.setOnClickListener {
                onTicketClick(ticket)
            }
        }
    }

    class TicketDiffCallback : DiffUtil.ItemCallback<Ticket>() {
        override fun areItemsTheSame(oldItem: Ticket, newItem: Ticket): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Ticket, newItem: Ticket): Boolean = oldItem == newItem
    }
}
