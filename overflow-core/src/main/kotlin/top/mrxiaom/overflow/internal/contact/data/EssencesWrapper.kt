package top.mrxiaom.overflow.internal.contact.data

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.contact.essence.EssenceMessageRecord
import net.mamoe.mirai.contact.essence.Essences
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.contact.GroupWrapper

class EssencesWrapper(
    val impl: GroupWrapper,
    private var list: List<EssenceMessageRecord>
) : Essences {
    override fun asFlow(): Flow<EssenceMessageRecord> {
        return flow {
            var offset = 0
            while (currentCoroutineContext().isActive) {
                val list = impl.fetchEssencesList(offset)
                for (message in list) {
                    emit(message)
                }
                if (list.isEmpty() || list.size < 20) break
                offset++
            }
        }
    }

    override suspend fun getPage(start: Int, limit: Int): List<EssenceMessageRecord> {
        return impl.fetchEssencesList().also { list = it }
    }

    override suspend fun remove(source: MessageSource) {
        impl.checkBotPermission(MemberPermission.ADMINISTRATOR)
        impl.botWrapper.impl.deleteEssenceMsg(source.ids[0])
    }

    override suspend fun share(source: MessageSource): String {
        TODO("Not yet implemented")
    }

    companion object {
        @OptIn(MiraiInternalApi::class)
        suspend fun GroupWrapper.fetchEssencesList(page: Int = 0): List<EssenceMessageRecord> {
            return botWrapper.impl.getEssenceMsgList(id, page).data.map {
                // TODO: 获取不到 NormalMember 时尝试通过 get_member_info 获取
                EssenceMessageRecord(
                    this, this[it.senderId], it.senderId, it.senderNick, it.senderTime.toInt(),
                    this[it.operatorId], it.operatorId, it.operatorNick, it.operatorTime.toInt()
                ) { parse ->
                    MessageSourceBuilder().apply {
                        id(it.messageId)
                        internalId(it.messageId)
                        time(it.senderTime.toInt())
                        target(this@fetchEssencesList)
                        sender(it.senderId)
                        if (parse) {
                            botWrapper.getMsg(it.messageId)?.also { msg -> messageChainOf(msg) }
                        }
                    }.build(bot.id, MessageSourceKind.GROUP)
                }
            }
        }
        suspend fun GroupWrapper.fetchEssences(): EssencesWrapper {
            return EssencesWrapper(this, fetchEssencesList())
        }
    }
}
