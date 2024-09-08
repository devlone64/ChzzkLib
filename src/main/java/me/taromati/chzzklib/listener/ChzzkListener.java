package me.taromati.chzzklib.listener;

import me.taromati.chzzklib.event.implement.DonationChatEvent;
import me.taromati.chzzklib.event.implement.MessageChatEvent;
import me.taromati.chzzklib.event.implement.SubscriptionChatEvent;

public interface ChzzkListener {

    default void onMessageChat(final MessageChatEvent e) { }
    default void onDonationChat(final DonationChatEvent e) { }
    default void onSubscriptionChat(final SubscriptionChatEvent e) { }

}