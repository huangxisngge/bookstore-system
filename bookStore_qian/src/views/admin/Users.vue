<template>
  <div>
    <el-card>
      <div class="toolbar">
        <h3 style="margin:0">用户管理</h3>
      </div>

      <el-table :data="users" v-loading="loading" style="width:100%;margin-top:12px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
        <el-table-column label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">{{ row.role === 1 ? '管理员' : '用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="会员等级" width="90">
          <template #default="{ row }">{{ MEMBER_LEVEL[row.memberLevel] || '普通会员' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.role !== 1"
              text :type="row.status === 1 ? 'danger' : 'success'" size="small"
              @click="toggleStatus(row)"
            >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0" style="margin-top:16px;justify-content:center"
        background layout="prev, pager, next, total"
        :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadUsers"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MEMBER_LEVEL } from '@/utils/constants'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

onMounted(() => loadUsers())

async function loadUsers() {
  loading.value = true
  try {
    const res = await request.get('/user/list', { params: { current: currentPage.value, size: pageSize.value } })
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    users.value = []
  } finally { loading.value = false }
}

async function toggleStatus(user) {
  const action = user.status === 1 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定${action}用户 ${user.username}？`, '操作确认')
  await request.put('/user/admin/update', { id: user.id, status: user.status === 1 ? 0 : 1 })
  ElMessage.success(`已${action}`)
  loadUsers()
}
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
</style>
