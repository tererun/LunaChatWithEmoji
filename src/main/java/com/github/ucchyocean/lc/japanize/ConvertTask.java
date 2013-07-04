/*
 * @author     ucchy
 * @license    GPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.lc.japanize;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.ucchyocean.lc.Channel;
import com.github.ucchyocean.lc.Utility;
import com.github.ucchyocean.lc.event.LunaChatPostJapanizeEvent;

/**
 * Japanize変換を実行して、実行後に発言を行うタスク
 * @author ucchy
 */
public class ConvertTask extends BukkitRunnable {

    private String org;
    private JapanizeType type;
    private Channel channel;
    private Player player;
    private String format;
    private String result;

    /**
     * コンストラクタ
     * @param org 変換前の文字列
     * @param type 変換タイプ
     * @param channel 変換後に発言する、発言先チャンネル
     * @param player 発言したプレイヤー
     * @param format 変換後に発言するときの、発言フォーマット
     */
    public ConvertTask(String org, JapanizeType type, Channel channel, 
            Player player, String format) {
        this.org = org;
        this.type = type;
        this.channel = channel;
        this.player = player;
        this.format = format;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        if ( runSync() ) {

            // チャンネルへ送信
            if ( channel != null ) {
                channel.sendMessage(player, result);
            } else {
                Bukkit.broadcastMessage(result);
            }
        }
    }

    /**
     * 同期処理で変換を行います。結果は getResult() で取得してください。
     * @return 同期処理を実行したかどうか（イベントでキャンセルされた場合はfalseになります）
     */
    public boolean runSync() {

        // カナ変換
        String japanized = KanaConverter.conv(org);

        // IME変換
        if ( type == JapanizeType.GOOGLE_IME ) {
            japanized = IMEConverter.convByGoogleIME(japanized);
        } else if ( type == JapanizeType.SOCIAL_IME ) {
            japanized = IMEConverter.convBySocialIME(japanized);
        }

        // イベントコール
        String channelName = (channel == null) ? "" : channel.getName();
        LunaChatPostJapanizeEvent event =
                new LunaChatPostJapanizeEvent(channelName, org, japanized);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if ( event.isCancelled() ) {
            return false;
        }
        japanized = event.getJapanized();

        // フォーマットする
        result = format.replace("%msg", org);
        result = result.replace("%japanize", japanized);
        result = Utility.replaceColorCode(result);

        return true;
    }

    /**
     * 変換結果を取得します。
     * @return 変換結果
     */
    public String getResult() {
        return result;
    }
}

