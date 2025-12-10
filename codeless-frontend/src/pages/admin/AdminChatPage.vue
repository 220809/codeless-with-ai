<template>
  <div id="adminChatPage">
    <a-card class="search-card" :bordered="false">
      <template #title>
        <span class="card-title">对话搜索</span>
      </template>
      <a-form
        name="advanced_search"
        class="ant-advanced-search-form"
        :model="searchParams"
        @finish="handleSearch"
        ref="formRef"
      >
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item name="appId" label="应用ID">
              <a-input-number v-model:value="searchParams.appId" :min="1" placeholder="请输入应用ID" allow-clear style="width: 100%" />
              <div class="form-tip">ID仅精确搜索</div>
            </a-form-item>
          </a-col>
          <a-col :xs="24" :sm="12" :md="8" :lg="6">
            <a-form-item name="userId" label="用户ID">
              <a-input-number v-model:value="searchParams.userId" :min="1" placeholder="请输入用户ID" allow-clear style="width: 100%" />
              <div class="form-tip">ID仅精确搜索</div>
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
        <span class="card-title">对话列表</span>
      </template>
      <a-table
        :columns="columns"
        :data-source="data"
        :scroll="{ x: 1400, y: 800 }"
        :pagination="pagination"
        @change="onPageChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'id'">
            <a-typography-text :ellipsis="{ tooltip: record.id }" style="max-width: 200px">
              {{ record.id || '-' }}
            </a-typography-text>
          </template>
          <template v-if="column.dataIndex === 'messageContent'">
            <a-typography-text :ellipsis="{ tooltip: record.messageContent }" style="max-width: 300px">
              {{ record.messageContent || '-' }}
            </a-typography-text>
          </template>
          <template v-if="column.dataIndex === 'messageType'">
            <a-tag :color="record.messageType?.toLowerCase() === 'user' ? 'blue' : 'green'">
              {{ record.messageType || '-' }}
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'createTime'">
            {{ formatDateTimeSlash(record.createTime) }}
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>
<script lang="ts" setup>
import { message, type TableColumnsType } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { adminPageListChatHistory } from '@/api/chatHistory.ts'
import { formatDateTimeSlash } from '@/utils/date.ts'
import { SearchOutlined } from '@ant-design/icons-vue'

const columns: TableColumnsType = [
  {
    title: '对话ID',
    width: 200,
    dataIndex: 'id',
  },
  {
    title: '应用ID',
    width: 150,
    dataIndex: 'appId',
  },
  {
    title: '用户ID',
    width: 150,
    dataIndex: 'userId',
  },
  {
    title: '消息内容',
    width: 300,
    dataIndex: 'messageContent',
  },
  {
    title: '消息类型',
    width: 120,
    dataIndex: 'messageType',
  },
  {
    title: '创建时间',
    width: 150,
    dataIndex: 'createTime',
  },
]

const formRef = ref()

const data = ref<API.ChatHistory[]>([])

const defaultSearchParams = {
  pageNum: 1,
  pageSize: 10,
}

const searchParams = reactive<API.ChatHistorySearchRequest>({ ...defaultSearchParams })

const total = ref(0)

const fetchData = async () => {
  const res = await adminPageListChatHistory({ ...searchParams })
  if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
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
  searchParams.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchParams.appId = undefined
  searchParams.userId = undefined
  searchParams.pageNum = 1
  searchParams.pageSize = 10
  formRef.value.resetFields()
  fetchData()
}
</script>
<style scoped>
#adminChatPage {
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

