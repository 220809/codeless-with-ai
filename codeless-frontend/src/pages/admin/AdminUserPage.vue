<template>
  <div id="adminUserPage">
    <a-form
      name="advanced_search"
      class="ant-advanced-search-form"
      :model="searchParams"
      @finish="handleSearch"
    >
      <a-row :gutter="20" style="text-align: center">
        <a-col :span="4" />
        <a-col :span="6">
          <a-form-item name="userName" label="用户名" :label-col="{span: 5}">
            <a-input v-model:value="searchParams.username" allow-clear></a-input>
          </a-form-item>
        </a-col>
        <a-col :span="10">
          <a-row :gutter="24">
            <a-col :span="14">
              <a-form-item name="userAccount" label="账号" :label-col="{span: 5}">
                <a-input v-model:value="searchParams.userAccount" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="6">
              {{'(账号仅精确搜索)'}}
            </a-col>
          </a-row>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="4" />
        <a-col :span="6">
          <a-form-item name="gender" label="性别" :label-col="{span: 5}">
            <a-select v-model:value="searchParams.gender" allow-clear>
              <a-select-option :key="0">未知</a-select-option>
              <a-select-option :key="1">男</a-select-option>
              <a-select-option :key="2">女</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item name="userRole" label="角色" :label-col="{span: 5}">
            <a-select v-model:value="searchParams.userRole" allow-clear>
              <a-select-option :key="0">用户</a-select-option>
              <a-select-option :key="1">管理员</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row>
        <a-col :span="16" style="text-align: right">
          <a-button type="primary" html-type="submit">搜索</a-button>
        </a-col>
      </a-row>
    </a-form>
    <a-divider />
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
          {{ dayjs(record.createTime).format('YYYY/MM/DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'options'">
          <a-space :size="4">
            <a-button type="primary" ghost size="small" @click="handleEdit(record)">修改</a-button>
            <a-button danger ghost size="small" @click="handleDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
    <a-modal v-model:open="editModalOpen" title="修改用户信息" destroy-on-close :footer="null">
      <user-edit-page :edit-user="editUser" />
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { message, type TableColumnsType } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, pageListUser } from '@/api/user.ts'
import { SmileTwoTone, UserOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import UserEditPage from '@/pages/user/UserEditPage.vue'
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

const data = ref<API.AdminUserVo[]>([]);

const defaultSearchParams = {
  pageNum: 1,
  pageSize: 10,
}

const searchParams = reactive<API.UserSearchRequest>({...defaultSearchParams})

const total = ref(0)

const userGenderEnum = {
  0: '-',
  1: '男',
  2: '女',
}

const userRoleEnum = {
  0: '用户',
  1: '管理员',
}

const editModalOpen = ref(false);
const editUser = ref<API.LoginUserVo>();

const fetchData = async () => {
  const res = await pageListUser({ ...searchParams })
  if (res.data.code === 200) {
    data.value = res.data.data?.records ?? []
    total.value = res.data.data?.totalRow ?? 0
  } else {
    message.error('数据获取失败, ' + res.data.message)
  }
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
  pagination.value.current = 1;
  fetchData();
}

const handleDelete = async (id: number) => {
  const res = await deleteUser({id});
  if (res.data.code === 200) {
    await message.success('删除成功', .5);
    window.location.reload();
  } else {
    message.error('操作失败, ' + res.data.message);
  }
}

const handleEdit = (record: any) => {
  editUser.value = record;
  editModalOpen.value = true;
}
</script>
<style>
#adminUserPage {
  max-width: 1300px;
  margin: 16px auto;
}
</style>
