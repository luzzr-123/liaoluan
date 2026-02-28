package www.luuzr.liaoluan.data.model

import kotlinx.serialization.Serializable

/**
 * 优先级枚举 — 对应原型中的 !!!/!!/! 三级
 */
@Serializable
enum class Priority {
    LOW,    // ! 绿色
    MEDIUM, // !! 黄色
    HIGH    // !!! 红色
}
