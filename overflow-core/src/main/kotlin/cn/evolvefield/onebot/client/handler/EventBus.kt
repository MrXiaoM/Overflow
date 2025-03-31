package cn.evolvefield.onebot.client.handler

import cn.evolvefield.onebot.client.util.ListenerUtils
import cn.evolvefield.onebot.sdk.event.Event
import cn.evolvefield.onebot.sdk.event.UnsolvedEvent
import cn.evolvefield.onebot.sdk.util.JsonHelper.applyJson
import cn.evolvefield.onebot.sdk.util.JsonHelper.gson
import cn.evolvefield.onebot.sdk.util.ignorable
import com.google.gson.JsonParser
import net.mamoe.mirai.utils.currentTimeSeconds
import org.slf4j.LoggerFactory
import top.mrxiaom.overflow.internal.contact.BotWrapper
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

object EventBus {
    private val logger = LoggerFactory.getLogger(EventBus::class.java)
    private val handlers: MutableMap<KClass<*>, MutableList<Handler<*>>> = hashMapOf()

    internal fun addListener(handler: Handler<out Event>) {
        val list = handlers.getOrPut(handler.type) { mutableListOf() }
        list.add(handler)
    }
    internal inline fun <reified T : Event> listen(noinline block: suspend BotWrapper.(T) -> Unit) {
        addListener(BotHandler(T::class, null, block))
    }
    internal inline fun <reified T : Event> listenNormally(noinline block: suspend (T) -> Unit) {
        addListener(NormalHandler(T::class, null, block))
    }
    internal inline fun <reified T : Event> listen(subType: String, noinline block: suspend BotWrapper.(T) -> Unit) {
        val field = T::class.property("subType") ?: return
        addListener(BotHandler(T::class, field.check(subType), block))
    }
    internal inline fun <reified T : Event> listenNormally(subType: String, noinline block: suspend (T) -> Unit) {
        val field = T::class.property("subType") ?: return
        addListener(NormalHandler(T::class, field.check(subType), block))
    }
    private fun <T : Any> KClass<T>.property(name: String): KProperty1<T, *>? {
        val field = declaredMemberProperties
            .firstOrNull { it.name == name }
        if (field == null) {
            logger.warn("无法注册 ${java.name}: 找不到 $name")
            return null
        }
        return field
    }
    private fun <T : Any> KProperty1<T, *>.check(subType: String): (T) -> Boolean {
        return { get(it)?.toString() == subType }
    }

    fun clear() {
        handlers.clear()
    }

    suspend fun onReceive(message: String) {
        val (bean, executes) = matchHandlers(message)
        if (executes.isEmpty()) return
        for (handler in executes) {
            handler.onReceive(bean) //调用监听方法
        }
    }

    private fun matchHandlers(
        message: String
    ): Pair<Event, List<Handler<*>>> {
        val messageType = ListenerUtils[message] // 获取消息对应的实体类型
        val json = JsonParser.parseString(message).asJsonObject
        if (messageType != null) {
            val bean = gson.fromJson(message, messageType.java) // 将消息反序列化为对象
            bean.applyJson(json)
            logger.debug(String.format("接收到上报消息内容：%s", bean.toString()))
            val executes = handlers[messageType] ?: emptyList()
            if (executes.isNotEmpty()) {
                return bean to executes
            }
        }
        // 如果该事件未被监听，将其定为 UnsolvedEvent
        val bean = UnsolvedEvent().also { it.jsonString = message }
        val executes = handlers[UnsolvedEvent::class] ?: emptyList()
        bean.postType = json.ignorable("post_type", "")
        bean.time = json.ignorable("time", currentTimeSeconds())
        bean.selfId = json.ignorable("self_id", 0L)
        bean.applyJson(json)
        return bean to executes
    }
}
