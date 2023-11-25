package cn.evole.onebot.sdk.util;


import cn.evole.onebot.sdk.entity.OneBotMedia;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 13:48
 * Version: 1.0
 */
public class MsgUtils {
    StringBuffer stringBuffer = new StringBuffer();

    /**
     * 消息构建
     *
     * @return {@link MsgUtils}
     */
    public static MsgUtils builder() {
        return new MsgUtils();
    }

    /**
     * 文本内容
     *
     * @param text 内容
     * @return {@link String}
     */
    public MsgUtils text(String text) {
        stringBuffer.append(text);
        return this;
    }

    /**
     * 图片
     *
     * @param url 图片 URL
     * @return {@link String}
     */
    public MsgUtils img(String url) {
        String imgCode = String.format("[CQ:image,file=%s]", BotUtils.escape(url));
        stringBuffer.append(imgCode);
        return this;
    }

    /**
     * 图片
     *
     * @param img {@link OneBotMedia}
     * @return {@link String}
     */
    public MsgUtils img(OneBotMedia img) {
        stringBuffer.append("[CQ:image,").append(img.escape()).append("]");
        return this;
    }

    /**
     * 短视频
     *
     * @param video 视频地址, 支持 http 和 file 发送
     * @param cover 视频封面, 支持 http, file 和 base64 发送, 格式必须为 jpg
     * @return {@link String}
     */
    public MsgUtils video(String video, String cover) {
        String videoCode = String.format("[CQ:video,file=%s,cover=%s]", BotUtils.escape(video), BotUtils.escape(cover));
        stringBuffer.append(videoCode);
        return this;
    }

    /**
     * 闪照
     *
     * @param img 图片
     * @return {@link String}
     */
    public MsgUtils flashImg(String img) {
        String flashImgCode = String.format("[CQ:image,type=flash,file=%s]", BotUtils.escape(img));
        stringBuffer.append(flashImgCode);
        return this;
    }

    /**
     * QQ 表情
     * <a href="https://github.com/kyubotics/coolq-http-api/wiki/%E8%A1%A8%E6%83%85-CQ-%E7%A0%81-ID-%E8%A1%A8">对照表</a>
     *
     * @param id QQ 表情 ID
     * @return {@link String}
     */
    public MsgUtils face(int id) {
        String faceCode = String.format("[CQ:face,id=%s]", id);
        stringBuffer.append(faceCode);
        return this;
    }

    /**
     * 语音
     *
     * @param record 语音文件名
     * @return {@link String}
     */
    public MsgUtils record(String record) {
        String recordCode = String.format("[CQ:record,file=%s]", BotUtils.escape(record));
        stringBuffer.append(recordCode);
        return this;
    }

    /**
     * at 某人
     *
     * @param userId at 的 QQ 号, all 表示全体成员
     * @return {@link String}
     */
    public MsgUtils at(long userId) {
        String atCode = String.format("[CQ:at,qq=%s]", userId);
        stringBuffer.append(atCode);
        return this;
    }

    /**
     * at 全体成员
     *
     * @return {@link String}
     */
    public MsgUtils atAll() {
        stringBuffer.append("[CQ:at,qq=all]");
        return this;
    }

    /**
     * 戳一戳
     *
     * @param userId 需要戳的成员
     * @return {@link String}
     */
    public MsgUtils poke(long userId) {
        String pokeCode = String.format("[CQ:poke,qq=%s]", userId);
        stringBuffer.append(pokeCode);
        return this;
    }

    /**
     * 回复
     *
     * @param msgId 回复时所引用的消息 id, 必须为本群消息.
     * @return {@link String}
     */
    public MsgUtils reply(int msgId) {
        String replyCode = String.format("[CQ:reply,id=%s]", msgId);
        stringBuffer.append(replyCode);
        return this;
    }

    /**
     * 回复-频道
     *
     * @param msgId 回复时所引用的消息 id, 必须为本频道消息.
     * @return {@link String}
     */
    public MsgUtils reply(String msgId) {
        String replyCode = String.format("[CQ:reply,id=\"%s\"]", msgId);
        stringBuffer.append(replyCode);
        return this;
    }

    /**
     * 礼物
     * 仅支持免费礼物, 发送群礼物消息 无法撤回, 返回的 message id 恒定为 0
     *
     * @param userId 接收礼物的成员
     * @param giftId 礼物的类型
     * @return {@link String}
     */
    public MsgUtils gift(long userId, int giftId) {
        String giftCode = String.format("[CQ:gift,qq=%s,id=%s]", userId, giftId);
        stringBuffer.append(giftCode);
        return this;
    }

    /**
     * 文本转语音
     * 通过腾讯的 TTS 接口, 采用的音源与登录账号的性别有关
     *
     * @param text 内容
     * @return {@link String}
     */
    public MsgUtils tts(String text) {
        String ttsCode = String.format("[CQ:tts,text=%s]", BotUtils.escape(text));
        stringBuffer.append(ttsCode);
        return this;
    }

    /**
     * XML 消息
     *
     * @param data xml内容, xml 中的 value 部分, 记得实体化处理
     * @return {@link String}
     */
    public MsgUtils xml(String data) {
        String xmlCode = String.format("[CQ:xml,data=%s]", BotUtils.escape(data));
        stringBuffer.append(xmlCode);
        return this;
    }

