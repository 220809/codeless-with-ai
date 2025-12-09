import dayjs from 'dayjs'

/**
 * 格式化时间为相对时间（刚刚创建、X小时前、X天前等）
 */
export const formatRelativeTime = (time?: string): string => {
  if (!time) return ''
  const now = dayjs()
  const createTime = dayjs(time)
  const diffHours = now.diff(createTime, 'hour')
  const diffDays = now.diff(createTime, 'day')
  const diffWeeks = now.diff(createTime, 'week')

  if (diffHours < 1) {
    return '刚刚创建'
  } else if (diffHours < 24) {
    return `创建于${diffHours}小时前`
  } else if (diffDays < 7) {
    return `创建于${diffDays}天前`
  } else if (diffWeeks < 4) {
    return `创建于${diffWeeks}周前`
  } else {
    return createTime.format('YYYY-MM-DD')
  }
}

/**
 * 格式化时间为标准格式 YYYY-MM-DD HH:mm:ss
 */
export const formatDateTime = (time?: string): string => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化时间为日期格式 YYYY/MM/DD HH:mm:ss
 */
export const formatDateTimeSlash = (time?: string): string => {
  if (!time) return '-'
  return dayjs(time).format('YYYY/MM/DD HH:mm:ss')
}

