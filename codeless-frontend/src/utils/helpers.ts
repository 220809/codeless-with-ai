/**
 * 检查用户是否是管理员
 */
export const isAdmin = (userRole?: number): boolean => {
  return userRole === 1
}

/**
 * 检查用户是否有权限编辑应用（本人或管理员）
 */
export const canEditApp = (appUserId?: number, currentUserId?: number, currentUserRole?: number): boolean => {
  if (!appUserId || !currentUserId) return false
  return appUserId === currentUserId || isAdmin(currentUserRole)
}