    /**
     * XML 消息
     *
     * @param data  xml 内容, xml 中的 value部分, 记得实体化处理
     * @param resId 可以不填
     * @return {@link String}
     */
    public MsgUtils xml(String data, int resId) {
        String xmlCode = String.format("[CQ:xml,data=%s,resid=%s]", BotUtils.escape(data), resId);
        stringBuffer.append(xmlCode);
        return this;
    }

    /**
     * JSON 消息
     *
     * @param data json 内容, json 的所有字符串记得实体化处理
     * @return {@link String}
     */
    public MsgUtils json(String data) {
        String ttsCode = String.format("[CQ:json,data=%s]", BotUtils.escape(data));
        stringBuffer.append(ttsCode);
        return this;
    }

    /**
     * JSON 消息
     *
     * @param data  json 内容, json 的所有字符串记得实体化处理
     * @param resId 默认不填为 0, 走小程序通道, 填了走富文本通道发送
     * @return {@link String}
     */
    public MsgUtils json(String data, int resId) {
        String jsonCode = String.format("[CQ:json,data=%s,resid=%s]", BotUtils.escape(data), resId);
        stringBuffer.append(jsonCode);
        return this;
    }

    /**
     * 一种 xml 的图片消息
     * xml 接口的消息都存在风控风险, 请自行兼容发送失败后的处理 ( 可以失败后走普通图片模式 )
     *
     * @param file 和 image 的 file 字段对齐, 支持也是一样的
     * @return {@link String}
     */
    public MsgUtils cardImage(String file) {
        String cardImageCode = String.format("[CQ:cardimage,file=%s]", BotUtils.escape(file));
        stringBuffer.append(cardImageCode);
        return this;
    }

    /**
     * 一种 xml 的图片消息
     * xml 接口的消息都存在风控风险, 请自行兼容发送失败后的处理 ( 可以失败后走普通图片模式 )
     *
     * @param file      和 image 的 file 字段对齐, 支持也是一样的
     * @param minWidth  默认不填为 400, 最小 width
     * @param minHeight 默认不填为 400, 最小 height
     * @param maxWidth  默认不填为 500, 最大 width
     * @param maxHeight 默认不填为 1000, 最大 height
     * @param source    分享来源的名称, 可以留空
     * @param icon      分享来源的 icon 图标 url, 可以留空
     * @return {@link String}
     */
    public MsgUtils cardImage(String file, long minWidth, long minHeight, long maxWidth, long maxHeight, String source, String icon) {
        String cardImageCode =
                String.format("[CQ:cardimage,file=%s,minwidth=%s,minheight=%s,maxwidth=%s,maxheight=%s,source=%s,icon=%s]",
                        BotUtils.escape(file), minWidth, minHeight, maxWidth, maxHeight, BotUtils.escape(source),
                        BotUtils.escape(icon));
        stringBuffer.append(cardImageCode);
        return this;
    }

    /**
     * 音乐分享
     *
     * @param type qq 163 xm (分别表示使用 QQ 音乐、网易云音乐、虾米音乐)
     * @param id   歌曲 ID
     * @return {@link String}
     */
    public MsgUtils music(String type, long id) {
        String musicCode = String.format("[CQ:music,type=%s,id=%s]", BotUtils.escape(type), id);
        stringBuffer.append(musicCode);
        return this;
    }

    /**
     * 音乐自定义分享
     *
     * @param url     点击后跳转目标 URL
     * @param audio   音乐 URL
     * @param title   标题
     * @param content 发送时可选，内容描述
     * @param image   发送时可选，图片 URL
     * @return {@link String}
     */
    public MsgUtils customMusic(String url, String audio, String title, String content, String image) {
        String customMusicCode = String.format(
                "[CQ:music,type=custom,url=%s,audio=%s,title=%s,content=%s,image=%s]",
                BotUtils.escape(url), BotUtils.escape(audio), BotUtils.escape(title), BotUtils.escape(content),
                BotUtils.escape(image)
        );
        stringBuffer.append(customMusicCode);
        return this;
    }

    /**
     * 音乐自定义分享
     *
     * @param url   点击后跳转目标 URL
     * @param audio 音乐 URL
     * @param title 标题
     * @return {@link String}
     */
    public MsgUtils customMusic(String url, String audio, String title) {
        String customMusicCode = String.format(
                "[CQ:music,type=custom,url=%s,audio=%s,title=%s]",
                BotUtils.escape(url), BotUtils.escape(audio), BotUtils.escape(title)
        );
        stringBuffer.append(customMusicCode);
        return this;
    }

    /**
     * 发送猜拳消息
     *
     * @param value 0石头 1剪刀 2布
     * @return {@link String}
     */
    public MsgUtils rps(int value) {
        String rpsCode = String.format("[CQ:rps,value=%s]", value);
        stringBuffer.append(rpsCode);
        return this;
    }

    /**
     * 构建消息链
     *
     * @return {@link String}
     */
    public String build() {
        return stringBuffer.toString();
    }
}
