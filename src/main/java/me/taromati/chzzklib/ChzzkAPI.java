package me.taromati.chzzklib;

import me.taromati.chzzklib.data.ChzzkLiveInfo;
import me.taromati.chzzklib.exception.ChzzkException;
import me.taromati.chzzklib.exception.ExceptionCode;
import me.taromati.chzzklib.listener.ChzzkListener;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class ChzzkAPI {

    private String channelId;
    private ChzzkSocket socket;

    private final List<ChzzkListener> listeners = new ArrayList<>();

    public ChzzkAPI(final String channelId) {
        this.channelId = channelId;
    }

    public ChzzkAPI connect() {
        if (!isConnected()) {
            try {
                String channelId = getChannelId(this.channelId);
                String token = getAccessToken(channelId);
                String accessToken = token.split(";")[0];
                String extraToken = token.split(";")[1];
                ChzzkSocket webSocket = new ChzzkSocket(this, "wss://kr-ss1.chat.naver.com/chat", channelId, accessToken, extraToken);
                webSocket.connect();
                this.socket = webSocket;
            } catch (Exception e) {
                this.channelId = null;
                this.socket = null;
            }
        }
        return this;
    }

    public ChzzkAPI disconnect() {
        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
            this.channelId = null;
        }
        return this;
    }

    public ChzzkAPI addListeners(final ChzzkListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
        return this;
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public static ChzzkAPI createAPI(final String channelId) {
        return new ChzzkAPI(channelId);
    }

    public static class ChzzkBuilder {

        private String channelId;

        public ChzzkBuilder withData(final String channelId) {
            this.channelId = channelId;
            return this;
        }

        public ChzzkAPI build() {
            return ChzzkAPI.createAPI(this.channelId);
        }

    }

    public static ChzzkLiveInfo getLiveInfo(String id) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .uri(URI.create("https://api.chzzk.naver.com/polling/v2/channels/" + id + "/live-status"))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(response.body());
                JSONObject content = ((JSONObject)jsonObject.get("content"));

                String liveTitle = content.get("liveTitle").toString();
                List<String> categoryTags = new ArrayList<>();
                for (final Object s : (JSONArray) new JSONParser().parse(content.get("tags").toString())) {
                    categoryTags.add(s.toString());
                }
                int concurrentUserCount = Integer.parseInt(content.get("concurrentUserCount").toString());

                request = HttpRequest.newBuilder()
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .uri(URI.create("https://api.chzzk.naver.com/service/v1/channels/" + id))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                        .build();
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    parser = new JSONParser();
                    jsonObject = (JSONObject) parser.parse(response.body());
                    content = ((JSONObject)jsonObject.get("content"));

                    String name = content.get("channelName").toString();
                    String liveType = content.get("channelType").toString();
                    String description = content.get("channelDescription").toString();

                    int followerCount = Integer.parseInt(content.get("followerCount").toString());

                    boolean verifiedMark = Boolean.parseBoolean(content.get("verifiedMark").toString());
                    boolean openLive = Boolean.parseBoolean(content.get("openLive").toString());

                    return new ChzzkLiveInfo(
                            id,
                            name,
                            description,
                            liveTitle,
                            liveType,
                            categoryTags,
                            followerCount,
                            concurrentUserCount,
                            verifiedMark,
                            openLive
                    );
                } else {
                    throw new ChzzkException(ExceptionCode.API_CHAT_CHANNEL_ID_ERROR);
                }
            } else {
                throw new ChzzkException(ExceptionCode.API_CHAT_CHANNEL_ID_ERROR);
            }
        } catch (Exception e) {
            throw new ChzzkException(ExceptionCode.API_CHAT_CHANNEL_ID_ERROR);
        }
    }

    private static String getChannelId(String id) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .uri(URI.create("https://api.chzzk.naver.com/polling/v2/channels/" + id + "/live-status"))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(response.body());
                return ((JSONObject)jsonObject.get("content")).get("chatChannelId").toString();
            } else {
                throw new ChzzkException(ExceptionCode.API_CHAT_CHANNEL_ID_ERROR);
            }
        } catch (Exception e) {
            throw new ChzzkException(ExceptionCode.API_CHAT_CHANNEL_ID_ERROR);
        }
    }

    private static String getAccessToken(String chatChannelId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().method("GET", HttpRequest.BodyPublishers.noBody())
                    .uri(URI.create("https://comm-api.game.naver.com/nng_main/v1/chats/access-token?channelId=" + chatChannelId + "&chatType=STREAMING"))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(response.body());
                String accessToken = ((JSONObject)jsonObject.get("content")).get("accessToken").toString();
                String extraToken = ((JSONObject)jsonObject.get("content")).get("extraToken").toString();
                return accessToken + ";" + extraToken;
            } else {
                throw new ChzzkException(ExceptionCode.API_ACCESS_TOKEN_ERROR);
            }
        } catch (Exception e) {
            throw new ChzzkException(ExceptionCode.API_ACCESS_TOKEN_ERROR);
        }
    }

}