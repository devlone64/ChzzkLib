package me.taromati.chzzklib;

import me.taromati.chzzklib.event.ChzzkEvent;
import me.taromati.chzzklib.event.implement.DonationChatEvent;
import me.taromati.chzzklib.event.implement.MessageChatEvent;
import me.taromati.chzzklib.event.implement.SubscriptionChatEvent;
import me.taromati.chzzklib.listener.ChzzkListener;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.util.Objects;

public class ChzzkSocket extends WebSocketClient {

    private final ChzzkAPI api;
    private final String channelId;
    private final String accessToken;
    private final String extraToken;

    private Thread pingThread;
    private boolean isAlive = true;

    private final JSONParser parser = new JSONParser();

    private static final int CHZZK_CHAT_CMD_PING = 0;
    private static final int CHZZK_CHAT_CMD_PONG = 10000;
    private static final int CHZZK_CHAT_CMD_CONNECT = 100;

    private static final int CHZZK_CHAT_CMD_CHAT = 93101;
    private static final int CHZZK_CHAT_CMD_DONATION = 93102;

    public ChzzkSocket(final ChzzkAPI api, final String url, final String channelId, final String accessToken, final String extraToken) {
        super(URI.create(url));
        this.setConnectionLostTimeout(0);

        this.api = api;
        this.channelId = channelId;
        this.accessToken = accessToken;
        this.extraToken = extraToken;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        JSONObject baseObject = new JSONObject();
        baseObject.put("ver", "2");
        baseObject.put("svcid", "game");
        baseObject.put("cid", this.channelId);

        JSONObject sendObject = new JSONObject(baseObject);
        sendObject.put("cmd", CHZZK_CHAT_CMD_CONNECT);
        sendObject.put("tid", 1);

        JSONObject bdyObject = new JSONObject();
        bdyObject.put("uid", null);
        bdyObject.put("devType", 2001);
        bdyObject.put("accTkn", this.accessToken);
        bdyObject.put("auth", "READ");

        sendObject.put("bdy", bdyObject);

        send(sendObject.toJSONString());
        pingThread = new Thread(() -> {
            while (isAlive) {
                try {
                    Thread.sleep(19996);
                    JSONObject pongObject = new JSONObject();
                    pongObject.put("cmd", CHZZK_CHAT_CMD_PONG);
                    pongObject.put("ver", 2);
                    send(pongObject.toJSONString());
                } catch (InterruptedException ignore) { }
            }
        });
        pingThread.start();
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject messageObject = (JSONObject) parser.parse(message);
            int cmd = Integer.parseInt(messageObject.get("cmd").toString());
            if (cmd == CHZZK_CHAT_CMD_PING) {
                JSONObject pongObject = new JSONObject();
                pongObject.put("cmd", CHZZK_CHAT_CMD_PONG);
                pongObject.put("ver", 2);
                send(pongObject.toJSONString());
                return;
            }

            if (cmd == CHZZK_CHAT_CMD_PONG)
                return;
            if (cmd != CHZZK_CHAT_CMD_DONATION) {
                JSONObject bdyObject = (JSONObject) ((JSONArray) messageObject.get("bdy")).get(0);
                String nickname = parseNickname(bdyObject);
                String msg = parseMessage(bdyObject);
                boolean verifiedMark = parseVerifiedMark(bdyObject);
                if (nickname != null && msg != null) {
                    msg = msg.isEmpty() ? "없음" : msg;
                    processChatMessage(new MessageChatEvent(this.channelId, nickname, msg, verifiedMark));
                }
                return;
            }

            JSONObject bdyObject = (JSONObject) ((JSONArray) messageObject.get("bdy")).get(0);
            String nickname = "익명";
            String uid = (String) bdyObject.get("uid");
            String msg = (String) bdyObject.get("msg");
            if (!Objects.equals(uid, "anonymous")) {
                nickname = parseNickname(bdyObject);
            }

            int month = 1;
            boolean subscription = false;
            String extras = (String) bdyObject.get("extras");
            JSONObject extraObject = (JSONObject) parser.parse(extras);
            if (extraObject.get("payAmount") == null) {
                subscription = true;
                month = (int) extraObject.get("month");
            }

            int payAmount;
            try {
                payAmount = Integer.parseInt(extraObject.get("payAmount").toString());
            } catch (final NumberFormatException e) {
                payAmount = 0;
            }

            if (nickname != null && msg != null) {
                if (subscription) {
                    processChatMessage(new SubscriptionChatEvent(this.channelId, nickname, month));
                } else {
                    msg = msg.isEmpty() ? "없음" : msg;
                    if (payAmount > 0) {
                        processChatMessage(new DonationChatEvent(this.channelId, nickname, msg, payAmount));
                    }
                }
            }
        } catch (Exception ignored) { }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        isAlive = false;
        if (pingThread != null) {
            pingThread.interrupt();
            pingThread = null;
        }
    }

    @Override
    public void onError(Exception ex) { }

    private void processChatMessage(ChzzkEvent event) {
        for (final ChzzkListener listener : this.api.getListeners()) {
            if (event instanceof DonationChatEvent e) {
                listener.onDonationChat(e);
            } else if (event instanceof MessageChatEvent e) {
                listener.onMessageChat(e);
            } else if (event instanceof SubscriptionChatEvent e) {
                listener.onSubscriptionChat(e);
            }
        }
    }

    private String parseNickname(JSONObject bdyObject) {
        try {
            String profile = (String) bdyObject.get("profile");
            JSONObject profileObejct = (JSONObject) parser.parse(profile);
            return (String) profileObejct.get("nickname");
        } catch (ParseException e) {
            return null;
        }
    }

    private String parseMessage(JSONObject bdyObject) {
        return (String) bdyObject.get("msg");
    }

    private boolean parseVerifiedMark(JSONObject bdyObject) {
        return Boolean.parseBoolean((String) bdyObject.get("verifiedMark"));
    }

}