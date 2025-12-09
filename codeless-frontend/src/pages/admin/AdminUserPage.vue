<template>
  <div id="adminUserPage">
    <a-card class="search-card" :bordered="false">
      <template #title>
        <span class="card-title">用户搜索</span>
      </template>
      <a-form
        name="advanced_search"
        class="ant-advanced-search-form"
        ref="formRef"
        :model="searchParams"
        @finish="handleSearch"
      >
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item name="username" label="用户名">
              <a-input
                v-model:value="searchParams.username"
                placeholder="请输入用户名"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item name="userAccount" label="账号">
              <a-input
                v-model:value="searchParams.userAccount"
                placeholder="请输入账号"
                allow-clear
              />
              <div class="form-tip">账号仅精确搜索</div>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item name="gender" label="性别">
              <a-select v-model:value="searchParams.gender" placeholder="请选择性别" allow-clear>
                <a-select-option :key="0">未知</a-select-option>
                <a-select-option :key="1">男</a-select-option>
                <a-select-option :key="2">女</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item name="userRole" label="角色">
              <a-select v-model:value="searchParams.userRole" placeholder="请选择角色" allow-clear>
                <a-select-option :key="0">用户</a-select-option>
                <a-select-option :key="1">管理员</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="24" style="text-align: right">
            <a-space>
              <a-button @click="handleReset">重置</a-button>
              <a-button type="primary" html-type="submit">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-form>
    </a-card>
    <a-card class="table-card" :bordered="false">
      <template #title>
        <span class="card-title">用户列表</span>
      </template>
      <a-table
        :columns="columns"
        :data-source="data"
        :scroll="{ x: 1200, y: 800 }"
        :pagination="pagination"
        @change="onPageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'avatarUrl'">
            <a-image :src="record.avatarUrl" width="32px" />
          </template>
          <template v-if="column.dataIndex === 'gender'">
            {{ userGenderEnum[record.gender] }}
          </template>
          <template v-if="column.dataIndex === 'userIntro'">
            <a-textarea v-model:value="record.userIntro" disabled :rows="1" />
          </template>
          <template v-if="column.dataIndex === 'userRole'">
            <a-space>
              <SmileTwoTone v-if="record.userRole === 0" />
              <UserOutlined v-if="record.userRole === 1" style="color: #40a9ff" />
              {{ userRoleEnum[record.userRole] }}
            </a-space>
          </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ formatDateTimeSlash(record.createTime) }}
        </template>
          <template v-if="column.dataIndex === 'options'">
            <a-space :size="4">
              <a-button type="primary" ghost size="small" @click="handleEdit(record)"
                >修改</a-button
              >
              <a-button danger ghost size="small" @click="handleDelete(record.id)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
    <a-modal
      v-model:open="editModalOpen"
      title="修改用户信息"
      destroy-on-close
      :footer="null"
      width="960px"
    >
      <user-edit-page :edit-user="editUser" @close-modal="handleCloseModal" />
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { message, type TableColumnsType } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, pageListUser } from '@/api/user.ts'
import { SmileTwoTone, UserOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { formatDateTimeSlash } from '@/utils/date.ts'
import { USER_ROLE_TEXT, GENDER_TEXT } from '@/utils/constants.ts'
import UserEditPage from '@/pages/user/UserEditPage.vue'

const formRef = ref()
const columns: TableColumnsType = [
  {
    title: '用户名',
    width: 100,
    dataIndex: 'username',
  },
  {
    title: '账号',
    width: 100,
    dataIndex: 'userAccount',
  },
  {
    title: '用户头像',
    dataIndex: 'avatarUrl',
    width: 100,
  },
  {
    title: '简介',
    dataIndex: 'userIntro',
    width: 300,
  },
  {
    title: '性别',
    dataIndex: 'gender',
    width: 100,
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 150,
  },
  {
    title: '操作',
    dataIndex: 'options',
    fixed: 'right',
    width: 150,
  },
]

const data = ref<API.AdminUserVo[]>([])

const defaultSearchParams = {
  pageNum: 1,
  pageSize: 10,
}

const searchParams = reactive<API.UserSearchRequest>({ ...defaultSearchParams })

const total = ref(0)

const userGenderEnum = GENDER_TEXT
const userRoleEnum = USER_ROLE_TEXT

const editModalOpen = ref(false)
const editUser = ref<API.LoginUserVo>()

const fetchData = async () => {
  const res = await pageListUser({ ...searchParams })
  if (res.data.code === 200) {
    data.value = res.data.data?.records ?? []
    total.value = res.data.data?.totalRow ?? 0
  } else {
    message.error('数据获取失败, ' + res.data.message)
  }
}

const handleCloseModal = () => {
  editModalOpen.value = false
  fetchData()
}

onMounted(() => {
  fetchData()
})

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (total: number, range: number[]) => `${range[0]}-${range[1]} 共${total}条`,
  hideOnSinglePage: true,
}))

const onPageChange = (pagination: any) => {
  searchParams.pageNum = pagination.current
  searchParams.pageSize = pagination.pageSize
  fetchData()
}

const handleSearch = (values: any) => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

const handleReset = () => {
  Object.assign(searchParams, defaultSearchParams)
  formRef.value.resetFields()
}

const handleDelete = async (id: number) => {
  const res = await deleteUser({ id })
  if (res.data.code === 200) {
    await message.success('删除成功', 0.5)
    window.location.reload()
  } else {
    message.error('操作失败, ' + res.data.message)
  }
}

const handleEdit = (record: any) => {
  editUser.value = record
  editModalOpen.value = true
}
</script>
<style scoped>
#adminUserPage {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.search-card {
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.table-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.form-tip {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}

.ant-advanced-search-form {
  padding: 0;
}

.ant-advanced-search-form .ant-form-item {
  margin-bottom: 0;
}

:deep(.ant-table) {
  background: #fff;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}
</style>
