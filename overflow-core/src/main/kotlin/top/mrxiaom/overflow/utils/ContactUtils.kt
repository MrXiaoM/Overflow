package top.mrxiaom.overflow.utils

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * @param list 新的列表
 * @param updater this 是旧的，it 是新的，应当把新的内容放进旧的
 */
@OptIn(MiraiInternalApi::class)
internal inline fun <reified T : Contact> ContactList<T>.update(
    list: List<T>,
    updater: T.(T) -> Unit
) {
    // 删除旧的
    delegate.removeIf { old -> list.none { old.id == it.id } }
    // 更新旧的
    delegate.mapNotNull { old -> list.firstOrNull { old.id == it.id }?.to(old) }.forEach { it.second.updater(it.first) }
    // 添加新的
    delegate.addAll(list.filterNot { delegate.any { old -> old.id == it.id } })
}