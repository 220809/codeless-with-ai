// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 应用预览 GET /app/preview/${param0}/&#42;&#42; */
export async function appPreview(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.appPreviewParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<string>(`/app/preview/${param0}/**`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
