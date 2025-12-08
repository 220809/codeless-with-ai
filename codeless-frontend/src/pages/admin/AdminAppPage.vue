<template>
  <div id="adminAppPage">
    <a-form
      name="advanced_search"
      class="ant-advanced-search-form"
      :model="searchParams"
      @finish="handleSearch"
    >
      <a-row :gutter="20" style="text-align: center">
        <a-col :span="4" />
        <a-col :span="6">
          <a-form-item name="name" label="应用名称" :label-col="{span: 5}">
            <a-input v-model:value="searchParams.name" allow-clear></a-input>
          </a-form-item>
        </a-col>
        <a-col :span="10">
          <a-row :gutter="24">
            <a-col :span="14">
              <a-form-item name="id" label="应用ID" :label-col="{span: 5}">
                <a-input-number v-model:value="searchParams.id" :min="1" allow-clear style="width: 100%"></a-input-number>
              </a-form-item>
            </a-col>
            <a-col :span="6">
              {{'(ID仅精确搜索)'}}
            </a-col>
          </a-row>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="4" />
        <a-col :span="6">
          <a-form-item name="genFileType" label="文件类型" :label-col="{span: 5}">
            <a-input v-model:value="searchParams.genFileType" allow-clear></a-input>
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item name="priority" label="优先级" :label-col="{span: 5}">
            <a-input-number v-model:value="searchParams.priority" :min="0" allow-clear style="width: 100%"></a-input-number>
          </a-form-item>
        </a-col>
        <a-col :span="6">
          <a-form-item name="userId" label="用户ID" :label-col="{span: 5}">
            <a-input-number v-model:value="searchParams.userId" :min="1" allow-clear style="width: 100%"></a-input-number>
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
      :scroll="{ x: 1400, y: 800 }"
      :pagination="pagination"
      @change="onPageChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'cover'">
          <a-image v-if="record.cover" :src="record.cover" width="64px" />
          <span v-else>-</span>
        </template>
        <template v-if="column.dataIndex === 'name'">
          <a-typography-text :ellipsis="{ tooltip: record.name }" style="max-width: 200px">
            {{ record.name || '未命名应用' }}
          </a-typography-text>
        </template>
        <template v-if="column.dataIndex === 'initialPrompt'">
          <a-typography-text :ellipsis="{ tooltip: record.initialPrompt }" style="max-width: 300px">
            {{ record.initialPrompt || '-' }}
          </a-typography-text>
        </template>
        <template v-if="column.dataIndex === 'priority'">
          <a-tag :color="record.priority === 99 ? 'red' : 'default'">
            {{ record.priority || 0 }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'user'">
          <a-space>
            <a-avatar v-if="record.user?.avatarUrl" :src="record.user.avatarUrl" :size="24" />
            <span>{{ record.user?.username || '-' }}</span>
          </a-space>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY/MM/DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'options'">
          <a-space :size="4">
            <a-button type="primary" ghost size="small" @click="handleEdit(record)">编辑</a-button>
            <a-button danger ghost size="small" @click="handleDelete(record.id)">删除</a-button>
            <a-button 
              type="default" 
              ghost 
              size="small" 
              :disabled="record.priority === 99"
              @click="handleSetFeatured(record)"
            >
              精选
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { message, type TableColumnsType } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { adminDeleteApp, adminPageListApps, adminUpdateApp } from '@/api/app.ts'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'

const router = useRouter()

const columns: TableColumnsType = [
  {
    title: '应用ID',
    width: 100,
    dataIndex: 'id',
  },
  {
    title: '应用名称',
    width: 200,
    dataIndex: 'name',
  },
  {
    title: '封面',
    dataIndex: 'cover',
    width: 100,
  },
  {
    title: '初始提示词',
    dataIndex: 'initialPrompt',
    width: 300,
  },
  {
    title: '文件类型',
    dataIndex: 'genFileType',
    width: 120,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 100,
  },
  {
    title: '用户',
    dataIndex: 'user',
    width: 150,
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
    width: 200,
  },
]

const data = ref<API.AppVo[]>([]);

const defaultSearchParams = {
  pageNum: 1,
  pageSize: 10,
}

const searchParams = reactive<API.AppSearchRequest>({...defaultSearchParams})

const total = ref(0)

const fetchData = async () => {
  const res = await adminPageListApps({ ...searchParams })
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

const handleDelete = async (id: number | string) => {
  if (!id) return
  try {
    // 保持id为原始类型，避免精度丢失
    const res = await adminDeleteApp({ id: id as any })
    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      await message.success('删除成功', 0.5)
      fetchData()
    } else {
      message.error('操作失败, ' + res.data.message)
    }
  } catch (error: any) {
    message.error('删除失败: ' + (error.message || '网络错误'))
  }
}

const handleEdit = (record: API.AppVo) => {
  if (!record.id) return
  // 保持id为原始类型，避免精度丢失
  router.push(`/admin/app/edit?id=${record.id}`)
}

const handleSetFeatured = async (record: API.AppVo) => {
  if (!record.id) return
  try {
    // 保持id为原始类型，避免精度丢失
    const res = await adminUpdateApp({
      id: record.id as any,
      priority: 99,
    })
    if ((res.data.code === 200 || res.data.code === 0) && res.data.data) {
      await message.success('设置精选成功', 0.5)
      fetchData()
    } else {
      message.error('操作失败, ' + res.data.message)
    }
  } catch (error: any) {
    message.error('设置精选失败: ' + (error.message || '网络错误'))
  }
}
</script>
<style>
#adminAppPage {
  max-width: 1300px;
  margin: 16px auto;
}
</style>
