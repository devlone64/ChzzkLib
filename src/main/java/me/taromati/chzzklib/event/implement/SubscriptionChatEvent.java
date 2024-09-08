package me.taromati.chzzklib.event.implement;

import me.taromati.chzzklib.event.ChzzkEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionChatEvent implements ChzzkEvent {

    private final String channelId;

    private final String nickname;

    private final int month;

}