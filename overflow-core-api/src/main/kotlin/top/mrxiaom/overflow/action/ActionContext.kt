package top.mrxiaom.overflow.action

import kotlinx.serialization.Serializable

/**
 * Onebot 主动操作上下文
 */
@Serializable
data class ActionContext(
    val action: String,
    /**
     * 是否在调用失败时抛出异常。
     *
     * - 如果为 `false`（默认），则使用 warn 级别打印一条日志
     * - 如果为 `true`，则抛出异常
     * - 如果为 `null`，则使用 trace 级别打印日志，不抛出异常
     */
    var throwExceptions: Boolean? = false,
    /**
     * 是否将返回结果没有 `"status"` 的情况，当作调用成功。
     *
     * 若开启此选项，明确出现网络问题或者明确指定 `"status": "failed"` 才会当作调用失败。
     */
    var ignoreStatus: Boolean = false,
) {
    class Builder private constructor(action: String) {
        private val context = ActionContext(action)

        /**
         * @see ActionContext.throwExceptions
         */
        fun throwExceptions(flag: Boolean?) {
            context.throwExceptions = flag
        }

        /**
         * (旧版方法) 是否在报错时显示警告日志
         *
         * 迁移说明:
         * - `null` 的效果，与 `showWarning=false` 一致
         * - `false` 的效果，与 `showWarning=true` 一致
         * @see ActionContext.throwExceptions
         */
        fun showWarning(flag: Boolean) {
            if (flag) {
                context.throwExceptions = false
            } else {
                context.throwExceptions = null
            }
        }

        /**
         * @see ActionContext.ignoreStatus
         */
        fun ignoreStatus(flag: Boolean) {
            context.ignoreStatus = flag
        }

        fun build(): ActionContext = context

        companion object {
            fun create(action: String): Builder = Builder(action)
        }
    }
    companion object {
        @JvmStatic
        fun builder(action: String): Builder = Builder.create(action)
    }
}
