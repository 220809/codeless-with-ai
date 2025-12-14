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

/**
 * 代码生成类型枚举
 */
export enum CodeGenTypeEnum {
  HTML = 'html',
  MULTI_FILE = 'multi_file',
  VUE_PROJECT = 'vue_project',
}

/**
 * 代码生成类型配置
 */
export const CODE_GEN_TYPE_CONFIG = {
  [CodeGenTypeEnum.HTML]: {
    label: '原生 HTML 模式',
    value: CodeGenTypeEnum.HTML,
  },
  [CodeGenTypeEnum.MULTI_FILE]: {
    label: '原生多文件模式',
    value: CodeGenTypeEnum.MULTI_FILE,
  },
  [CodeGenTypeEnum.VUE_PROJECT]: {
    label: 'Vue 项目模式',
    value: CodeGenTypeEnum.VUE_PROJECT,
  },
}

