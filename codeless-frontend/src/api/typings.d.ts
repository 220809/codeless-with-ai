declare namespace API {
  type AdminUserVo = {
    id?: number
    username?: string
    userAccount?: string
    userIntro?: string
    avatarUrl?: string
    gender?: number
    userRole?: number
    createTime?: string
  }

  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseLoginUserVo = {
    code?: number
    data?: LoginUserVo
    message?: string
  }

  type BaseResponseLong = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePageAdminUserVo = {
    code?: number
    data?: PageAdminUserVo
    message?: string
  }

  type BaseResponseVoid = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type LoginUserVo = {
    id?: number
    username?: string
    userIntro?: string
    userAccount?: string
    avatarUrl?: string
    gender?: number
    userRole?: number
    createTime?: string
    updateTime?: string
  }

  type PageAdminUserVo = {
    records?: AdminUserVo[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type UserAddRequest = {
    username?: string
    userIntro?: string
    userAccount?: string
    password?: string
    gender?: number
    userRole?: number
  }

  type UserLoginRequest = {
    userAccount?: string
    password?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    password?: string
    checkedPassword?: string
  }

  type UserSearchRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    username?: string
    userAccount?: string
    gender?: number
    userRole?: number
  }

  type UserUpdateRequest = {
    id?: number
    username?: string
    userIntro?: string
    userAccount?: string
    password?: string
    gender?: number
    userRole?: number
  }
}
