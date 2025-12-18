declare namespace API {
  type adminGetAppByIdParams = {
    id: number
  }

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

  type AppAddRequest = {
    initialPrompt?: string
  }

  type AppAdminUpdateRequest = {
    id?: number
    name?: string
    cover?: string
    priority?: number
  }

  type AppDeployRequest = {
    appId?: number
  }

  type appPreviewParams = {
    dirName: string
  }

  type AppSearchRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    name?: string
    initialPrompt?: string
    genFileType?: string
    deployKey?: string
    priority?: number
    userId?: number
  }

  type AppUpdateRequest = {
    id?: number
    name?: string
  }

  type AppVo = {
    id?: number
    name?: string
    cover?: string
    initialPrompt?: string
    genFileType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: number
    user?: LoginUserVo
    createTime?: string
  }

  type BaseResponseAppVo = {
    code?: number
    data?: AppVo
    message?: string
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

  type BaseResponsePageAppVo = {
    code?: number
    data?: PageAppVo
    message?: string
  }

  type BaseResponsePageChatHistory = {
    code?: number
    data?: PageChatHistory
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseVoid = {
    code?: number
    data?: Record<string, any>
    message?: string
  }

  type ChatHistory = {
    id?: number
    messageContent?: string
    messageType?: string
    appId?: number
    userId?: number
    createTime?: string
    updateTime?: string
    deleted?: number
  }

  type ChatHistorySearchRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    appId?: number
    lastRecentCreateTime?: string
    messageType?: string
    userId?: number
  }

  type DeleteRequest = {
    id?: number
  }

  type downloadAppParams = {
    id: number
  }

  type genCodeFromChatParams = {
    appId: number
    userMessage: string
  }

  type getAppByIdParams = {
    id: number
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

  type PageAppVo = {
    records?: AppVo[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type pageListMyChatHistoryParams = {
    appId: number
    pageSize?: number
    lastRecentCreateTime?: string
  }

  type ServerSentEventString = true

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
