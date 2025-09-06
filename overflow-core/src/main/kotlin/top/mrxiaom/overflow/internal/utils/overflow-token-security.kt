package top.mrxiaom.overflow.internal.utils

import kotlin.math.log2

/**
 * ================================================
 * Author:     886kagg
 * Created on: 2025/9/6 20:31
 * ================================================
 */


/**
 * 计算密码复杂度，算法如下：
 * - 按是否是数字分类，分别计算每个分类下排序字符串的 Shannon Entropy 并求和
 * - 按是否是字母分类，分别计算每个分类下排序字符串的 Shannon Entropy 并求和
 * - 按是否是特殊字符分类，分别计算每个分类下排序字符串的 Shannon Entropy 并求和
 * - 密码复杂度 = 三个分类下的熵和 / 3
 */
fun String.securityLength(): Double = if (isBlank()) 0.0 else run {
    val a = groupBy { it.isDigit() }.values.sumOf { it.joinToString("").shannonEntropy() } //数字分割
    val b = groupBy { it.isLetter() }.values.sumOf { it.joinToString("").shannonEntropy() } //大小写分割
    val c = groupBy { it.isLetterOrDigit() }.values.sumOf { it.joinToString("").shannonEntropy() } //特殊符号分割

    return (a + b + c) / 3
}


private fun String.shannonEntropy() = groupingBy { it }.eachCount().values.sumOf {
    val p = it.toDouble() / length
    -p * log2(p)
}
