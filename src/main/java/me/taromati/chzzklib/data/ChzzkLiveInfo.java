package me.taromati.chzzklib.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChzzkLiveInfo {

    private final String channelId;
    private final String channelName;
    private final String channelDescription;

    private final String liveTitle;
    private final String liveType;
    private final List<String> categoryTags;

    private final int followerCount;
    private final int currentUserCount;

    private final boolean verifiedMark;
    private final boolean openLive;

}