/**
 * 用户角色枚举
 */
export const USER_ROLE = {
  USER: 0,
  ADMIN: 1,
} as const

/**
 * 用户角色文本映射
 */
export const USER_ROLE_TEXT = {
  [USER_ROLE.USER]: '用户',
  [USER_ROLE.ADMIN]: '管理员',
} as const

/**
 * 性别枚举
 */
export const GENDER = {
  UNKNOWN: 0,
  MALE: 1,
  FEMALE: 2,
} as const

/**
 * 性别文本映射
 */
export const GENDER_TEXT = {
  [GENDER.UNKNOWN]: '未知',
  [GENDER.MALE]: '男',
  [GENDER.FEMALE]: '女',
} as const

/**
 * 应用优先级
 */
export const APP_PRIORITY = {
  FEATURED: 99, // 精选
  MIN: 0,
  MAX: 100,
} as const

