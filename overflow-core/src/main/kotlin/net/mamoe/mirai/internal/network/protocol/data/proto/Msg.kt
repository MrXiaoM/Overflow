/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.isSameType

@Serializable
internal class ImMsgBody /*: ProtoBuf*/ {

    @Serializable
    internal data class MarketFace(
        /*@ProtoNumber(1) */@JvmField var faceName: ByteArray = EMPTY_BYTE_ARRAY,
        /*@ProtoNumber(2) */@JvmField val itemType: Int = 0,
        /*@ProtoNumber(3) */@JvmField val faceInfo: Int = 0,
        /*@ProtoNumber(4) */@JvmField val faceId: ByteArray = EMPTY_BYTE_ARRAY,
        /*@ProtoNumber(5) */@JvmField val tabId: Int = 0,
        /*@ProtoNumber(6) */@JvmField val subType: Int = 0,
        /*@ProtoNumber(7) */@JvmField val key: ByteArray = EMPTY_BYTE_ARRAY,
        /*@ProtoNumber(8) */@JvmField val param: ByteArray = EMPTY_BYTE_ARRAY,
        /*@ProtoNumber(9) */@JvmField val mediaType: Int = 0,
        /*@ProtoNumber(10) */@JvmField val imageWidth: Int = 0,
        /*@ProtoNumber(11) */@JvmField val imageHeight: Int = 0,
        /*@ProtoNumber(12) */@JvmField val mobileParam: ByteArray = EMPTY_BYTE_ARRAY,
        /*@ProtoNumber(13) */@JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
    ) /*: ProtoBuf*/ {
        @Suppress("DuplicatedCode")
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (!isSameType(this, other)) return false

            if (!faceName.contentEquals(other.faceName)) return false
            if (itemType != other.itemType) return false
            if (faceInfo != other.faceInfo) return false
            if (!faceId.contentEquals(other.faceId)) return false
            if (tabId != other.tabId) return false
            if (subType != other.subType) return false
            if (!key.contentEquals(other.key)) return false
            if (!param.contentEquals(other.param)) return false
            if (mediaType != other.mediaType) return false
            if (imageWidth != other.imageWidth) return false
            if (imageHeight != other.imageHeight) return false
            if (!mobileParam.contentEquals(other.mobileParam)) return false
            if (!pbReserve.contentEquals(other.pbReserve)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = faceName.contentHashCode()
            result = 31 * result + itemType
            result = 31 * result + faceInfo
            result = 31 * result + faceId.contentHashCode()
            result = 31 * result + tabId
            result = 31 * result + subType
            result = 31 * result + key.contentHashCode()
            result = 31 * result + param.contentHashCode()
            result = 31 * result + mediaType
            result = 31 * result + imageWidth
            result = 31 * result + imageHeight
            result = 31 * result + mobileParam.contentHashCode()
            result = 31 * result + pbReserve.contentHashCode()
            return result
        }
    }
}