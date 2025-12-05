import type { Rule } from 'ant-design-vue'

export const accountValidator = async (_rule: Rule, value: string) => {
  if (!value || (value.length < 4 || value.length > 11)) {
    return Promise.reject('账号长度应为4-11位');
  }
  if (!value.match(/^[a-zA-Z0-9]{4,11}$/)) {
    return Promise.reject('账号不合法');
  }

}

export const passwordValidator = async (_rule: Rule, value: string) => {
  if (!value || (value.length < 8 || value.length > 16)) {
    return Promise.reject('密码长度应为8-16位');
  }
  if (!value.match(/^[a-zA-Z0-9#$%&'\u0022()*+,\-.\/:;<=>?@\[\]^_{|}~\\]{8,16}$/)) {
    return Promise.reject('密码不合法');
  }

}

export const usernameValidator = async (_rule: Rule, value: string) => {
  if (!value || (value.length < 3 || value.length > 16)) {
    return Promise.reject('用户名长度应为3-16位');
  }
  if (!value.match(/^\S{3,16}$/)) {
    return Promise.reject('用户名不合法');
  }
}
